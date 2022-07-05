package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface KakaoService {

    public KakaoTokenDTO getKakaoAccessToken(KakaoRequest kakaoRequest);

    public KakaoUserDTO getKakaoUserCode(KakaoTokenDTO kakaoTokenDTO);

    public LoginDTO loginWithAuthenticationCode(KakaoUserDTO kakaoUserDTO,
        HttpServletResponse response);
}
