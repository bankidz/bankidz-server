package com.ceos.bankids.service;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.NotificationController;
import com.ceos.bankids.controller.request.ChallengeRequest;
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
import com.ceos.bankids.dto.KidAchievedChallengeListDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
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
import java.time.DayOfWeek;
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
    private final ChallengeUserRepository challengeUserRepository;
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
    public ChallengeDTO createChallenge(User user, ChallengeRequest challengeRequest) {

        sundayValidation();
        userRoleValidation(user, true);
        long count = challengeUserRepository.findByUserId(user.getId()).stream()
            .filter(challengeUser -> challengeUser.getChallenge().getChallengeStatus()
                == walking).count();
        if (count >= 5) {
            throw new ForbiddenException(ErrorCode.CHALLENGE_COUNT_OVER_FIVE.getErrorCode());
        }
        Boolean isMom = challengeRequest.getIsMom();
        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ForbiddenException(
                ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
        User contractUser = familyUserRepository.findByFamily(familyUser.getFamily())
            .stream()
            .filter(f -> !f.getUser().getIsKid() && f.getUser().getIsFemale() == isMom).findFirst()
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CONSTRUCT_USER.getErrorCode()))
            .getUser();

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
            .contractUser(contractUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        challengeRepository.save(newChallenge);

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(user).build();
        challengeUserRepository.save(newChallengeUser);

        // 자녀가 제안한 총 돈길
        Parent parent = contractUser.getParent();
        parent.setTotalRequest(contractUser.getParent().getTotalRequest() + 1);
        parentRepository.save(parent);

        notificationController.createPendingChallengeNotification(contractUser, newChallengeUser);

        return new ChallengeDTO(newChallenge, null, null);
    }

    // 돈길 삭제 API (2주에 한번)
    @Transactional
    @Override
    public ChallengeDTO deleteChallenge(User user, Long challengeId) {

        sundayValidation();
        userRoleValidation(user, true);
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        Optional<ChallengeUser> deleteChallengeUserRow = challengeUserRepository.findByChallengeId(
            challengeId);
        if (deleteChallengeUserRow.isPresent()) {
            ChallengeUser deleteChallengeUser = deleteChallengeUserRow.get();
            Challenge deleteChallenge = deleteChallengeUser.getChallenge();
            Kid kid = deleteChallengeUser.getUser().getKid();
            if (!Objects.equals(deleteChallengeUser.getUser().getId(), user.getId())) {
                throw new ForbiddenException(ErrorCode.NOT_MATCH_CHALLENGE_USER.getErrorCode());
            } else if (deleteChallenge.getChallengeStatus()
                == failed) {
                List<Progress> failureProgressList = deleteChallenge.getProgressList();
                progressRepository.deleteAll(failureProgressList);
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
                return new ChallengeDTO(deleteChallenge, null, null);
            } else if (deleteChallenge.getChallengeStatus() == rejected) {
                commentRepository.delete(deleteChallenge.getComment());
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
                return new ChallengeDTO(deleteChallenge, null, null);
            } else if (deleteChallenge.getChallengeStatus() == pending) {
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
                return new ChallengeDTO(deleteChallenge, null, null);
            } else if (kid.getDeleteChallenge() == null) {
                long datetime = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(datetime);
                kid.setDeleteChallenge(timestamp);
            } else if (deleteChallenge.getChallengeStatus() == walking && !kid.getDeleteChallenge()
                .equals(null)) {
                Timestamp deleteChallengeTimestamp = kid.getDeleteChallenge();
                Calendar deleteCal = Calendar.getInstance();
                deleteCal.setTime(deleteChallengeTimestamp);
                int lastDeleteWeek = deleteCal.get(Calendar.WEEK_OF_YEAR);
                int currentWeek = nowCal.get(Calendar.WEEK_OF_YEAR);
                int diffYears = nowCal.get(Calendar.YEAR) - deleteCal.get(Calendar.YEAR);
                int l = deleteCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? lastDeleteWeek - 1
                    : lastDeleteWeek;
                int c = nowCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? currentWeek - 1
                    : currentWeek;
                if (diffYears == 0 && l + 2 > c) {
                    throw new ForbiddenException(ErrorCode.NOT_TWO_WEEKS_YET.getErrorCode());
                } else if (diffYears > 0) {
                    int newC = diffYears * deleteCal.getActualMaximum(Calendar.WEEK_OF_YEAR) + c;
                    if (l + 2 > newC) {
                        throw new ForbiddenException(ErrorCode.NOT_TWO_WEEKS_YET.getErrorCode());
                    }
                }
                long datetime = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(datetime);
                kid.setDeleteChallenge(timestamp);
                kidRepository.save(kid);
            }
            List<Progress> progressList = deleteChallenge.getProgressList();
            progressRepository.deleteAll(progressList);
            challengeUserRepository.delete(deleteChallengeUser);
            challengeRepository.delete(deleteChallenge);

            return new ChallengeDTO(deleteChallenge, null, null);
        } else {
            throw new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE.getErrorCode());
        }
    }

    // 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public List<ChallengeDTO> readChallenge(User user, String status) {

        userRoleValidation(user, true);
        if (!Objects.equals(status, "walking") && !Objects.equals(status, "pending")) {
            throw new BadRequestException(ErrorCode.INVALID_QUERYPARAM.getErrorCode());
        }
        List<ChallengeUser> challengeUserRow = challengeUserRepository.findByUserId(
            user.getId());
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        for (ChallengeUser r : challengeUserRow) {
            if (status.equals("walking")) {
                if (r.getChallenge().getChallengeStatus() == walking) {
                    List<ProgressDTO> progressDTOList = new ArrayList<>();
                    List<Progress> progressList = r.getChallenge().getProgressList();
                    Kid kid = user.getKid();
                    Long diffWeeks =
                        timeLogic(progressList) > r.getChallenge().getWeeks() ? r.getChallenge()
                            .getWeeks() + 1 : (long) timeLogic(progressList);
                    Challenge challenge = r.getChallenge();
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
                        notificationController.challengeFailedNotification(
                            challenge.getContractUser(), r);
                    } else if (diffWeeks > challenge.getWeeks()) {
                        challenge.setChallengeStatus(achieved);
                        Long userLevel = userLevelUp(kid.getAchievedChallenge() + 1);
                        kid.setAchievedChallenge(kid.getAchievedChallenge() + 1);
                        if (!Objects.equals(userLevel, kid.getLevel())) {
                            notificationController.kidLevelUpNotification(
                                challenge.getContractUser(), user, kid.getLevel(), userLevel);
                            kid.setLevel(userLevel);
                        }
                        challengeRepository.save(challenge);
                        kidRepository.save(kid);
                        notificationController.achieveChallengeNotification(
                            challenge.getContractUser(), r);
                    }
                    if (challenge.getChallengeStatus() != achieved) {
                        challengeDTOList.add(new ChallengeDTO(r.getChallenge(), progressDTOList,
                            r.getChallenge().getComment()));
                    }
                } else if (r.getChallenge().getChallengeStatus() == failed) {
                    List<Progress> progressList = r.getChallenge().getProgressList();
                    List<ProgressDTO> progressDTOList = new ArrayList<>();
                    Long diffWeeks =
                        timeLogic(progressList) > r.getChallenge().getWeeks() ? r.getChallenge()
                            .getWeeks() : (long) timeLogic(progressList);
                    for (Progress progress : progressList) {
                        if (progress.getWeeks() <= diffWeeks) {
                            progressDTOList.add(new ProgressDTO(progress, r.getChallenge()));
                        }
                    }
                    challengeDTOList.add(
                        new ChallengeDTO(r.getChallenge(), progressDTOList, r.getChallenge()
                            .getComment()));
                }
            } else if ((status.equals("pending"))
                && (r.getChallenge().getChallengeStatus() == pending
                || r.getChallenge().getChallengeStatus() == rejected)) {
                challengeDTOList.add(new ChallengeDTO(r.getChallenge(), null,
                    r.getChallenge().getComment()));
            }
        }
        return challengeDTOList;
    }

    // 자녀의 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public KidChallengeListDTO readKidChallenge(User user, Long kidId, String status) {

        userRoleValidation(user, false);
        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ForbiddenException(ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
        Family family = familyUser.getFamily();
        User kid = familyUserRepository.findByFamily(family).stream()
            .filter(f -> f.getUser().getIsKid() && Objects.equals(
                f.getUser().getKid().getId(), kidId)).map(FamilyUser::getUser).findFirst()
            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_KID.getErrorCode()));
        List<ChallengeDTO> challengeDTOList = readChallenge(kid, status);
        if (Objects.equals(status, "pending")) {
            List<ChallengeDTO> resultList = challengeDTOList.stream()
                .filter(challengeDTO -> challengeDTO.getIsMom() == user.getIsFemale()).collect(
                    Collectors.toList());
            return new KidChallengeListDTO(kid, resultList);
        }
        return new KidChallengeListDTO(kid, challengeDTOList);
    }

    // 돈길 수락 / 거절 API
    @Transactional
    @Override
    public ChallengeDTO updateChallengeStatus(User user, Long challengeId,
        KidChallengeRequest kidChallengeRequest) {

        sundayValidation();
        userRoleValidation(user, false);
        ChallengeUser findChallengeUser = challengeUserRepository.findByChallengeId(challengeId)
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
            long count = challengeUserRepository.findByUserId(cUser.getId()).stream()
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
        notificationController.notification(challenge, user);
        return new ChallengeDTO(challenge, progressDTOList, challenge.getComment());
    }

    // 주차 정보 가져오기 API
    @Transactional
    @Override
    public WeekDTO readWeekInfo(User user) {

        userRoleValidation(user, true);
        Long[] currentPrice = {0L};
        Long[] totalPrice = {0L};
        List<ChallengeUser> challengeUserList = challengeUserRepository.findByUserId(
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

        userRoleValidation(user, false);
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

        List<Challenge> challengeList = challengeUserRepository.findByUserId(user.getId()).stream()
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
        challenge.setIsInterestPayment(true);
        challengeRepository.save(challenge);

        return new AchievedChallengeDTO(challenge);
    }

    //자녀의 완주한 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public KidAchievedChallengeListDTO readKidAchievedChallenge(User user, Long kidId,
        String interestPayment) {

        userRoleValidation(user, false);
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

        return new KidAchievedChallengeListDTO(kidId, achievedChallengeListDTO);
    }

    private void userRoleValidation(User user, Boolean approveRole) {
        if (user.getIsKid() != approveRole) {
            throw new ForbiddenException(ErrorCode.USER_ROLE_ERROR.getErrorCode());
        }
    }

    private void sundayValidation() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int value = dayOfWeek.getValue();
        if (value == 8) {       // test환경에선 접근이 안되는 8로 실환경에선 일요일인 7로 설정
            throw new ForbiddenException(ErrorCode.SUNDAY_ERROR.getErrorCode());
        }
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
        userRoleValidation(user, true);
        List<ChallengeUser> challengeUserList = challengeUserRepository.findByUserId(user.getId());
        List<Challenge> challengeList = challengeUserList.stream().map(ChallengeUser::getChallenge)
            .collect(
                Collectors.toList());
        long[] momRequest = new long[]{0L, 0L};
        long[] dadRequest = new long[]{0L, 0L};

        //challenge / progress / comment 한번에 삭제
        challengeUserRepository.deleteAll(challengeUserList);
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
        kid.setLevel(0L);
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
        userRoleValidation(user, false);
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
            ChallengeUser challengeUser = challengeUserRepository.findByChallengeId(
                challenge.getId()).orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE_USER.getErrorCode()));
            Kid kid = challengeUser.getUser().getKid();
            kid.setTotalChallenge(kid.getTotalChallenge() - kidTotalChallenge);
            kid.setSavings(kid.getSavings() - kidSavings);
            kid.setAchievedChallenge(kid.getAchievedChallenge() - kidAchievedChallenge);
            kidRepository.save(kid);
            challengeUserRepository.delete(challengeUser);
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

