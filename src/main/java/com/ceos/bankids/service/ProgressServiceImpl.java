package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ProgressRequest;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.ProgressRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final ChallengeUserRepository challengeUserRepository;

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
                p.setIsAchieved(true);
                progressRepository.save(p);
            });
            return new ProgressDTO(progress.get());
        } else {
            throw new BadRequestException("존재하지 않는 프로그레스 입니다.");
        }
    }
}
