package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ProgressRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.ProgressRepository;
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

    @Transactional
    @Override
    public ProgressDTO updateProgress(User user, Long challengeId,
        ProgressRequest progressRequest) {

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
                FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
                    .orElseThrow(BadRequestException::new);
                Family family = familyUser.getFamily();
                familyUserRepository.findByFamily(family).forEach(familyUser1 -> {
                    if (familyUser1.getUser() == user) {
                        Long savings = familyUser1.getUser().getKid().getSavings();
                        Challenge challenge = challengeRepository.findById(challengeId)
                            .orElseThrow(BadRequestException::new);
                        Long weekPrice = challenge.getWeekPrice();
                        familyUser1.getUser().getKid().setSavings(savings + weekPrice);
                    } else if (!familyUser1.getUser().getIsKid()) {
                        Long savings = familyUser1.getUser().getParent().getSavings();
                        Challenge challenge = challengeRepository.findById(challengeId)
                            .orElseThrow(BadRequestException::new);
                        Long weekPrice = challenge.getWeekPrice();
                        familyUser1.getUser().getParent().setSavings(savings + weekPrice);
                    }
                });
                p.setIsAchieved(true);
                progressRepository.save(p);
            });
            return new ProgressDTO(progress.get());
        } else {
            throw new BadRequestException("존재하지 않는 프로그레스 입니다.");
        }
    }
}
