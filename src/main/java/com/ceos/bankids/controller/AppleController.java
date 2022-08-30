package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import com.ceos.bankids.service.AppleServiceImpl;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @ApiOperation(value = "애플 로그인")
    @PostMapping(value = "/login", produces = "application/json; charset=utf-8", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void postAppleLogin(
        @RequestBody MultiValueMap<String, String> formData, HttpServletResponse response)
        throws IOException {

        AppleRequest appleRequest = new AppleRequest(formData.get("code").get(0),
            formData.get("id_token").get(0));

        log.info("api = 애플 로그인");
        AppleKeyListDTO appleKeyListDTO = appleService.getAppleIdentityToken();

        Claims claims = appleService.verifyIdentityToken(appleRequest, appleKeyListDTO);

        AppleTokenDTO appleTokenDTO = appleService.getAppleAccessToken(appleRequest);

        LoginDTO loginDTO = appleService.loginWithAuthenticationCode(claims, appleRequest,
            response);

        response.sendRedirect("https://bankidz.com/auth/apple/callback");
    }

    @ApiOperation(value = "애플 연동해제")
    @DeleteMapping(value = "/login", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<Object> deleteAppleLogin(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody AppleRequest appleRequest, HttpServletResponse response) {

        log.info("api = 애플 연동해제");
        AppleKeyListDTO appleKeyListDTO = appleService.getAppleIdentityToken();

        Claims claims = appleService.verifyIdentityToken(appleRequest, appleKeyListDTO);

        AppleTokenDTO appleTokenDTO = appleService.getAppleAccessToken(appleRequest);

        Object appleResponse = appleService.revokeAppleAccount(appleTokenDTO);

        return CommonResponse.onSuccess(appleResponse);
    }

}
