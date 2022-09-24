package com.ceos.bankids.controller;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.mapper.AppleMapper;
import com.ceos.bankids.mapper.UserMapper;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/apple")
@RequiredArgsConstructor
public class AppleController {

    private final AppleMapper appleMapper;
    private final UserMapper userMapper;

    @ApiOperation(value = "애플 로그인")
    @PostMapping(value = "/login", produces = "application/json; charset=utf-8", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void postAppleLogin(@RequestBody MultiValueMap<String, String> formData,
        HttpServletResponse response)
        throws IOException {

        log.info("api = 애플 로그인");

        User user = appleMapper.postAppleLogin(formData);

        LoginDTO loginDTO = userMapper.refreshUserToken(user);

        response.sendRedirect(
            "https://bankidz.com/auth/apple/callback?isKid=" + loginDTO.getIsKid() + "&level="
                + loginDTO.getLevel() + "&accessToken=" + loginDTO.getAccessToken() + "&provider="
                + loginDTO.getProvider());
    }

    @ApiOperation(value = "애플 연동해제")
    @PostMapping(value = "/revoke", produces = "application/json; charset=utf-8")
    @ResponseBody
    public void postAppleRevoke(@RequestBody MultiValueMap<String, String> formData,
        HttpServletResponse response)
        throws IOException {

        log.info("api = 애플 연동해제");

        appleMapper.postAppleRevoke(formData, response);

        response.sendRedirect("https://bankidz.com/manage/withdraw/callback?isError=false");
    }
}
