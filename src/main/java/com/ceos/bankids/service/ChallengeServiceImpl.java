package com.ceos.bankids.service;

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
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.exception.NotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

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
            .filter(challengeUser -> challengeUser.getChallenge().getStatus() == 2
                && challengeUser.getChallenge().getIsAchieved() == 1).count();
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
            .isAchieved(1L)
            .status(1L).interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem).build();
        challengeRepository.save(newChallenge);

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(user).build();
        challengeUserRepository.save(newChallengeUser);

        Parent parent = contractUser.getParent();
        parent.setTotalRequest(contractUser.getParent().getTotalRequest() + 1);
        parentRepository.save(parent);

        return new ChallengeDTO(newChallenge, null, null);
    }

    // 돈길 상세 정보 API
    @Transactional
    @Override
    public ChallengeDTO detailChallenge(User user, Long challengeId) {
        Optional<ChallengeUser> challengeUserRow = challengeUserRepository.findByChallengeId(
            challengeId);
        if (challengeUserRow.isPresent()) {
            ChallengeUser challengeUser = challengeUserRow.get();
            if (!Objects.equals(challengeUser.getUser().getId(), user.getId())) {
                throw new ForbiddenException("권한이 없습니다.");
            }
            Challenge findChallenge = challengeUser.getChallenge();
            List<ProgressDTO> progressDTOList = new ArrayList<>();
            if (findChallenge.getStatus() == 2L) {
                List<Progress> progressList = findChallenge.getProgressList();
                progressList.forEach(
                    progress -> progressDTOList.add(new ProgressDTO(progress)));
                return new ChallengeDTO(findChallenge, progressDTOList, null);
            } else if (findChallenge.getStatus() == 0L) {
                return new ChallengeDTO(findChallenge, null, findChallenge.getComment());
            } else {
                return new ChallengeDTO(findChallenge, null, null);
            }
        } else {
            throw new NotFoundException("챌린지가 없습니다.");
        }
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
            } else if (deleteChallenge.getStatus() == 0) {
                if (deleteChallenge.getIsAchieved() == 0) {
                    kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                    List<Progress> failureProgressList = deleteChallenge.getProgressList();
                    progressRepository.deleteAll(failureProgressList);
                } else if (deleteChallenge.getIsAchieved() == 1) {
                    commentRepository.delete(deleteChallenge.getComment());
                }
                challengeUserRepository.delete(deleteChallengeUser);
                challengeRepository.delete(deleteChallenge);
                return new ChallengeDTO(deleteChallenge, null, null);
            } else if (kid.getDeleteChallenge() == null) {
                Long datetime = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(datetime);
                kid.setDeleteChallenge(timestamp);
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
                kidRepository.save(kid);
            } else if (!kid.getDeleteChallenge().equals(null)) {
                Timestamp deleteChallengeTimestamp = kid.getDeleteChallenge();
                Calendar deleteCal = Calendar.getInstance();
                deleteCal.setTime(deleteChallengeTimestamp);
                deleteCal.add(Calendar.DATE, 14);
                if (nowCal.getTime().getTime() <= deleteCal.getTime().getTime()
                    && deleteChallenge.getStatus() != 0) {
                    throw new ForbiddenException("돈길은 2주에 한번씩 삭제할 수 있습니다.");
                }
                Long datetime = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(datetime);
                kid.setDeleteChallenge(timestamp);
                kid.setTotalChallenge(kid.getTotalChallenge() - 1);
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
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        List<ChallengeUser> challengeUserRow = challengeUserRepository.findByUserId(
            user.getId());
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        for (ChallengeUser r : challengeUserRow) {
            System.out.println("r.getChallenge().getId() = " + r.getChallenge().getId());
            System.out.println("r.getChallenge().getStatus() = " + r.getChallenge().getStatus());
            System.out.println(
                "r.getChallenge().getIsAchieved() = " + r.getChallenge().getIsAchieved());
            if (status.equals("accept")) {
                if (r.getChallenge().getStatus() == 2L) {
                    List<ProgressDTO> progressDTOList = new ArrayList<>();
                    List<Progress> progressList = r.getChallenge().getProgressList();
                    Progress progress1 = progressList.stream().findFirst()
                        .orElseThrow(BadRequestException::new);
                    Timestamp createdAt1 = progress1.getCreatedAt();
                    Calendar createdAtCal = Calendar.getInstance();
                    createdAtCal.setTime(createdAt1);
                    Challenge challenge = r.getChallenge();
                    Long interestRate = challenge.getInterestRate();
                    Long risk = 0L;
                    Long falseCnt = 0L;
                    if (interestRate == 10L) {
                        risk = challenge.getWeeks();
                    } else if (interestRate == 20L) {
                        risk = 3L;
                    } else if (interestRate == 30L) {
                        risk = 1L;
                    }
                    for (Progress progress : progressList) {
                        if (createdAtCal.getTime().getTime() <= nowCal.getTime().getTime()) {
                            if (!progress.getIsAchieved()) {
                                falseCnt += 1;
                            }
                            progressDTOList.add(new ProgressDTO(progress));
                            createdAtCal.add(Calendar.DATE, 7);
                        }
                    }
                    if (falseCnt > risk) {
                        challenge.setIsAchieved(0L);
                        challenge.setStatus(0L);
                        challengeRepository.save(challenge);
                    }
                    challengeDTOList.add(new ChallengeDTO(r.getChallenge(), progressDTOList,
                        r.getChallenge().getComment()));
                } else if (r.getChallenge().getStatus() == 0
                    && r.getChallenge().getIsAchieved() == 0) {
                    List<Progress> progressList = r.getChallenge().getProgressList();
                    List<ProgressDTO> progressDTOList = new ArrayList<>();
                    Progress progress1 = progressList.stream().findFirst()
                        .orElseThrow(BadRequestException::new);
                    Timestamp createdAt1 = progress1.getCreatedAt();
                    Calendar createdAtCal = Calendar.getInstance();
                    createdAtCal.setTime(createdAt1);
                    Challenge challenge = r.getChallenge();
                    for (Progress progress : progressList) {
                        if (createdAtCal.getTime().getTime() <= nowCal.getTime().getTime()) {
                            progressDTOList.add(new ProgressDTO(progress));
                            createdAtCal.add(Calendar.DATE, 7);
                        }
                    }
                    challengeDTOList.add(
                        new ChallengeDTO(r.getChallenge(), progressDTOList, r.getChallenge()
                            .getComment()));
                }
            } else if ((status.equals("pending"))
                && r.getChallenge().getStatus() != 2L) {
                challengeDTOList.add(new ChallengeDTO(r.getChallenge(), null,
                    r.getChallenge().getComment()));
            }
        }
        return challengeDTOList;
    }

    // 자녀의 돈길 리스트 가져오기 API
    @Transactional
    @Override
    public List<KidChallengeListDTO> readKidChallenge(User user) {

        userRoleValidation(user, false);
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        Optional<FamilyUser> familyUser = familyUserRepository.findByUserId(user.getId());
        List<KidChallengeListDTO> kidChallengeListDTOList = new ArrayList<>();
        familyUser.ifPresent(c -> {
            List<FamilyUser> familyUserList = familyUserRepository.findByFamily(c.getFamily());
            familyUserList.forEach(familyUser1 -> {
                List<ChallengeDTO> challengeList = new ArrayList<>();
                if (familyUser1.getUser().getIsKid()) {
                    List<ChallengeUser> challengeUserList = challengeUserRepository.findByUserId(
                        familyUser1.getUser().getId());
                    challengeUserList.forEach(challengeUser -> {
                        List<ProgressDTO> progressDTOList = new ArrayList<>();
                        Long status = challengeUser.getChallenge().getStatus();
                        if (status == 2L) {
                            List<Progress> progressList = challengeUser.getChallenge()
                                .getProgressList();
                            Progress progress1 = progressList.get(0);
                            Timestamp createdAt = progress1.getCreatedAt();
                            Calendar createdAtCal = Calendar.getInstance();
                            createdAtCal.setTime(createdAt);
                            progressList
                                .forEach(progress -> {
                                    if (createdAtCal.getTime().getTime() <= nowCal.getTime()
                                        .getTime()) {
                                        progressDTOList.add(new ProgressDTO(progress));
                                    }
                                    createdAtCal.add(Calendar.DATE, 7);
                                });
                            challengeList.add(
                                new ChallengeDTO(challengeUser.getChallenge(), progressDTOList,
                                    challengeUser.getChallenge().getComment()));
                        } else {
                            challengeList.add(new ChallengeDTO(challengeUser.getChallenge(), null,
                                challengeUser.getChallenge()
                                    .getComment()));
                        }
                    });
                    kidChallengeListDTOList.add(
                        new KidChallengeListDTO(familyUser1.getUser(), challengeList));
                }
            });
        });

        return kidChallengeListDTOList;
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
                    || user.getId() != challenge.getContractUser().getId()) {
                    throw new ForbiddenException("권한이 없습니다.");
                }
            });
        });

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        if (challenge.getStatus() != 1L) {
            throw new BadRequestException("이미 승인 혹은 거절된 돈길입니다.");
        }
        if (kidChallengeRequest.getAccept()) {
            long count = challengeUserRepository.findByUserId(cUser.getId()).stream()
                .filter(challengeUser -> challengeUser.getChallenge().getStatus() == 2
                    && challengeUser.getChallenge().getIsAchieved() == 1).count();
            System.out.println("count = " + count);
            if (count >= 5) {
                throw new ForbiddenException("자녀가 돈길 생성 개수 제한에 도달했습니다.");
            }
            Kid kid = cUser.getKid();
            challenge.setStatus(2L);
            challengeRepository.save(challenge);
            kid.setTotalChallenge(kid.getTotalChallenge() + 1);
            kidRepository.save(kid);
            Parent parent = user.getParent();
            parent.setTotalChallenge(parent.getTotalChallenge() + 1);
            parent.setAcceptedRequest(parent.getAcceptedRequest() + 1);
            parentRepository.save(parent);
            for (int i = 1; i <= challenge.getWeeks(); i++) {
                Progress newProgress = Progress.builder().weeks(Long.valueOf(i))
                    .challenge(challenge)
                    .isAchieved(false).build();
                progressDTOList.add(new ProgressDTO(newProgress));
                progressRepository.save(newProgress);
            }
        } else {
            Comment newComment = Comment.builder().challenge(challenge).content(
                kidChallengeRequest.getComment()).user(user).build();
            challenge.setStatus(0L);
            commentRepository.save(newComment);
            challenge.setComment(newComment);
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
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        Long[] currentPrice = {0L};
        Long[] totalPrice = {0L};
        List<ChallengeUser> challengeUserList = challengeUserRepository.findByUserId(
            user.getId());
        challengeUserList.forEach(challengeUser -> {
            Challenge challenge = challengeUser.getChallenge();
            if (challenge.getStatus() == 2 && challenge.getIsAchieved() == 1) {
                List<Progress> progressList = challenge.getProgressList();
                Progress progress1 = progressList.stream().findFirst()
                    .orElseThrow(BadRequestException::new);
                Timestamp createdAt = progress1.getCreatedAt();
                Calendar createdAtCal = Calendar.getInstance();
                createdAtCal.setTime(createdAt);
                long diffSec = (nowCal.getTimeInMillis() - createdAtCal.getTimeInMillis()) / 1000;
                long diffWeeks = diffSec / (24 * 60 * 60 * 7) + 1;
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
    public WeekDTO readKidWeekInfo(User user, String kidName) {

        userRoleValidation(user, false);
        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException("유저의 가족이 없습니다."));
        Family family = familyUser.getFamily();
        User kid = familyUserRepository.findByFamily(family).stream()
            .map(FamilyUser::getUser)
            .filter(fUser -> Objects.equals(fUser.getUsername(), kidName) && fUser.getIsKid())
            .findFirst()
            .orElseThrow(() -> new BadRequestException("해당 자식이 존재하지 않습니다."));

        return readWeekInfo(kid);
    }

    public void userRoleValidation(User user, Boolean approveRole) {
        if (user.getIsKid() != approveRole) {
            throw new ForbiddenException("접근 불가능한 API 입니다.");
        }
    }

    public void sundayValidation() {
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

}

