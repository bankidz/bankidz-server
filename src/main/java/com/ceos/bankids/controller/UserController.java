package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.service.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
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

}
