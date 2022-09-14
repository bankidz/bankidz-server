package com.ceos.bankids.service;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.NotificationController;
import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Comment;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AchievedChallengeDTO;
import com.ceos.bankids.dto.AchievedChallengeListDTO;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.ChallengeListMapperDTO;
import com.ceos.bankids.dto.ChallengePostDTO;
import com.ceos.bankids.dto.KidAchievedChallengeListDTO;
import com.ceos.bankids.dto.KidWeekDTO;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    // Enum ChallengeStatus
    private static final ChallengeStatus pending = ChallengeStatus.PENDING;
    private static final ChallengeStatus walking = ChallengeStatus.WALKING;
    private static final ChallengeStatus achieved = ChallengeStatus.ACHIEVED;
    private static final ChallengeStatus failed = ChallengeStatus.FAILED;
    private static final ChallengeStatus rejected = ChallengeStatus.REJECTED;

    private final ChallengeRepository challengeRepository;
    private final ChallengeCategoryRepository challengeCategoryRepository;
    private final TargetItemRepository targetItemRepository;
    private final ChallengeUserRepository cuRepo;
    private final ProgressRepository progressRepository;
    private final FamilyUserRepository familyUserRepository;
    private final CommentRepository commentRepository;
    private final KidRepository kidRepository;
    private final ParentRepository parentRepository;
    private final NotificationController notificationController;
    private final FamilyRepository familyRepository;

    // 돈길 생성 API
    @Transactional
    @Override
    public ChallengeDTO createChallenge(User user, ChallengePostDTO challengeRequest) {

        String category = challengeRequest.getChallengeCategory();
        String name = challengeRequest.getItemName();
        ChallengeCategory challengeCategory = challengeCategoryRepository.findByCategory(category);
        TargetItem targetItem = targetItemRepository.findByName(name);

        if (targetItem == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_CATEGORY.getErrorCode());
        }
        if (challengeCategory == null) {
            throw new BadRequestException(ErrorCode.NOT_EXIST_ITEM.getErrorCode());
        }

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getContractUser())
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        challengeRepository.save(newChallenge);

        return new ChallengeDTO(newChallenge, null, null);
    }

    // 돈길 삭제 API (2주에 한번)
    @Transactional
    @Override
    public ChallengeDTO deleteWalkingChallenge(User user, ChallengeUser challengeUser) {

        Challenge deleteChallenge = challengeUser.getChallenge();
        List<Progress> deleteChallengeProgressList = deleteChallenge.getProgressList();
        progressRepository.deleteAll(deleteChallengeProgressList);
        challengeRepository.delete(deleteChallenge);

        return new ChallengeDTO(deleteChallenge, null, null);
    }

    @Override
    public ChallengeDTO deleteRejectedChallenge(User user, ChallengeUser challengeUser) {
        Challenge deleteChallenge = challengeUser.getChallenge();
        Comment comment = deleteChallenge.getComment();
        commentRepository.delete(comment);
        challengeRepository.delete(deleteChallenge);

        return new ChallengeDTO(deleteChallenge, null, null);
    }

    @Override
    public ChallengeDTO deletePendingChallenge(User user, ChallengeUser challengeUser) {
        Challenge deleteChallenge = challengeUser.getChallenge();
        challengeRepository.delete(deleteChallenge);

        return new ChallengeDTO(deleteChallenge, null, null);
    }

    @Transactional
    @Override
    public ChallengeListMapperDTO test(Challenge challenge) {

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        List<Progress> progressList = challenge.getProgressList();
        Long diffWeeks =
            timeLogic(progressList) > challenge.getWeeks() ? challenge
                .getWeeks() + 1 : (long) timeLogic(progressList);
        Long interestRate = challenge.getInterestRate();
        Long risk = 0L;
        Long falseCnt = 0L;
        if (interestRate == 10L) {
            risk = 1000L;
        } else if (interestRate == 20L) {
            risk = 4L;
        } else if (interestRate == 30L) {
            risk = 2L;
        }
        for (Progress progress : progressList) {
            if (progress.getWeeks() <= diffWeeks) {
                if (!progress.getIsAchieved() && progress.getWeeks() < diffWeeks) {
                    falseCnt += 1;
                }
                progressDTOList.add(new ProgressDTO(progress, challenge));
            }
        }
        if (falseCnt >= risk) {
            challenge.setChallengeStatus(failed);
            challengeRepository.save(challenge);
            return new ChallengeListMapperDTO(challenge, progressDTOList, true);
        } else if (diffWeeks > challenge.getWeeks()) {
            challenge.setChallengeStatus(achieved);
//                    Long userLevel = userLevelUp(kid.getAchievedChallenge() + 1);
//                    kid.setAchievedChallenge(kid.getAchievedChallenge() + 1);
//                    if (!Objects.equals(userLevel, kid.getLevel())) {
//                        notificationController.kidLevelUpNotification(
//                            challenge.getContractUser(), user, kid.getLevel(), userLevel);
//                        kid.setLevel(userLevel);
//                    }
            challengeRepository.save(challenge);
//                    kidRepository.save(kid);
//                    notificationController.achieveChallengeNotification(
//                        challenge.getContractUser(), r);
            return new ChallengeListMapperDTO(challenge, progressDTOList, true);
        }
        return new ChallengeListMapperDTO(challenge, progressDTOList, false);
    }

    // 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public List<ChallengeDTO> readChallengeList(User user, List<Challenge> challengeList,
        String status) {

        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        if (Objects.equals(status, "pending")) {
            challengeList.forEach(challenge -> {
                challengeDTOList.add(new ChallengeDTO(challenge, null, challenge.getComment()));
            });
            return challengeDTOList;
        } else {
            challengeList.forEach(challenge -> {
                List<ProgressDTO> progressDTOList = new ArrayList<>();
                List<Progress> progressList = challenge.getProgressList();
                Long diffWeeks =
                    timeLogic(progressList) > challenge.getWeeks() ? challenge
                        .getWeeks() + 1 : (long) timeLogic(progressList);
                Long interestRate = challenge.getInterestRate();
                Long risk = 0L;
                Long falseCnt = 0L;
                if (interestRate == 10L) {
                    risk = 1000L;
                } else if (interestRate == 20L) {
                    risk = 4L;
                } else if (interestRate == 30L) {
                    risk = 2L;
                }
                for (Progress progress : progressList) {
                    if (progress.getWeeks() <= diffWeeks) {
                        if (!progress.getIsAchieved() && progress.getWeeks() < diffWeeks) {
                            falseCnt += 1;
                        }
                        progressDTOList.add(new ProgressDTO(progress, challenge));
                    }
                }
                if (falseCnt >= risk) {
                    challenge.setChallengeStatus(failed);
                    challengeRepository.save(challenge);
//                    notificationController.challengeFailedNotification(
//                        challenge.getContractUser(), r);
                } else if (diffWeeks > challenge.getWeeks()) {
                    challenge.setChallengeStatus(achieved);
//                    Long userLevel = userLevelUp(kid.getAchievedChallenge() + 1);
//                    kid.setAchievedChallenge(kid.getAchievedChallenge() + 1);
//                    if (!Objects.equals(userLevel, kid.getLevel())) {
//                        notificationController.kidLevelUpNotification(
//                            challenge.getContractUser(), user, kid.getLevel(), userLevel);
//                        kid.setLevel(userLevel);
//                    }
                    challengeRepository.save(challenge);
//                    kidRepository.save(kid);
//                    notificationController.achieveChallengeNotification(
//                        challenge.getContractUser(), r);
                }
                if (challenge.getChallengeStatus() != achieved) {
                    challengeDTOList.add(new ChallengeDTO(challenge, progressDTOList,
                        challenge.getComment()));
                } else if (challenge.getChallengeStatus() == failed) {
                    List<Progress> failedProgressList = challenge.getProgressList();
                    List<ProgressDTO> failedProgressDTOList = new ArrayList<>();
                    Long failedDiffWeeks =
                        timeLogic(failedProgressList) > challenge.getWeeks() ? challenge
                            .getWeeks() : (long) timeLogic(failedProgressList);
                    for (Progress progress : failedProgressList) {
                        if (progress.getWeeks() <= failedDiffWeeks) {
                            progressDTOList.add(new ProgressDTO(progress, challenge));
                        }
                    }
                    challengeDTOList.add(
                        new ChallengeDTO(challenge, failedProgressDTOList, challenge
                            .getComment()));
                }
            });
        }

        return challengeDTOList;
    }

//    // 자녀의 돈길 리스트 가져오기 API
//    @Transactional
//    @Override
//    public KidChallengeListDTO readKidChallenge(User user, Long kidId, String status) {
//
//        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
//            .orElseThrow(() -> new ForbiddenException(ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
//        Family family = familyUser.getFamily();
//        User kid = familyUserRepository.findByFamily(family).stream()
//            .filter(f -> f.getUser().getIsKid() && Objects.equals(
//                f.getUser().getKid().getId(), kidId)).map(FamilyUser::getUser).findFirst()
//            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_KID.getErrorCode()));
//        List<ChallengeDTO> challengeDTOList = readChallengeList(kid, status, );
//        if (Objects.equals(status, "pending")) {
//            List<ChallengeDTO> resultList = challengeDTOList.stream()
//                .filter(challengeDTO -> challengeDTO.getIsMom() == user.getIsFemale()).collect(
//                    Collectors.toList());
//            return new KidChallengeListDTO(kid, resultList);
//        }
//        return new KidChallengeListDTO(kid, challengeDTOList);
//    }

    // 돈길 수락 / 거절 API
    @Transactional
    @Override
    public ChallengeDTO updateChallengeStatus(User user, Long challengeId,
        KidChallengeRequest kidChallengeRequest) {

        ChallengeUser findChallengeUser = cuRepo.findByChallengeId(challengeId)
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE.getErrorCode()));
        User cUser = findChallengeUser.getUser();
        Optional<FamilyUser> familyUser = familyUserRepository.findByUserId(cUser.getId());
        Optional<FamilyUser> familyUser1 = familyUserRepository.findByUserId(user.getId());
        Challenge challenge = findChallengeUser.getChallenge();

        familyUser.ifPresent(f -> {
            familyUser1.ifPresent(f1 -> {
                if (f.getFamily() != f1.getFamily()
                    || !Objects.equals(user.getId(), challenge.getContractUser().getId())) {
                    throw new ForbiddenException(ErrorCode.NOT_MATCH_CONTRACT_USER.getErrorCode());
                }
            });
        });

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        if (challenge.getChallengeStatus() != pending) {
            throw new BadRequestException(ErrorCode.ALREADY_APPROVED_CHALLENGE.getErrorCode());
        }
        if (kidChallengeRequest.getAccept()) {
            long count = cuRepo.findByUserId(cUser.getId()).stream()
                .filter(
                    challengeUser -> challengeUser.getChallenge().getChallengeStatus() == walking)
                .count();
            if (count >= 5) {
                throw new ForbiddenException(
                    ErrorCode.KID_CHALLENGE_COUNT_OVER_FIVE.getErrorCode());
            }
            Kid kid = cUser.getKid();
            challenge.setChallengeStatus(walking);
            challengeRepository.save(challenge);

            // 자녀의 총 돈길 + 1
            kid.setTotalChallenge(kid.getTotalChallenge() + 1);
            kidRepository.save(kid);

            // 부모의 수락한 돈길 + 1
            Parent parent = user.getParent();
            parent.setAcceptedRequest(parent.getAcceptedRequest() + 1);
            parentRepository.save(parent);

            for (int i = 1; i <= challenge.getWeeks(); i++) {
                Progress newProgress = Progress.builder().weeks((long) i)
                    .challenge(challenge)
                    .isAchieved(false).build();
                progressDTOList.add(new ProgressDTO(newProgress, challenge));
                progressRepository.save(newProgress);
            }
        } else {
            Comment newComment = Comment.builder().challenge(challenge).content(
                kidChallengeRequest.getComment()).user(user).build();
            challenge.setChallengeStatus(rejected);
            challenge.setComment(newComment);
            commentRepository.save(newComment);
            challengeRepository.save(challenge);
            progressDTOList = null;
        }

        notificationController.notification(challenge, cUser);
        return new ChallengeDTO(challenge, progressDTOList, challenge.getComment());
    }

    // 주차 정보 가져오기 API
    @Transactional
    @Override
    public WeekDTO readWeekInfo(User user) {

        Long[] currentPrice = {0L};
        Long[] totalPrice = {0L};
        List<ChallengeUser> challengeUserList = cuRepo.findByUserId(
            user.getId());
        challengeUserList.forEach(challengeUser -> {
            Challenge challenge = challengeUser.getChallenge();
            if (challenge.getChallengeStatus() == walking) {
                List<Progress> progressList = challenge.getProgressList();
                int diffWeeks = timeLogic(progressList);
                progressList.forEach(progress -> {
                    if (progress.getWeeks() == diffWeeks) {
                        totalPrice[0] += challenge.getWeekPrice();
                        if (progress.getIsAchieved()) {
                            currentPrice[0] += challenge.getWeekPrice();
                        }
                    }
                });
            }
        });

        return new WeekDTO(currentPrice[0], totalPrice[0]);
    }

    // 자녀의 주차 정보 가져오기 API
    @Transactional
    @Override
    public KidWeekDTO readKidWeekInfo(User user, Long kidId) {

        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ForbiddenException(ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
        Family family = familyUser.getFamily();
        User kid = familyUserRepository.findByFamily(family).stream()
            .map(FamilyUser::getUser)
            .filter(fUser -> fUser.getIsKid() && Objects.equals(fUser.getKid().getId(), kidId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_KID.getErrorCode()));

        WeekDTO weekDTO = readWeekInfo(kid);

        return new KidWeekDTO(kid.getKid(), weekDTO);
    }

    // 완주한 돈길만 가져오기 API
    @Transactional
    @Override
    public AchievedChallengeListDTO readAchievedChallenge(User user, String interestPayment) {

        List<Challenge> challengeList = cuRepo.findByUserId(user.getId()).stream()
            .map(ChallengeUser::getChallenge).filter(challenge -> Objects.equals(
                challenge.getChallengeStatus(), achieved))
            .filter(challenge -> {
                if (Objects.equals(interestPayment, "payed")) {
                    return challenge.getIsInterestPayment();
                } else if (Objects.equals(interestPayment, "notPayed")) {
                    return !challenge.getIsInterestPayment();
                } else {
                    return challenge.getChallengeStatus() == achieved;
                }
            })
            .collect(
                Collectors.toList());
        Long[] totalInterestPrice = {0L};
        List<AchievedChallengeDTO> achievedChallengeDTOList = challengeList.stream()
            .map(challenge -> {
                totalInterestPrice[0] += (challenge.getInterestPrice() / challenge.getWeeks())
                    * challenge.getSuccessWeeks();
                return new AchievedChallengeDTO(challenge);
            }).collect(Collectors.toList());
        return new AchievedChallengeListDTO(
            totalInterestPrice[0], achievedChallengeDTOList);
    }

    // 완주한 돈길에 이자 지급 API
    @Transactional
    @Override
    public AchievedChallengeDTO updateChallengeInterestPayment(User user, Long challengeId) {

        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE.getErrorCode()));
        if (!Objects.equals(challenge.getContractUser().getId(), user.getId())) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_CONTRACT_USER.getErrorCode());
        }
        if (challenge.getIsInterestPayment()) {
            throw new BadRequestException(ErrorCode.ALREADY_INTEREST_PAYMENT.getErrorCode());
        }
        if (challenge.getChallengeStatus() != achieved) {
            throw new BadRequestException(ErrorCode.NOT_ALREADY_ACHIEVED_CHALLENGE.getErrorCode());
        }
        challenge.setIsInterestPayment(true);
        challengeRepository.save(challenge);

        return new AchievedChallengeDTO(challenge);
    }

    //자녀의 완주한 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public KidAchievedChallengeListDTO readKidAchievedChallenge(User user, Long kidId,
        String interestPayment) {

        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ForbiddenException(ErrorCode.FAMILY_NOT_EXISTS.getErrorCode()));
        Kid kid = kidRepository.findById(kidId)
            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_KID.getErrorCode()));
        User kidUser = kid.getUser();
        FamilyUser kidFamilyUser = familyUserRepository.findByUserId(kidUser.getId())
            .orElseThrow(() -> new BadRequestException(ErrorCode.FAMILY_NOT_EXISTS.getErrorCode()));
        if (familyUser.getFamily() != kidFamilyUser.getFamily()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_FAMILY.getErrorCode());
        }

        if (!Objects.equals(interestPayment, "payed") && !Objects.equals(interestPayment,
            "notPayed")) {
            throw new BadRequestException(ErrorCode.QUERY_PARAM_ERROR.getErrorCode());
        }

        AchievedChallengeListDTO achievedChallengeListDTO = readAchievedChallenge(kidUser,
            interestPayment);
        List<AchievedChallengeDTO> challengeDTOList = achievedChallengeListDTO.getChallengeDTOList();
        List<AchievedChallengeDTO> contractUserChallengeDTOList = challengeDTOList.stream().filter(
            achievedChallengeDTO -> achievedChallengeDTO.getChallenge().getIsMom()
                == user.getIsFemale()).collect(
            Collectors.toList());
        achievedChallengeListDTO.setChallengeDTOList(contractUserChallengeDTOList);
        Long[] totalInterestPrice = {0L};
        contractUserChallengeDTOList.forEach(challenge -> {
            totalInterestPrice[0] +=
                (challenge.getChallenge().getInterestPrice() / challenge.getChallenge().getWeeks())
                    * challenge.getChallenge().getSuccessWeeks();
        });
        achievedChallengeListDTO.setTotalInterestPrice(totalInterestPrice[0]);

        return new KidAchievedChallengeListDTO(kidId, achievedChallengeListDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public Challenge readChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE.getErrorCode()));
    }

    private int timeLogic(List<Progress> progressList) {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        int dayOfWeek = nowCal.get(Calendar.DAY_OF_WEEK);
        Progress progress1 = progressList.stream().findFirst()
            .orElseThrow(() -> new ForbiddenException(ErrorCode.TIMELOGIC_ERROR.getErrorCode()));
        Timestamp createdAt1 = progress1.getCreatedAt();
        Calendar createdAtCal = Calendar.getInstance();
        createdAtCal.setTime(createdAt1);
        int createdWeek = createdAtCal.get(Calendar.WEEK_OF_YEAR);
        int currentWeek = nowCal.get(Calendar.WEEK_OF_YEAR);
        currentWeek = ProgressServiceImpl.getCurrentWeek(nowCal, createdAtCal, currentWeek);
        return dayOfWeek == 1 ? currentWeek - createdWeek
            : currentWeek - createdWeek + 1;
    }

    private Long userLevelUp(Long kidAchievedChallenge) {

        if (1 <= kidAchievedChallenge && kidAchievedChallenge < 5) {
            return 2L;
        } else if (5 <= kidAchievedChallenge && kidAchievedChallenge < 10) {
            return 3L;
        } else if (10 <= kidAchievedChallenge && kidAchievedChallenge < 15) {
            return 4L;
        } else if (15 <= kidAchievedChallenge && kidAchievedChallenge < 20) {
            return -4L;
        } else if (20 <= kidAchievedChallenge) {
            return 5L;
        }
        throw new IllegalArgumentException();
    }

    @Transactional
    public void challengeCompleteDeleteByKid(User user, FamilyRequest familyRequest) {
        //ToDo: 부모는 가족나가면 어케되냐?
        List<ChallengeUser> challengeUserList = cuRepo.findByUserId(user.getId());
        List<Challenge> challengeList = challengeUserList.stream().map(ChallengeUser::getChallenge)
            .collect(
                Collectors.toList());
        long[] momRequest = new long[]{0L, 0L};
        long[] dadRequest = new long[]{0L, 0L};

        //challenge / progress / comment 한번에 삭제
        cuRepo.deleteAll(challengeUserList);
        challengeList.forEach(challenge -> {
            boolean isMom = challenge.getContractUser().getIsFemale();
            if (isMom) {
                momRequest[0] = momRequest[0] + 1;
            } else {
                dadRequest[0] = dadRequest[0] + 1;
            }
            if (challenge.getChallengeStatus() == rejected) {
                commentRepository.delete(challenge.getComment());
            } else if (challenge.getChallengeStatus() == achieved
                || challenge.getChallengeStatus() == walking
                || challenge.getChallengeStatus() == failed) {
                if (isMom) {
                    momRequest[1] = momRequest[1] + 1;
                } else {
                    dadRequest[1] = dadRequest[1] + 1;
                }
                progressRepository.deleteAll(challenge.getProgressList());
            }
            challengeRepository.delete(challenge);
        });
        Kid kid = user.getKid();
        kid.setSavings(0L);
        kid.setTotalChallenge(0L);
        kid.setAchievedChallenge(0L);
        kid.setLevel(1L);
        kidRepository.save(kid);
        Family family = familyRepository.findByCode(familyRequest.getCode())
            .orElseThrow(() -> new ForbiddenException(ErrorCode.FAMILY_NOT_EXISTS.getErrorCode()));
        List<FamilyUser> familyUserList = familyUserRepository.findByFamily(family);
        List<Parent> parentList = familyUserList.stream()
            .filter(familyUser -> !familyUser.getUser().getIsKid())
            .map(familyUser -> familyUser.getUser().getParent())
            .collect(Collectors.toList());
        parentList.forEach(parent -> {
            if (parent.getUser().getIsFemale()) {
                parent.setTotalRequest(parent.getTotalRequest() - momRequest[0]);
                parent.setAcceptedRequest(parent.getAcceptedRequest() - momRequest[1]);
            } else {
                parent.setTotalRequest(parent.getTotalRequest() - dadRequest[0]);
                parent.setAcceptedRequest(parent.getAcceptedRequest() - dadRequest[1]);
            }
            parentRepository.save(parent);
        });
    }

    @Transactional
    public void challengeCompleteDeleteByParent(User user, FamilyRequest familyRequest) {
        //ToDo: 부모는 가족나가면 어케되냐?
        List<Challenge> challengeList = challengeRepository.findByContractUserId(user.getId());
        challengeList.forEach(challenge -> {
            long kidSavings = 0L;
            long kidAchievedChallenge = 0L;
            long kidTotalChallenge = 0L;
            if (challenge.getChallengeStatus() != pending
                && challenge.getChallengeStatus() != rejected) {
                kidTotalChallenge = kidTotalChallenge + 1L;
                kidSavings =
                    kidSavings + challenge.getSuccessWeeks() * challenge.getWeekPrice();
                progressRepository.deleteAll(challenge.getProgressList());
            }
            if (challenge.getChallengeStatus() == achieved) {
                kidAchievedChallenge = kidAchievedChallenge + 1L;
            } else if (challenge.getChallengeStatus() == rejected) {
                commentRepository.delete(challenge.getComment());
            }
            ChallengeUser challengeUser = cuRepo.findByChallengeId(
                challenge.getId()).orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE_USER.getErrorCode()));
            Kid kid = challengeUser.getUser().getKid();
            kid.setTotalChallenge(kid.getTotalChallenge() - kidTotalChallenge);
            kid.setSavings(kid.getSavings() - kidSavings);
            kid.setAchievedChallenge(kid.getAchievedChallenge() - kidAchievedChallenge);
            kidRepository.save(kid);
            cuRepo.delete(challengeUser);
            challengeRepository.delete(challenge);
        });
        Parent parent = user.getParent();
        parent.setTotalRequest(0L);
        parent.setAcceptedRequest(0L);
        parentRepository.save(parent);
    }

    private void userLevelNotification(User authUser, Long kidAchievedChallenge) {

        if (kidAchievedChallenge == 4 || kidAchievedChallenge == 9 || kidAchievedChallenge == 14
            || kidAchievedChallenge == 19) {
            notificationController.userLevelUpMinusOne(authUser);
        } else if (kidAchievedChallenge == 3 || kidAchievedChallenge == 8
            || kidAchievedChallenge == 13 || kidAchievedChallenge == 15) {
            notificationController.userLevelUpHalf(authUser);
        }
    }
}

