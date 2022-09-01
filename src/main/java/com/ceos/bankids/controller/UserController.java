package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.controller.request.WithdrawalRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.KidBackupDTO;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.ParentBackupDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.KidBackupServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentBackupServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import com.ceos.bankids.service.SlackServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final ChallengeServiceImpl challengeService;
    private final KidBackupServiceImpl kidBackupService;
    private final ParentBackupServiceImpl parentBackupService;
    private final KidServiceImpl kidService;
    private final ParentServiceImpl parentService;
    private final SlackServiceImpl slackService;

    @ApiOperation(value = "유저 타입 선택")
    @PatchMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> patchUserType(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody UserTypeRequest userTypeRequest) {

        log.info("api = 유저 타입 선택, user = {}", authUser.getUsername());
        UserDTO userDTO = userService.updateUserType(authUser, userTypeRequest);

        return CommonResponse.onSuccess(userDTO);
    }

    @ApiOperation(value = "토큰 리프레시")
    @PatchMapping(value = "/refresh", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<LoginDTO> refreshUserToken(
        @CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {

        log.info("api = 토큰 리프레시");
        User user = userService.getUserByRefreshToken(refreshToken);
        LoginDTO loginDTO = userService.issueNewTokens(user, response);

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
        UserDTO userDTO = userService.updateUserLogout(authUser);

        return CommonResponse.onSuccess(userDTO);
    }

    @ApiOperation(value = "유저 탈퇴")
    @DeleteMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> deleteUserAccount(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody WithdrawalRequest withdrawalRequest) {

        log.info("api = 유저 탈퇴, user = {}", authUser.getUsername());

        FamilyDTO familyDTO = familyService.getFamily(authUser);
        if (familyDTO.getCode() != null) {
            FamilyRequest familyRequest = new FamilyRequest(familyDTO.getCode());
            if (authUser.getIsKid()) {
                challengeService.challengeCompleteDeleteByKid(authUser, familyRequest);
            } else {
                challengeService.challengeCompleteDeleteByParent(authUser, familyRequest);
            }

            FamilyDTO deletedFamilyDTO = familyService.deleteFamilyUser(authUser,
                familyRequest.getCode());
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

        UserDTO userDTO = userService.deleteUser(authUser);

        return CommonResponse.onSuccess(userDTO);
    }

    @ApiOperation(value = "유저 엑스포 토큰 등록")
    @PatchMapping(value = "/expo", produces = "application/json; charset=utf-8")
    @ResponseBody
    public void patchExpoToken(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody ExpoRequest expoRequest, HttpServletResponse response) {

        log.info("api = 유저 엑스포 토큰 등록, user = {}", authUser.getUsername());
        User user = userService.updateUserExpoToken(authUser, expoRequest);

        userService.setNewCookie(user, response);
    }
}
