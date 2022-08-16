package com.ceos.bankids.service;

import com.ceos.bankids.Enum.ChallengeStatus;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ProgressRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
public class ProgressServiceImpl implements ProgressService {

    // Enum ChallengeStatus
    private static final ChallengeStatus pending = ChallengeStatus.PENDING;
    private static final ChallengeStatus walking = ChallengeStatus.WALKING;
    private static final ChallengeStatus achieved = ChallengeStatus.ACHIEVED;
    private static final ChallengeStatus failed = ChallengeStatus.FAILED;
    private static final ChallengeStatus rejected = ChallengeStatus.REJECTED;
    private final ProgressRepository progressRepository;
    private final ChallengeUserRepository challengeUserRepository;
    private final ChallengeRepository challengeRepository;
    private final KidRepository kidRepository;

    static int getCurrentWeek(Calendar nowCal, Calendar createdAtCal, int currentWeek) {
        if (nowCal.get(Calendar.YEAR) != createdAtCal.get(Calendar.YEAR)) {
            System.out.println("통과");
            int diffYears = nowCal.get(Calendar.YEAR) - createdAtCal.get(Calendar.YEAR);
            System.out.println("diffYears = " + diffYears);
            currentWeek =
                diffYears * createdAtCal.getActualMaximum(Calendar.WEEK_OF_YEAR) + currentWeek;
            System.out.println("바뀐 currentWeek = " + currentWeek);
        }
        return currentWeek;
    }

    //돈길 걷기 API
    @Transactional
    @Override
    public ProgressDTO updateProgress(User user, Long challengeId) {

        userRoleValidation(user, true);
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(BadRequestException::new);
        Optional<ChallengeUser> challengeUser = challengeUserRepository.findByChallengeId(
            challengeId);
        challengeUser.ifPresent(c -> {
            if (!Objects.equals(c.getUser().getId(), user.getId())) {
                throw new ForbiddenException("해당 유저는 해당 돈길에 접근 할 수 없습니다.");
            }
        });
        if (challenge.getChallengeStatus() != walking) {
            throw new BadRequestException("걷고있는 돈길이 아닙니다.");
        }
        Kid kid = user.getKid();
        Long diffWeeks = (long) timeLogic(challenge.getProgressList());
        Progress progress = progressRepository.findByChallengeIdAndWeeks(challengeId, diffWeeks)
            .orElseThrow(BadRequestException::new);
        if (progress.getIsAchieved()) {
            throw new BadRequestException("이번주는 이미 저축했습니다.");
        }
        if (diffWeeks > challenge.getWeeks()) {
            throw new BadRequestException("걸을 수 있는 돈길이 없습니다.");
        } else if (diffWeeks.equals(challenge.getWeeks())) {
            Long userLevel = userLevelUp(kid.getAchievedChallenge() + 1);
            challenge.setChallengeStatus(achieved);
            kid.setAchievedChallenge(kid.getAchievedChallenge() + 1);
            if (!Objects.equals(userLevel, kid.getLevel())) {
                kid.setLevel(userLevel);
            }
        }

        progress.setIsAchieved(true);
        challenge.setSuccessWeeks(challenge.getSuccessWeeks() + 1);
        kid.setSavings(kid.getSavings() + challenge.getWeekPrice());
        challengeRepository.save(challenge);
        kidRepository.save(kid);
        progressRepository.save(progress);

        return new ProgressDTO(progress);
    }

    public void userRoleValidation(User user, Boolean approveRole) {
        if (user.getIsKid() != approveRole) {
            throw new ForbiddenException("접근 불가능한 API 입니다.");
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
        currentWeek = getCurrentWeek(nowCal, createdAtCal, currentWeek);
        System.out.println("currentWeek = " + currentWeek);
        System.out.println("createdWeek = " + createdWeek);
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
