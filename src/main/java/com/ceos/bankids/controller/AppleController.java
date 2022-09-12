package com.ceos.bankids.controller;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleSubjectDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import com.ceos.bankids.service.AppleServiceImpl;
import com.ceos.bankids.service.JwtTokenServiceImpl;
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
    private final JwtTokenServiceImpl jwtTokenService;

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

        AppleTokenDTO appleTokenDTO = appleService.getAppleAccessToken(appleRequest, "login");

        User user = userService.loginWithAppleAuthenticationCode(
            appleSubjectDTO.getAuthenticationCode(), appleRequest);

        String newRefreshToken = jwtTokenService.encodeJwtRefreshToken(user.getId());
        String newAccessToken = jwtTokenService.encodeJwtToken(new TokenDTO(user));

        LoginDTO loginDTO = userService.issueNewTokens(user, newAccessToken, newRefreshToken);

        response.sendRedirect(
            "https://bankidz.com/auth/apple/callback?isKid=" + loginDTO.getIsKid() + "&level="
                + loginDTO.getLevel() + "&accessToken=" + loginDTO.getAccessToken() + "&provider="
                + loginDTO.getProvider());
    }

    @ApiOperation(value = "애플 연동해제")
    @PostMapping(value = "/revoke", produces = "application/json; charset=utf-8")
    @ResponseBody
    public void deleteAppleLogin(
        @RequestBody MultiValueMap<String, String> formData, HttpServletResponse response)
        throws IOException {

        log.info("api = 애플 연동해제");

        try {
            AppleRequest appleRequest = appleService.getAppleRequest(formData);

            AppleKeyListDTO appleKeyListDTO = appleService.getAppleIdentityToken();

            AppleSubjectDTO appleSubjectDTO = appleService.verifyIdentityToken(appleRequest,
                appleKeyListDTO);

            AppleTokenDTO appleTokenDTO = appleService.getAppleAccessToken(appleRequest, "revoke");

            Object appleResponse = appleService.revokeAppleAccount(appleTokenDTO);
        } catch (Exception e) {
            response.sendRedirect("https://bankidz.com/manage/withdraw/callback?isError=true");
        }

        response.sendRedirect("https://bankidz.com/manage/withdraw/callback?isError=false");
    }
}
