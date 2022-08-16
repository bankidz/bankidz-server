package com.ceos.bankids.service;

import com.ceos.bankids.Enum.ChallengeStatus;
import com.ceos.bankids.controller.request.ChallengeRequest;
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
import com.ceos.bankids.dto.ChallengeDTO;
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
            throw new ForbiddenException("돈길 생성 개수 제한에 도달했습니다.");
        }
        Boolean isMom = challengeRequest.getIsMom();
        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ForbiddenException("가족이 없는 유저는 돈길을 생성 할 수 없습니다."));
        User contractUser = familyUserRepository.findByFamily(familyUser.getFamily())
            .stream()
            .filter(f -> !f.getUser().getIsKid() && f.getUser().getIsFemale() == isMom).findFirst()
            .orElseThrow(() -> new BadRequestException("해당 부모가 없습니다.")).getUser();

        String category = challengeRequest.getCategory();
        String name = challengeRequest.getItemName();
        ChallengeCategory challengeCategory = challengeCategoryRepository.findByCategory(category);
        TargetItem targetItem = targetItemRepository.findByName(name);

        if (targetItem == null) {
            throw new BadRequestException("목표 아이템 입력이 잘 못 되었습니다.");
        }
        if (challengeCategory == null) {
            throw new BadRequestException("카테고리 입력이 잘 못 되었습니다.");
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
                throw new ForbiddenException("권한이 없습니다.");
            } else if (deleteChallenge.getChallengeStatus()
                == failed) {        // Todo: 부모 측 컬럼 조건 확실히 한 다음 추가
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                List<Progress> failureProgressList = deleteChallenge.getProgressList();
                progressRepository.deleteAll(failureProgressList);
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                kidRepository.save(kid);
                return new ChallengeDTO(deleteChallenge, null, null);
            } else if (deleteChallenge.getChallengeStatus() == rejected) {
                commentRepository.delete(deleteChallenge.getComment());
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                kidRepository.save(kid);
                return new ChallengeDTO(deleteChallenge, null, null);
            } else if (deleteChallenge.getChallengeStatus() == pending) {
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                kidRepository.save(kid);
                return new ChallengeDTO(deleteChallenge, null, null);
            } else if (kid.getDeleteChallenge() == null) {
                long datetime = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(datetime);
                kid.setDeleteChallenge(timestamp);
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                kidRepository.save(kid);
            } else if (deleteChallenge.getChallengeStatus() == walking && !kid.getDeleteChallenge()
                .equals(null)) {
                Timestamp deleteChallengeTimestamp = kid.getDeleteChallenge();
                System.out.println("deleteChallengeTimestamp = " + deleteChallengeTimestamp);
                Calendar deleteCal = Calendar.getInstance();
                deleteCal.setTime(deleteChallengeTimestamp);
                int lastDeleteWeek = deleteCal.get(Calendar.WEEK_OF_YEAR);
                int currentWeek = nowCal.get(Calendar.WEEK_OF_YEAR);
                int diffYears = nowCal.get(Calendar.YEAR) - deleteCal.get(Calendar.YEAR);
                int l = deleteCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? lastDeleteWeek - 1
                    : lastDeleteWeek;
                int c = nowCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? currentWeek - 1
                    : currentWeek;
                if (diffYears == 0 && l + 2 >= c) {
                    throw new ForbiddenException("돈길은 2주에 한번씩 삭제할 수 있습니다.");
                } else if (diffYears > 0) {
                    int newC = diffYears * deleteCal.getActualMaximum(Calendar.WEEK_OF_YEAR) + c;
                    if (l + 2 >= newC) {
                        throw new ForbiddenException("돈길은 2주에 한번씩 삭제할 수 있습니다.");
                    }
                }
                long datetime = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(datetime);
                kid.setDeleteChallenge(timestamp);
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                kid.setSavings(kid.getSavings()
                    - deleteChallenge.getSuccessWeeks() * deleteChallenge.getWeekPrice());
                kidRepository.save(kid);
            }
            List<Progress> progressList = deleteChallenge.getProgressList();
            progressRepository.deleteAll(progressList);
            challengeUserRepository.delete(deleteChallengeUser);
            challengeRepository.delete(deleteChallenge);

            return new ChallengeDTO(deleteChallenge, null, null);
        } else {
            throw new BadRequestException("챌린지가 없습니다.");
        }
    }

    // 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public List<ChallengeDTO> readChallenge(User user, String status) {

        userRoleValidation(user, true);
        if (!Objects.equals(status, "walking") && !Objects.equals(status, "pending")) {
            throw new BadRequestException("status 값을 확인해주세요");
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
                        risk = challenge.getWeeks();
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
                            progressDTOList.add(new ProgressDTO(progress));
                        }
                    }
                    if (falseCnt >= risk) {
                        challenge.setChallengeStatus(failed);
                        challengeRepository.save(challenge);
                    } else if (diffWeeks > challenge.getWeeks()) {
                        challenge.setChallengeStatus(achieved);
                        Long userLevel = userLevelUp(kid.getAchievedChallenge() + 1);
                        kid.setAchievedChallenge(kid.getAchievedChallenge() + 1);
                        if (!Objects.equals(userLevel, kid.getLevel())) {
                            kid.setLevel(userLevel);
                        }
                        challengeRepository.save(challenge);
                        kidRepository.save(kid);
                    }
                    challengeDTOList.add(new ChallengeDTO(r.getChallenge(), progressDTOList,
                        r.getChallenge().getComment()));
                } else if (r.getChallenge().getChallengeStatus() == failed) {
                    List<Progress> progressList = r.getChallenge().getProgressList();
                    List<ProgressDTO> progressDTOList = new ArrayList<>();
                    Long diffWeeks =
                        timeLogic(progressList) > r.getChallenge().getWeeks() ? r.getChallenge()
                            .getWeeks() : (long) timeLogic(progressList);
                    for (Progress progress : progressList) {
                        if (progress.getWeeks() <= diffWeeks) {
                            progressDTOList.add(new ProgressDTO(progress));
                        }
                    }
                    challengeDTOList.add(
                        new ChallengeDTO(r.getChallenge(), progressDTOList, r.getChallenge()
                            .getComment()));
                } else if (r.getChallenge().getChallengeStatus() == achieved) {
                    List<Progress> progressList = r.getChallenge().getProgressList();
                    List<ProgressDTO> progressDTOList = progressList.stream()
                        .map(ProgressDTO::new).collect(
                            Collectors.toList());
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
            .orElseThrow(BadRequestException::new);
        Family family = familyUser.getFamily();
        User kid = familyUserRepository.findByFamily(family).stream()
            .filter(f -> f.getUser().getIsKid() && Objects.equals(
                f.getUser().getKid().getId(), kidId)).map(FamilyUser::getUser).findFirst()
            .orElseThrow(BadRequestException::new);
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
            .orElseThrow(() -> new BadRequestException("존재하지 않는 돈길입니다."));
        User cUser = findChallengeUser.getUser();
        Optional<FamilyUser> familyUser = familyUserRepository.findByUserId(cUser.getId());
        Optional<FamilyUser> familyUser1 = familyUserRepository.findByUserId(user.getId());
        Challenge challenge = findChallengeUser.getChallenge();

        familyUser.ifPresent(f -> {
            familyUser1.ifPresent(f1 -> {
                if (f.getFamily() != f1.getFamily()
                    || !Objects.equals(user.getId(), challenge.getContractUser().getId())) {
                    throw new ForbiddenException("권한이 없습니다.");
                }
            });
        });

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        if (challenge.getChallengeStatus() != pending) {
            throw new BadRequestException("이미 승인 혹은 거절된 돈길입니다.");
        }
        if (kidChallengeRequest.getAccept()) {
            long count = challengeUserRepository.findByUserId(cUser.getId()).stream()
                .filter(
                    challengeUser -> challengeUser.getChallenge().getChallengeStatus() == walking)
                .count();
            if (count >= 5) {
                throw new ForbiddenException("자녀가 돈길 생성 개수 제한에 도달했습니다.");
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
                progressDTOList.add(new ProgressDTO(newProgress));
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
            .orElseThrow(() -> new BadRequestException("유저의 가족이 없습니다."));
        Family family = familyUser.getFamily();
        User kid = familyUserRepository.findByFamily(family).stream()
            .map(FamilyUser::getUser)
            .filter(fUser -> fUser.getIsKid() && Objects.equals(fUser.getKid().getId(), kidId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("해당 자식이 존재하지 않습니다."));

        WeekDTO weekDTO = readWeekInfo(kid);

        return new KidWeekDTO(kid.getKid(), weekDTO);
    }

    private void userRoleValidation(User user, Boolean approveRole) {
        if (user.getIsKid() != approveRole) {
            throw new ForbiddenException("접근 불가능한 API 입니다.");
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
            throw new ForbiddenException("일요일에는 접근 불가능한 API 입니다.");
        }
    }

    private int timeLogic(List<Progress> progressList) {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        int dayOfWeek = nowCal.get(Calendar.DAY_OF_WEEK);
        Progress progress1 = progressList.stream().findFirst()
            .orElseThrow(BadRequestException::new);
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
}

