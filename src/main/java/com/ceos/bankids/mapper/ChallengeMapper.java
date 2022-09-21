package com.ceos.bankids.mapper;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AchievedChallengeDTO;
import com.ceos.bankids.dto.AchievedChallengeListDTO;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.ChallengeListMapperDTO;
import com.ceos.bankids.dto.ChallengePostDTO;
import com.ceos.bankids.dto.KidAchievedChallengeListDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.dto.KidWeekDTO;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.mapper.request.ChallengeRequest;
import com.ceos.bankids.mapper.request.KidChallengeRequest;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.ChallengeUserServiceImpl;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeMapper {

    private final ChallengeServiceImpl challengeService;
    private final FamilyUserServiceImpl familyUserService;
    private final ChallengeUserServiceImpl challengeUserService;
    private final ExpoNotificationServiceImpl notificationService;
    private final ParentServiceImpl parentService;
    private final KidServiceImpl kidService;

    // 돈길 생성 API Mapper
    @Transactional
    public ChallengeDTO createChallengeMapper(User authUser, ChallengeRequest challengeRequest) {

        // validation
        sundayValidation();
        userRoleValidation(authUser, true);
        challengeUserService.checkMaxChallengeCount(authUser);

        // 계약 대상 부모 유저 가져오기
        User contractUser = familyUserService.getContractUser(authUser,
            challengeRequest.getIsMom());

        // 실제 돈길 저장로직
        ChallengePostDTO challengePostDTO = new ChallengePostDTO(challengeRequest, contractUser);
        ChallengeDTO challengeDTO = challengeService.createChallenge(authUser, challengePostDTO);
        Challenge challenge = challengeService.readChallenge(challengeDTO.getId());
        ChallengeUser challengeUser = challengeUserService.createdChallengeUser(authUser,
            challenge);
        parentService.updateParentForCreateChallenge(contractUser);

        // 저장로직 성공시 알림 로직
        notificationService.createPendingChallengeNotification(contractUser, challengeUser);

        return challengeDTO;
    }

    // 돈길 삭제 API Mapper
    @Transactional
    public ChallengeDTO deleteChallengeMapper(User authUser, Long challengeId) {

        sundayValidation();
        userRoleValidation(authUser, true);
        ChallengeUser challengeUser = challengeUserService.readChallengeUser(challengeId);
        Challenge deleteChallenge = challengeUser.getChallenge();
        if (challengeUser.getUser().getId() != authUser.getId()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_CHALLENGE_USER.getErrorCode());
        }
        if (deleteChallenge.getChallengeStatus() == ChallengeStatus.WALKING) {
            kidService.checkKidDeleteChallenge(authUser, deleteChallenge);
            return challengeService.deleteWalkingChallenge(
                authUser,
                challengeUser);
        } else if (deleteChallenge.getChallengeStatus() == ChallengeStatus.FAILED) {
            return challengeService.deleteWalkingChallenge(
                authUser,
                challengeUser);
        } else if (deleteChallenge.getChallengeStatus() == ChallengeStatus.REJECTED) {
            return challengeService.deleteRejectedChallenge(
                authUser,
                challengeUser);
        } else if (deleteChallenge.getChallengeStatus() == ChallengeStatus.PENDING) {
            return challengeService.deletePendingChallenge(
                authUser,
                challengeUser);
        }

        throw new BadRequestException(ErrorCode.CANT_DELETE_CHALLENGE_STATUS.getErrorCode());
    }

    // 돈길 리스트 가져오기 API Mapper
    @Transactional
    public List<ChallengeDTO> readChallengeListMapper(User authUser, String status) {

        userRoleValidation(authUser, true);
        if (!Objects.equals(status, "walking") && !Objects.equals(status, "pending")) {
            throw new BadRequestException(ErrorCode.INVALID_QUERYPARAM.getErrorCode());
        }
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<Challenge> challengeList = challengeUserService.readChallengeUserList(authUser,
            status);
        if (Objects.equals(status, "walking")) {
            challengeList.forEach(challenge -> {
                ChallengeListMapperDTO challengeListMapperDTO = challengeService.readWalkingChallenge(
                    challenge);
                if (challengeListMapperDTO.getChangeStatus()
                    && challenge.getChallengeStatus() == ChallengeStatus.ACHIEVED) {
                    notificationService.challengeAchievedNotification(authUser,
                        challenge.getContractUser(),
                        challenge);
                    kidService.userLevelUp(challenge.getContractUser(),
                        authUser);
                } else if (challengeListMapperDTO.getChangeStatus()
                    && challenge.getChallengeStatus() == ChallengeStatus.FAILED) {
                    notificationService.challengeFailedNotification(challenge.getContractUser(),
                        challenge.getChallengeUser());
                }
                if (challenge.getChallengeStatus() != ChallengeStatus.ACHIEVED) {
                    ChallengeDTO challengeDTO = new ChallengeDTO(
                        challengeListMapperDTO.getChallenge(),
                        challengeListMapperDTO.getProgressDTOList(), null);
                    challengeDTOList.add(challengeDTO);
                }
            });
        } else if (Objects.equals(status, "pending")) {
            challengeList.forEach(challenge -> {
                ChallengeListMapperDTO challengeListMapperDTO = challengeService.readPendingChallenge(
                    challenge);
                ChallengeDTO challengeDTO = new ChallengeDTO(challengeListMapperDTO.getChallenge(),
                    null, challenge.getComment());
                challengeDTOList.add(challengeDTO);
            });
        }

        return challengeDTOList;
    }

    // 자녀의 돈길 리스트 가져오기 API Mapper
    @Transactional
    public KidChallengeListDTO readKidChallengeListMapper(User authUser, Long kidId,
        String status) {

        userRoleValidation(authUser, false);
        Kid kid = kidService.getKid(kidId);
        User kidUser = kid.getUser();
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<Challenge> challengeList = challengeUserService.readChallengeUserList(kidUser,
            status);
        if (Objects.equals(status, "walking")) {
            challengeList.forEach(challenge -> {
                ChallengeListMapperDTO challengeListMapperDTO = challengeService.readWalkingChallenge(
                    challenge);
                if (challengeListMapperDTO.getChangeStatus()
                    && challenge.getChallengeStatus() == ChallengeStatus.ACHIEVED) {
                    notificationService.challengeAchievedNotification(authUser,
                        challenge.getContractUser(), challenge);
                    kidService.userLevelUp(challenge.getContractUser(),
                        authUser);
                } else if (challengeListMapperDTO.getChangeStatus()
                    && challenge.getChallengeStatus() == ChallengeStatus.FAILED) {
                    notificationService.challengeFailedNotification(challenge.getContractUser(),
                        challenge.getChallengeUser());
                }
                if (challenge.getChallengeStatus() != ChallengeStatus.ACHIEVED) {
                    ChallengeDTO challengeDTO = new ChallengeDTO(
                        challengeListMapperDTO.getChallenge(),
                        challengeListMapperDTO.getProgressDTOList(), null);
                    challengeDTOList.add(challengeDTO);
                }
            });
        } else if (Objects.equals(status, "pending")) {
            challengeList.forEach(challenge -> {
                if (challenge.getContractUser().getId() == authUser.getId()) {
                    ChallengeListMapperDTO challengeListMapperDTO = challengeService.readPendingChallenge(
                        challenge);
                    ChallengeDTO challengeDTO = new ChallengeDTO(
                        challengeListMapperDTO.getChallenge(),
                        null, challenge.getComment());
                    challengeDTOList.add(challengeDTO);
                }
            });
        }
        return new KidChallengeListDTO(kidUser,
            challengeDTOList);
    }

    // 돈길 수락 / 거절 API Mapper
    @Transactional
    public ChallengeDTO updateChallengeStatusMapper(User authUser, Long challengeId,
        KidChallengeRequest kidChallengeRequest) {

        sundayValidation();
        userRoleValidation(authUser, false);
        ChallengeUser challengeUser = challengeUserService.readChallengeUser(challengeId);
        User user = challengeUser.getUser();
        Challenge challenge = challengeService.readChallenge(challengeId);
        if (challenge.getContractUser().getId() != authUser.getId()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_CONTRACT_USER.getErrorCode());
        }
        if (kidChallengeRequest.getAccept()) {
            challengeUserService.checkMaxChallengeCount(user);
            ChallengeDTO challengeDTO = challengeService.updateChallengeStatusToWalking(challenge);
            kidService.updateKidTotalChallenge(user);
            parentService.updateParentAcceptedChallenge(authUser);
            notificationService.notification(challenge, user);
            return challengeDTO;
        } else {
            ChallengeDTO challengeDTO = challengeService.updateChallengeStatusToRejected(challenge,
                kidChallengeRequest, authUser);
            notificationService.notification(challenge, user);
            return challengeDTO;
        }
    }

    // 주차 정보 가져오기 API Mapper
    @Transactional(readOnly = true)
    public WeekDTO readWeekInfoMapper(User authUser) {

        userRoleValidation(authUser, true);
        List<Challenge> walkingChallengeList = challengeUserService.readChallengeUserList(authUser,
                "walking")
            .stream()
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.WALKING).collect(
                Collectors.toList());

        return challengeService.readWeekInfo(walkingChallengeList);
    }

    // 자녀의 주차 정보 가져오기 API Mapper
    @Transactional(readOnly = true)
    public KidWeekDTO readKidWeekInfoMapper(User authUser, Long kidId) {

        userRoleValidation(authUser, false);
        Kid kid = kidService.getKid(kidId);
        User kidUser = kid.getUser();
        familyUserService.checkSameFamily(authUser, kidUser);
        List<Challenge> kidWalkingChallengeList = challengeUserService.readChallengeUserList(
                kidUser,
                "walking")
            .stream()
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.WALKING).collect(
                Collectors.toList());
        WeekDTO weekDTO = challengeService.readWeekInfo(kidWalkingChallengeList);

        return new KidWeekDTO(kid, weekDTO);
    }

    // 완주한 돈길 리스트 가져오기 API Mapper
    @Transactional(readOnly = true)
    public AchievedChallengeListDTO readAchievedChallengeListMapper(User authUser,
        String interestPayment) {

        userRoleValidation(authUser, true);
        List<Challenge> achievedChallengeUserList = challengeUserService.readAchievedChallengeUserList(
            authUser);

        return challengeService.readAchievedChallenge(
            achievedChallengeUserList,
            interestPayment);
    }

    // 자녀의 완주한 돈길 리스트 가져오기 API Mapper
    @Transactional(readOnly = true)
    public KidAchievedChallengeListDTO readKidAchievedChallengeListMapper(User authUser, Long kidId,
        String interestPayment) {

        userRoleValidation(authUser, false);
        Kid kid = kidService.getKid(kidId);
        User kidUser = kid.getUser();
        familyUserService.checkSameFamily(authUser, kidUser);
        List<Challenge> achievedChallengeUserList = challengeUserService.readAchievedChallengeUserList(
            kidUser);

        return challengeService.readKidAchievedChallenge(
            authUser, achievedChallengeUserList, interestPayment, kidId);
    }

    // 이자 지급 API Mapper
    @Transactional
    public AchievedChallengeDTO updateChallengeInterestPaymentMapper(User authUser,
        Long challengeId) {

        sundayValidation();
        userRoleValidation(authUser, false);
        return challengeService.updateChallengeInterestPayment(
            authUser,
            challengeId);
    }

    // 돈길 걷기 API Mapper
    @Transactional
    public ProgressDTO updateProgressMapper(User authUser, Long challengeId) {

        sundayValidation();
        userRoleValidation(authUser, true);
        Challenge challenge = challengeService.readChallenge(challengeId);
        if (challenge.getChallengeStatus() != ChallengeStatus.WALKING) {
            throw new BadRequestException(ErrorCode.NOT_WALKING_CHALLENGE.getErrorCode());
        }
        ProgressDTO progressDTO = challengeService.updateProgress(challenge);
        kidService.updateKidByPatchProgress(authUser, challenge);
        notificationService.runProgressNotification(authUser, challenge.getContractUser(),
            challenge);

        if (progressDTO.getChallengeStatus() == ChallengeStatus.ACHIEVED) {
            kidService.userLevelUp(challenge.getContractUser(), authUser);
            notificationService.challengeAchievedNotification(authUser, challenge.getContractUser(),
                challenge);
        }

        return progressDTO;
    }

    // 일요일 처리 validation
    private void sundayValidation() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTimestamp = Timestamp.valueOf(now);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(nowTimestamp);
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int value = dayOfWeek.getValue();
        if (value == 7) {       // test환경에선 접근이 안되는 8로 실환경에선 일요일인 7로 설정
            throw new ForbiddenException(ErrorCode.SUNDAY_ERROR.getErrorCode());
        }
    }

    // 유저의 역할 검사 validation
    private void userRoleValidation(User user, Boolean approveRole) {
        if (user.getIsKid() != approveRole) {
            throw new ForbiddenException(ErrorCode.USER_ROLE_ERROR.getErrorCode());
        }
    }
}
