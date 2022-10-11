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
    public ChallengeUser createdChallengeUser(User user, Challenge challenge) {
        ChallengeUser challengeUser = ChallengeUser.builder().user(user).challenge(challenge)
            .member("parent")
            .build();
        cuRepo.save(challengeUser);
        return challengeUser;
    }

    @Transactional(readOnly = true)
    @Override
    public ChallengeUser readChallengeUser(Long challengeId) {
        ChallengeUser challengeUser = cuRepo.findByChallengeId(challengeId)
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE_USER.getErrorCode()));
        return challengeUser;
    }

    @Transactional
    @Override
    public List<Challenge> readChallengeUserList(User authUser, String status) {
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

    @Transactional(readOnly = true)
    @Override
    public List<Challenge> readAchievedChallengeUserList(User authUser) {
        return cuRepo.findByUserId(authUser.getId()).stream().map(ChallengeUser::getChallenge)
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.ACHIEVED)
            .collect(
                Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Challenge> readKidAchievedChallengeUserList(User authUser, User kidUser) {
        return cuRepo.findByUserId(kidUser.getId())
            .stream().map(ChallengeUser::getChallenge)
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.ACHIEVED
                && challenge.getContractUser().getId() == authUser.getId())
            .collect(
                Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Challenge> readAllChallengeUserListToChallengeList(User authUser) {
        return cuRepo.findByUserId(authUser.getId()).stream().map(ChallengeUser::getChallenge)
            .collect(
                Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllChallengeUserOfUser(User authUser) {
        List<ChallengeUser> challengeUserList = cuRepo.findByUserId(authUser.getId());
        cuRepo.deleteAll(challengeUserList);
    }

    @Transactional(readOnly = true)
    public void checkMaxChallengeCount(User user) {
//        List<Challenge> walkingChallengeList = cuRepo.findByUserId(user.getId()).stream()
//            .map(ChallengeUser::getChallenge)
//            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.WALKING).collect(
//                Collectors.toList());
        List<ChallengeUser> walkingChallengeList = cuRepo.findByUserIdAndChallenge_ChallengeStatus(
            user.getId(), ChallengeStatus.WALKING);
        if (walkingChallengeList.size() >= 5) {
            throw new ForbiddenException(ErrorCode.CHALLENGE_COUNT_OVER_FIVE.getErrorCode());
        }
    }

    @Transactional(readOnly = true)
    public List<ChallengeUser> getChallengeUserListByContractUser(User user) {
        return cuRepo.findByChallenge_ContractUserId(user.getId());

    }
}
