package com.ceos.bankids.mapper;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.mapper.request.ExpoRequest;
import com.ceos.bankids.mapper.request.FamilyRequest;
import com.ceos.bankids.mapper.request.UserTypeRequest;
import com.ceos.bankids.mapper.request.WithdrawalRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeCompleteDeleteByKidMapperDTO;
import com.ceos.bankids.dto.KidBackupDTO;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.ParentBackupDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.ChallengeUserServiceImpl;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import com.ceos.bankids.service.KidBackupServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentBackupServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import com.ceos.bankids.service.SlackServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;
    private final FamilyServiceImpl familyService;
    private final FamilyUserServiceImpl familyUserService;
    private final ChallengeServiceImpl challengeService;
    private final KidBackupServiceImpl kidBackupService;
    private final ParentBackupServiceImpl parentBackupService;
    private final KidServiceImpl kidService;
    private final ParentServiceImpl parentService;
    private final SlackServiceImpl slackService;
    private final ExpoNotificationServiceImpl notificationService;
    private final JwtTokenServiceImpl jwtTokenService;
    private final ChallengeUserServiceImpl challengeUserService;

    @ApiOperation(value = "유저 타입 선택")
    @PatchMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> patchUserType(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody UserTypeRequest userTypeRequest) {

        log.info("api = 유저 타입 선택, user = {}", authUser.getUsername());

        UserDTO userDTO = userService.updateUserType(authUser, userTypeRequest);
        if (userDTO.getIsKid() == true) {
            kidService.createNewKid(authUser);
        } else {
            parentService.createNewParent(authUser);
        }

        return CommonResponse.onSuccess(userDTO);
    }

    @ApiOperation(value = "토큰 리프레시")
    @PatchMapping(value = "/refresh", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<LoginDTO> refreshUserToken(@AuthenticationPrincipal User authUser) {

        log.info("api = 토큰 리프레시, user = {}", authUser.getUsername());

        String newRefreshToken = jwtTokenService.encodeJwtRefreshToken(authUser.getId());
        String newAccessToken = jwtTokenService.encodeJwtToken(new TokenDTO(authUser));

        LoginDTO loginDTO = userService.issueNewTokens(authUser, newAccessToken, newRefreshToken);

        return CommonResponse.onSuccess(loginDTO);
    }

    @ApiOperation(value = "유저 정보 조회하기")
    @GetMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<MyPageDTO> getUserInfo(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 정보 조회하기, user = {}", authUser.getUsername());

        MyPageDTO myPageDTO = userService.getUserInformation(authUser);

        return CommonResponse.onSuccess(myPageDTO);
    }

    @ApiOperation(value = "유저 로그아웃")
    @PatchMapping(value = "/logout", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> patchUserLogout(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 로그아웃, user = {}", authUser.getUsername());

        userService.updateUserLogout(authUser);

        return CommonResponse.onSuccess(null);
    }

    @ApiOperation(value = "유저 탈퇴")
    @DeleteMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> deleteUserAccount(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody WithdrawalRequest withdrawalRequest) {

        log.info("api = 유저 탈퇴, user = {}", authUser.getUsername());

        Optional<FamilyUser> familyUser = familyUserService.findByUserNullable(authUser);
        if (familyUser.isPresent()) {
            Family family = familyUser.get().getFamily();
            List<FamilyUser> familyUserList = familyUserService.getFamilyUserListExclude(family,
                authUser);
            FamilyRequest familyRequest = new FamilyRequest(family.getCode());

            if (authUser.getIsKid()) {
                List<Challenge> challengeList = challengeUserService.getAllChallengeUserList(
                    authUser);
                challengeUserService.deleteAllChallengeUser(authUser);
                ChallengeCompleteDeleteByKidMapperDTO challengeCompleteDeleteByKidMapperDTO = challengeService.challengeCompleteDeleteByKid(
                    challengeList);
                kidService.updateInitKid(authUser);
                parentService.updateParentForDeleteFamilyUserByKid(familyUserList,
                    challengeCompleteDeleteByKidMapperDTO);
            } else {
                List<ChallengeUser> challengeUserList = challengeUserService.getChallengeUserListByContractUser(
                    authUser);
                kidService.updateKidForDeleteFamilyUserByParent(challengeUserList);
                parentService.updateInitParent(authUser);
                challengeService.challengeCompleteDeleteByParent(challengeUserList);
            }

            familyUserService.deleteFamilyUser(familyUser.get());
            if (familyUserList.size() == 0) {
                familyService.deleteFamily(family);
            }
        }

        if (authUser.getIsKid()) {
            KidBackupDTO kidBackupDTO = kidBackupService.backupKidUser(authUser);
            slackService.sendWithdrawalMessage("KidBackup ", kidBackupDTO.getId(),
                withdrawalRequest.getMessage());
            kidService.deleteKid(authUser);
        } else {
            ParentBackupDTO parentBackupDTO = parentBackupService.backupParentUser(authUser);
            slackService.sendWithdrawalMessage("ParentBackup ", parentBackupDTO.getId(),
                withdrawalRequest.getMessage());
            parentService.deleteParent(authUser);
        }
        notificationService.deleteAllNotification(authUser);
        UserDTO userDTO = userService.deleteUser(authUser);

        return CommonResponse.onSuccess(userDTO);
    }

    @ApiOperation(value = "유저 엑스포 토큰 등록")
    @PatchMapping(value = "/expo", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> patchExpoToken(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody ExpoRequest expoRequest, HttpServletResponse response) {

        log.info("api = 유저 엑스포 토큰 등록, user = {}", authUser.getUsername());

        userService.updateUserExpoToken(authUser, expoRequest);

        return CommonResponse.onSuccess(null);
    }

    @ApiOperation(value = "유저 공지 및 이벤트 알림 동의")
    @PatchMapping(value = "/notice", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<OptInDTO> patchNoticeOptIn(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 공지 및 이벤트 알림 동의, user = {}", authUser.getUsername());

        OptInDTO optInDTO = userService.updateNoticeOptIn(authUser);

        return CommonResponse.onSuccess(optInDTO);
    }

    @ApiOperation(value = "가족 활동 알림 동의")
    @PatchMapping(value = "/service", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<OptInDTO> patchServiceOptIn(@AuthenticationPrincipal User authUser) {

        log.info("api = 가족 활동 알림 동의, user = {}", authUser.getUsername());

        OptInDTO optInDTO = userService.updateServiceOptIn(authUser);

        return CommonResponse.onSuccess(optInDTO);
    }

    @ApiOperation(value = "유저 알림 동의 조회")
    @GetMapping(value = "/opt-in", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<OptInDTO> getOptIn(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 알림 동의 조회, user = {}", authUser.getUsername());

        OptInDTO optInDTO = userService.getOptIn(authUser);

        return CommonResponse.onSuccess(optInDTO);
    }
}