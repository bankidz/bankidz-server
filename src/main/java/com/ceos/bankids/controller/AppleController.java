package com.ceos.bankids.controller;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleSubjectDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import com.ceos.bankids.service.AppleServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
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

    private final AppleServiceImpl appleService;
    private final UserServiceImpl userService;

    @ApiOperation(value = "애플 로그인")
    @PostMapping(value = "/login", produces = "application/json; charset=utf-8", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void postAppleLogin(
        @RequestBody MultiValueMap<String, String> formData, HttpServletResponse response)
        throws IOException {

        log.info("api = 애플 로그인");
        AppleRequest appleRequest = appleService.getAppleRequest(formData);

        AppleKeyListDTO appleKeyListDTO = appleService.getAppleIdentityToken();

        AppleSubjectDTO appleSubjectDTO = appleService.verifyIdentityToken(appleRequest,
            appleKeyListDTO);

        AppleTokenDTO appleTokenDTO = appleService.getAppleAccessToken(appleRequest);

        LoginDTO loginDTO = userService.loginWithAppleAuthenticationCode(
            appleSubjectDTO.getAuthenticationCode(), appleRequest);

        response.sendRedirect(
            "https://bankidz.com/auth/apple/callback?isKid=" + loginDTO.getIsKid() + "&level="
                + loginDTO.getLevel() + "&accessToken=" + loginDTO.getAccessToken());
    }

    @ApiOperation(value = "애플 연동해제")
    @PostMapping(value = "/revoke", produces = "application/json; charset=utf-8")
    @ResponseBody
    public void deleteAppleLogin(
        @RequestBody MultiValueMap<String, String> formData, HttpServletResponse response)
        throws IOException {

        log.info("api = 애플 연동해제");
        AppleRequest appleRequest = appleService.getAppleRequest(formData);

        AppleKeyListDTO appleKeyListDTO = appleService.getAppleIdentityToken();

        AppleSubjectDTO appleSubjectDTO = appleService.verifyIdentityToken(appleRequest,
            appleKeyListDTO);

        AppleTokenDTO appleTokenDTO = appleService.getAppleAccessToken(appleRequest);

        Object appleResponse = appleService.revokeAppleAccount(appleTokenDTO);

        response.sendRedirect("https://bankidz.com/auth/apple/callback");
    }
}
