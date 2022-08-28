package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import io.jsonwebtoken.Claims;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface AppleService {

    public AppleKeyListDTO getAppleIdentityToken();

    public Claims verifyIdentityToken(AppleRequest appleRequest,
        AppleKeyListDTO appleKeyListDTO);

    public AppleTokenDTO getAppleAccessToken(AppleRequest appleRequest);

    public Object revokeAppleAccount(AppleTokenDTO appleTokenDTO);

    public LoginDTO loginWithAuthenticationCode(Claims claims, AppleRequest appleRequest,
        HttpServletResponse response);

}
