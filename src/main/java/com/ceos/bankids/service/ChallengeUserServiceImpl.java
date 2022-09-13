package com.ceos.bankids.service;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeUserServiceImpl implements ChallengeUserService {

    private final ChallengeUserRepository cuRepo;

    @Transactional
    @Override
    public ChallengeUser postChallengeUser(User user, Challenge challenge) {
        ChallengeUser challengeUser = ChallengeUser.builder().user(user).challenge(challenge)
            .member("parent")
            .build();
        cuRepo.save(challengeUser);
        return challengeUser;
    }

    public void checkMaxChallengeCount(User user) {
        List<Challenge> walkingChallengeList = cuRepo.findByUserId(user.getId()).stream()
            .map(ChallengeUser::getChallenge)
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.WALKING).collect(
                Collectors.toList());
        if (walkingChallengeList.size() >= 5) {
            throw new ForbiddenException(ErrorCode.CHALLENGE_COUNT_OVER_FIVE.getErrorCode());
        }
    }
}
