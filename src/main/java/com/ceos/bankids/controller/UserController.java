package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.service.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Log
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

        UserDTO userDTO = userService.updateUserType(authUser, userTypeRequest);
        return CommonResponse.onSuccess(userDTO);
    }
}
