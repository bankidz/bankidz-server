package com.ceos.bankids.mapper;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleSubjectDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import com.ceos.bankids.service.AppleServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/apple")
@RequiredArgsConstructor
public class AppleMapper {

    private final AppleServiceImpl appleService;
    private final UserServiceImpl userService;

    @Transactional
    public User postAppleLogin(MultiValueMap<String, String> formData) throws IOException {
        AppleRequest appleRequest = appleService.getAppleRequest(formData);
        AppleKeyListDTO appleKeyListDTO = appleService.getAppleIdentityToken();
        AppleSubjectDTO appleSubjectDTO = appleService.verifyIdentityToken(appleRequest,
            appleKeyListDTO);
        AppleTokenDTO appleTokenDTO = appleService.getAppleAccessToken(appleRequest, "login");

        Optional<User> registeredUser = userService.findUserByAuthenticationCodeNullable(
            appleSubjectDTO.getAuthenticationCode());

        User user;
        if (registeredUser.isPresent()) {
            user = registeredUser.get();
        } else {
            user = userService.createNewUser(
                appleRequest.getUsername(),
                appleSubjectDTO.getAuthenticationCode(),
                "apple");
        }

        return user;
    }

    public void postAppleRevoke(MultiValueMap<String, String> formData,
        HttpServletResponse response) throws IOException {
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
    }
}
