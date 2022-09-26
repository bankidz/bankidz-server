package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.controller.request.WithdrawalRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.mapper.UserMapper;
import io.swagger.annotations.ApiOperation;
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

    private final UserMapper userMapper;

    @ApiOperation(value = "유저 타입 선택")
    @PatchMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> patchUserType(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody UserTypeRequest userTypeRequest) {

        log.info("api = 유저 타입 선택, user = {}", authUser.getUsername());

        UserDTO userDTO = userMapper.updateUserType(authUser, userTypeRequest);

        return CommonResponse.onSuccess(userDTO);
    }

    @ApiOperation(value = "토큰 리프레시")
    @PatchMapping(value = "/refresh", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<LoginDTO> patchRefreshToken(@AuthenticationPrincipal User authUser) {

        log.info("api = 토큰 리프레시, user = {}", authUser.getUsername());

        LoginDTO loginDTO = userMapper.refreshUserToken(authUser);

        return CommonResponse.onSuccess(loginDTO);
    }

    @ApiOperation(value = "유저 정보 조회하기")
    @GetMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<MyPageDTO> getUserInfo(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 정보 조회하기, user = {}", authUser.getUsername());

        MyPageDTO myPageDTO = userMapper.readUserInformation(authUser);

        return CommonResponse.onSuccess(myPageDTO);
    }

    @ApiOperation(value = "유저 로그아웃")
    @PatchMapping(value = "/logout", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> patchUserLogout(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 로그아웃, user = {}", authUser.getUsername());

        userMapper.updateUserLogout(authUser);

        return CommonResponse.onSuccess(null);
    }

    @ApiOperation(value = "유저 탈퇴")
    @DeleteMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> deleteUserAccount(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody WithdrawalRequest withdrawalRequest) {

        log.info("api = 유저 탈퇴, user = {}", authUser.getUsername());

        userMapper.deleteFamilyUserIfExists(authUser);
        UserDTO userDTO = userMapper.deleteUserAccount(authUser, withdrawalRequest);

        return CommonResponse.onSuccess(userDTO);
    }

    @ApiOperation(value = "유저 엑스포 토큰 등록")
    @PatchMapping(value = "/expo", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> patchExpoToken(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody ExpoRequest expoRequest, HttpServletResponse response) {

        log.info("api = 유저 엑스포 토큰 등록, user = {}", authUser.getUsername());

        userMapper.updateUserExpoToken(authUser, expoRequest);

        return CommonResponse.onSuccess(null);
    }

    @ApiOperation(value = "유저 공지 및 이벤트 알림 동의")
    @PatchMapping(value = "/notice", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<OptInDTO> patchNoticeOptIn(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 공지 및 이벤트 알림 동의, user = {}", authUser.getUsername());

        OptInDTO optInDTO = userMapper.updateNoticeOptIn(authUser);

        return CommonResponse.onSuccess(optInDTO);
    }

    @ApiOperation(value = "가족 활동 알림 동의")
    @PatchMapping(value = "/service", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<OptInDTO> patchServiceOptIn(@AuthenticationPrincipal User authUser) {

        log.info("api = 가족 활동 알림 동의, user = {}", authUser.getUsername());

        OptInDTO optInDTO = userMapper.updateServiceOptIn(authUser);

        return CommonResponse.onSuccess(optInDTO);
    }

    @ApiOperation(value = "유저 알림 동의 조회")
    @GetMapping(value = "/opt-in", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<OptInDTO> getOptIn(@AuthenticationPrincipal User authUser) {

        log.info("api = 유저 알림 동의 조회, user = {}", authUser.getUsername());

        OptInDTO optInDTO = userMapper.readOptIn(authUser);

        return CommonResponse.onSuccess(optInDTO);
    }

}
