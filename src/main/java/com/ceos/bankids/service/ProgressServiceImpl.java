package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ProgressRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
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

    private final ProgressRepository progressRepository;
    private final ChallengeUserRepository challengeUserRepository;
    private final ChallengeRepository challengeRepository;
    private final FamilyUserRepository familyUserRepository;
    private final KidRepository kidRepository;
    private final ParentRepository parentRepository;

    // 돈길 걷기 API
    @Transactional
    @Override
    public ProgressDTO updateProgress(User user, Long challengeId,
        ProgressRequest progressRequest) {

        userRoleValidation(user, true);
        Long weeks = progressRequest.getWeeks();
        Optional<Progress> progress = progressRepository.findByChallengeIdAndWeeks(
            challengeId, weeks);
        Optional<ChallengeUser> challengeUser = challengeUserRepository.findByChallengeId(
            challengeId);
        challengeUser.ifPresent(c -> {
            if (!c.getUser().equals(user)) {
                throw new ForbiddenException("접근 할 수 없는 돈길입니다.");
            }
        });
        if (progress.isPresent()) {
            progress.ifPresent(p -> {
                if (p.getIsAchieved()) {
                    throw new BadRequestException("이번주는 이미 저축했습니다.");
                }
                FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
                    .orElseThrow(BadRequestException::new);
                Family family = familyUser.getFamily();
                Challenge challenge = challengeRepository.findById(challengeId)
                    .orElseThrow(BadRequestException::new);
                familyUserRepository.findByFamily(family).forEach(familyUser1 -> {
                    if (!familyUser1.getUser().getIsKid()) {
                        Long savings = familyUser1.getUser().getParent().getSavings();
                        Long weekPrice = challenge.getWeekPrice();
                        Parent parent = familyUser1.getUser().getParent();
                        parent.setSavings(savings + weekPrice);
                        parentRepository.save(parent);
                    } else if (Objects.equals(familyUser1.getUser().getId(), user.getId())) {
                        Long savings = familyUser1.getUser().getKid().getSavings();
                        Long weekPrice = challenge.getWeekPrice();
                        Kid kid = familyUser1.getUser().getKid();
                        kid.setSavings(savings + weekPrice);
                        kidRepository.save(kid);
                    }
                });
                p.setIsAchieved(true);
                progressRepository.save(p);
                challenge.setSuccessWeeks(challenge.getSuccessWeeks() + 1);
                if (Objects.equals(weeks, challenge.getWeeks())) {
                    challenge.setStatus(0L);
                    challenge.setIsAchieved(2L);
                    Kid kid = user.getKid();
                    long interestAmount =
                        (challenge.getTotalPrice() * challenge.getInterestRate() / 10
                            * challenge.getWeeks()) * challenge.getSuccessWeeks();
                    kid.setSavings(kid.getSavings() + challenge.getTotalPrice() + interestAmount);
                    kidRepository.save(kid);
                }
                challengeRepository.save(challenge);
            });
            return new ProgressDTO(progress.get());
        } else {
            throw new BadRequestException("존재하지 않는 프로그레스 입니다.");
        }
    }

    public void userRoleValidation(User user, Boolean approveRole) {
        if (user.getIsKid() != approveRole) {
            throw new ForbiddenException("접근 불가능한 API 입니다.");
        }
    }
}
