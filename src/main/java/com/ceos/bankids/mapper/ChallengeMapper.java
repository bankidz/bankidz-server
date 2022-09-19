package com.ceos.bankids.mapper;

import com.ceos.bankids.config.CommonResponse;
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
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeMapper {

    private final ChallengeServiceImpl challengeService;
    private final UserServiceImpl userService;
    private final FamilyServiceImpl familyService;
    private final FamilyUserServiceImpl familyUserService;
    private final ChallengeUserServiceImpl challengeUserService;
    private final ExpoNotificationServiceImpl notificationService;
    private final ParentServiceImpl parentService;
    private final KidServiceImpl kidService;

    // 돈길 생성 API Mapper
    public ChallengeDTO postChallenge(User authUser, ChallengeRequest challengeRequest) {

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
        ChallengeUser challengeUser = challengeUserService.postChallengeUser(authUser, challenge);
        parentService.updateParentForCreateChallenge(contractUser);

        // 저장로직 성공시 알림 로직
        notificationService.createPendingChallengeNotification(contractUser, challengeUser);

        return challengeDTO;
    }

    public ChallengeDTO deleteChallenge(User authUser, Long challengeId) {

        userRoleValidation(authUser, true);
        ChallengeUser challengeUser = challengeUserService.getChallengeUser(challengeId);
        Challenge deleteChallenge = challengeUser.getChallenge();
        if (challengeUser.getUser().getId() != authUser.getId()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_CHALLENGE_USER.getErrorCode());
        }
        if (deleteChallenge.getChallengeStatus() == ChallengeStatus.WALKING) {
            kidService.checkKidDeleteChallenge(authUser);
            challengeUserService.deleteChallengeUser(authUser, challengeId);
            return challengeService.deleteWalkingChallenge(
                authUser,
                challengeUser);
        } else if (deleteChallenge.getChallengeStatus() == ChallengeStatus.FAILED) {
            challengeUserService.deleteChallengeUser(authUser, challengeId);
            return challengeService.deleteWalkingChallenge(
                authUser,
                challengeUser);
        } else if (deleteChallenge.getChallengeStatus() == ChallengeStatus.REJECTED) {
            challengeUserService.deleteChallengeUser(authUser, challengeId);
            return challengeService.deleteRejectedChallenge(
                authUser,
                challengeUser);
        } else if (deleteChallenge.getChallengeStatus() == ChallengeStatus.PENDING) {
            challengeUserService.deleteChallengeUser(authUser, challengeId);
            return challengeService.deletePendingChallenge(
                authUser,
                challengeUser);
        }

        throw new BadRequestException(ErrorCode.CANT_DELETE_CHALLENGE_STATUS.getErrorCode());
    }

    public List<ChallengeDTO> getListChallenge(User authUser, String status) {

        log.info("api = 돈길 리스트 가져오기, user = {}, status = {}", authUser.getUsername(), status);
        if (!Objects.equals(status, "walking") && !Objects.equals(status, "pending")) {
            throw new BadRequestException(ErrorCode.INVALID_QUERYPARAM.getErrorCode());
        }
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<Challenge> challengeList = challengeUserService.getChallengeUserList(authUser,
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

    public KidChallengeListDTO getListKidChallenge(User authUser, Long kidId, String status) {

        Kid kid = kidService.getKid(kidId);
        User kidUser = kid.getUser();
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<Challenge> challengeList = challengeUserService.getChallengeUserList(kidUser,
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
        KidChallengeListDTO kidChallengeListDTO = new KidChallengeListDTO(kidUser,
            challengeDTOList);
        return kidChallengeListDTO;
    }

    @ApiOperation(value = "자녀의 돈길 수락 / 거절")
    @PatchMapping(value = "/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<ChallengeDTO> patchChallengeStatus(@AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId,
        @Valid @RequestBody KidChallengeRequest kidChallengeRequest) {

        log.info("api = 자녀의 돈길 수락 / 거절, user = {}, challengeId = {}, 수락여부 = {}",
            authUser.getUsername(), challengeId, kidChallengeRequest.getAccept());
        ChallengeUser challengeUser = challengeUserService.getChallengeUser(challengeId);
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
            return CommonResponse.onSuccess(challengeDTO);
        } else {
            ChallengeDTO challengeDTO = challengeService.updateChallengeStatusToRejected(challenge,
                kidChallengeRequest, authUser);
            notificationService.notification(challenge, user);
            return CommonResponse.onSuccess(challengeDTO);
        }
    }

    @ApiOperation(value = "주차 정보 가져오기")
    @GetMapping(value = "/progress", produces = "application/json; charset=utf-8")
    public CommonResponse<WeekDTO> getWeekInfo(@AuthenticationPrincipal User authUser) {

        log.info("api = 주차 정보 가져오기, user = {}", authUser.getUsername());
        List<Challenge> walkingChallengeList = challengeUserService.getChallengeUserList(authUser,
                "walking")
            .stream()
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.WALKING).collect(
                Collectors.toList());
        WeekDTO weekDTO = challengeService.readWeekInfo(walkingChallengeList);

        return CommonResponse.onSuccess(weekDTO);
    }

    @ApiOperation(value = "자녀의 주차 정보 가져오기")
    @GetMapping(value = "/kid/progress/{kidId}", produces = "application/json; charset=utf-8")
    public CommonResponse<KidWeekDTO> getKidWeekInfo(@AuthenticationPrincipal User authUser,
        @PathVariable Long kidId) {

        log.info("api = 자녀의 주차 정보 가져오기, user = {}, kid = {}", authUser.getUsername(), kidId);
        Kid kid = kidService.getKid(kidId);
        User kidUser = kid.getUser();
        familyUserService.checkSameFamily(authUser, kidUser);
        List<Challenge> kidWalkingChallengeList = challengeUserService.getChallengeUserList(kidUser,
                "walking")
            .stream()
            .filter(challenge -> challenge.getChallengeStatus() == ChallengeStatus.WALKING).collect(
                Collectors.toList());
        WeekDTO weekDTO = challengeService.readWeekInfo(kidWalkingChallengeList);
        KidWeekDTO kidWeekDTO = new KidWeekDTO(kid, weekDTO);

        return CommonResponse.onSuccess(kidWeekDTO);
    }

    @ApiOperation(value = "완주한 돈길 리스트 가져오기")
    @GetMapping(value = "/achieved", produces = "application/json; charset=utf-8")
    public CommonResponse<AchievedChallengeListDTO> getAchievedListChallenge(
        @AuthenticationPrincipal User authUser, @RequestParam String interestPayment) {

        log.info("api = 완주한 돈길 리스트 가져오기, user = {}", authUser.getUsername());
        List<Challenge> achievedChallengeUserList = challengeUserService.getAchievedChallengeUserList(
            authUser);
        AchievedChallengeListDTO achievedChallengeListDTO = challengeService.readAchievedChallenge(
            achievedChallengeUserList,
            interestPayment);

        return CommonResponse.onSuccess(achievedChallengeListDTO);
    }

    @ApiOperation(value = "자녀의 완주한 돈길 리스트 가져오기")
    @GetMapping(value = "kid/achieved/{kidId}", produces = "application/json; charset=utf-8")
    public CommonResponse<KidAchievedChallengeListDTO> getKidAchievedListChallenge(
        @AuthenticationPrincipal User authUser, @PathVariable Long kidId,
        @RequestParam String interestPayment) {

        log.info("api = 완주한 돈길 리스트 가져오기, user = {}, kid = {}", authUser.getUsername(), kidId);
        Kid kid = kidService.getKid(kidId);
        User kidUser = kid.getUser();
        familyUserService.checkSameFamily(authUser, kidUser);
        List<Challenge> achievedChallengeUserList = challengeUserService.getAchievedChallengeUserList(
            kidUser);
        KidAchievedChallengeListDTO kidAchievedChallengeListDTO = challengeService.readKidAchievedChallenge(
            authUser, achievedChallengeUserList, interestPayment, kidId);

        return CommonResponse.onSuccess(kidAchievedChallengeListDTO);
    }

    @ApiOperation(value = "완주한 돈길에 이자 지급하기")
    @PatchMapping(value = "/interest-payment/{challengeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<AchievedChallengeDTO> patchInterestPayment(
        @AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId) {

        log.info("api = 완주한 돈길에 이자 지급, user = {}, challengeId = {}", authUser.getUsername(),
            challengeId);
        AchievedChallengeDTO achievedChallengeDTO = challengeService.updateChallengeInterestPayment(
            authUser,
            challengeId);

        return CommonResponse.onSuccess(achievedChallengeDTO);
    }

    @ApiOperation(value = "돈길 걷기")
    @PatchMapping(value = "/{challengeId}/progress", produces = "application/json; charset=utf-8")
    public CommonResponse<ProgressDTO> patchProgress(@AuthenticationPrincipal User authUser,
        @PathVariable Long challengeId) {

        log.info("api = 돈길 걷기, user = {}, challengeId = {}", authUser, challengeId);
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

        return CommonResponse.onSuccess(progressDTO);
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
