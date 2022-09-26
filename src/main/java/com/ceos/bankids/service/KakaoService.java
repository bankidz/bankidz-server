package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import org.springframework.stereotype.Service;

@Service
public interface KakaoService {

    public KakaoTokenDTO getKakaoAccessToken(KakaoRequest kakaoRequest);

    public KakaoUserDTO getKakaoUserCode(KakaoTokenDTO kakaoTokenDTO);
}
