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
    public ChallengeUser getChallengeUser(Long challengeId) {
        ChallengeUser challengeUser = cuRepo.findByChallengeId(challengeId)
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE_USER.getErrorCode()));
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

    @Transactional(readOnly = true)
    @Override
    public List<Challenge> getAchievedChallengeUserList(User authUser) {
        return cuRepo.findByUserId(authUser.getId()).stream().map(ChallengeUser::getChallenge)
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.ACHIEVED)
            .collect(
                Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Challenge> getAllChallengeUserList(User authUser) {
        return cuRepo.findByUserId(authUser.getId()).stream().map(ChallengeUser::getChallenge)
            .collect(
                Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllChallengeUser(User authUser) {
        List<ChallengeUser> challengeUserList = cuRepo.findByUserId(authUser.getId());
        cuRepo.deleteAll(challengeUserList);
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

    public List<ChallengeUser> test(User user) {
//        HashMap<Long, Long> kidIdMappingToKidTotalChallenge = new HashMap<>();
//        HashMap<Long, Long> kidIdMappingToKidAchievedChallenge = new HashMap<>();
//        HashMap<Long, Long> kidIdMappingToKidSavings = new HashMap<>();
//        challengeList.forEach(challenge -> {
//            ChallengeUser challengeUser = cuRepo.findByChallengeId(challenge.getId()).orElseThrow(
//                () -> new BadRequestException(ErrorCode.NOT_EXIST_CHALLENGE_USER.getErrorCode()));
//            Long kidId = challengeUser.getUser().getKid().getId();
//            if (challenge.getChallengeStatus() != ChallengeStatus.PENDING
//                && challenge.getChallengeStatus() != ChallengeStatus.REJECTED) {
//                kidTotalChallenge = kidTotalChallenge + 1L;
//                kidSavings =
//                    kidSavings + challenge.getSuccessWeeks() * challenge.getWeekPrice();
//                progressRepository.deleteAll(challenge.getProgressList());
//            }
//        });
        return cuRepo.findByChallenge_ContractUser(user.getId());

    }
}
