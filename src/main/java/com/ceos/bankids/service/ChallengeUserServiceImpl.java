package com.ceos.bankids.service;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeUserRepository;
import java.util.List;
import java.util.Objects;
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

    @Transactional(readOnly = true)
    @Override
    public ChallengeUser getChallengeUser(User authUser, Long challengeId) {
        ChallengeUser challengeUser = cuRepo.findByChallengeId(challengeId)
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE_USER.getErrorCode()));
        if (challengeUser.getUser().getId() != authUser.getId()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_CHALLENGE_USER.getErrorCode());
        }
        return challengeUser;
    }

    @Override
    public void deleteChallengeUser(User authUser, Long challengeId) {
        ChallengeUser deleteChallengeUser = cuRepo.findByChallengeId(challengeId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE_USER.getErrorCode()));
        if (deleteChallengeUser.getUser().getId() != authUser.getId()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_CHALLENGE_USER.getErrorCode());
        }

        cuRepo.delete(deleteChallengeUser);
    }

    @Override
    public List<Challenge> getChallengeUserList(User authUser, String status) {
        if (Objects.equals(status, "pending")) {
            return cuRepo.findByUserId(authUser.getId()).stream()
                .map(ChallengeUser::getChallenge)
                .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.PENDING
                    || challenge.getChallengeStatus() == ChallengeStatus.REJECTED).collect(
                    Collectors.toList());
        } else {
            return cuRepo.findByUserId(authUser.getId()).stream()
                .map(ChallengeUser::getChallenge)
                .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.WALKING
                    || challenge.getChallengeStatus() == ChallengeStatus.FAILED).collect(
                    Collectors.toList());
        }

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
