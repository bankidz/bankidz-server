package com.ceos.bankids.service;

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
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final ChallengeUserRepository challengeUserRepository;
    private final ChallengeRepository challengeRepository;
    private final FamilyUserRepository familyUserRepository;
    private final KidRepository kidRepository;
    private final ParentRepository parentRepository;

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
            if (c.getUser().getId() != user.getId()) {
                throw new ForbiddenException("해당 유저는 해당 돈길에 접근 할 수 없습니다.");
            }
        });
        if (challenge.getStatus() != 2 || challenge.getIsAchieved() != 1) {
            throw new BadRequestException("걷고있는 돈길이 아닙니다.");
        }

        Kid kid = user.getKid();
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        List<Progress> progressList = challenge.getProgressList();
        Timestamp createdAt = progressList.stream().findFirst()
            .orElseThrow(BadRequestException::new).getCreatedAt();
        Calendar createdAtCal = Calendar.getInstance();
        createdAtCal.setTime(createdAt);

        int diffWeeks =
            nowCal.get(Calendar.WEEK_OF_YEAR) - createdAtCal.get(Calendar.WEEK_OF_YEAR) + 1;

        if (diffWeeks > challenge.getWeeks()) {
            throw new BadRequestException("돈길 주차 정보를 확인해 주세요");
        } else if (diffWeeks == challenge.getWeeks()) {
            challenge.setStatus(0L);
            challenge.setIsAchieved(2L);
            long interestAmount =
                (challenge.getTotalPrice() * challenge.getInterestRate() / (100
                    * challenge.getWeeks()) * (challenge.getSuccessWeeks() + 1));
            kid.setSavings(kid.getSavings() + challenge.getTotalPrice() + interestAmount);
        }

        Progress progress = progressRepository.findByChallengeIdAndWeeks(challengeId,
                (long) diffWeeks)
            .orElseThrow(BadRequestException::new);
        if (progress.getIsAchieved()) {
            throw new BadRequestException("이번주는 이미 저축했습니다.");
        }
        progress.setIsAchieved(true);
        challenge.setSuccessWeeks(challenge.getSuccessWeeks() + 1);
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
}
