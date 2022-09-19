package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.mapper.request.KakaoRequest;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoService {

    private final WebClient webClient;

    @Value("${kakao.key}")
    private String KAKAO_KEY;
    @Value("${kakao.uri}")
    private String KAKAO_URI;

    @Override
    public KakaoTokenDTO getKakaoAccessToken(KakaoRequest kakaoRequest) {
        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + KAKAO_KEY + "&redirect_uri=" + KAKAO_URI + "&code="
                + kakaoRequest.getCode();

        WebClient.ResponseSpec responseSpec = webClient.post().uri(getTokenURL).retrieve();

        try {
            KakaoTokenDTO kakaoTokenDTO = responseSpec.bodyToMono(KakaoTokenDTO.class).block();
            return kakaoTokenDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.KAKAO_ACCESS_TOKEN_ERROR.getErrorCode());
        }
    }

    @Override
    public KakaoUserDTO getKakaoUserCode(KakaoTokenDTO kakaoTokenDTO) {
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        WebClient.ResponseSpec responseSpec = webClient.post().uri(getUserURL)
            .header("Authorization", "Bearer " + kakaoTokenDTO.getAccessToken()).retrieve();

        try {
            KakaoUserDTO kakaoUserDTO = responseSpec.bodyToMono(KakaoUserDTO.class).block();
            return kakaoUserDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.KAKAO_USER_CODE_ERROR.getErrorCode());
        }
    }

}
