package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.UserRepository;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class KakaoServiceImpl implements KakaoService {

    private final UserRepository uRepo;
    private final WebClient webClient;
    private final UserServiceImpl userService;

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
            throw new BadRequestException("잘못된 요청입니다.");
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
            throw new BadRequestException("잘못된 요청입니다.");
        }
    }

    @Override
    @Transactional
    public LoginDTO loginWithAuthenticationCode(KakaoUserDTO kakaoUserDTO,
        HttpServletResponse response) {
        Optional<User> user = uRepo.findByAuthenticationCode(kakaoUserDTO.getAuthenticationCode());
        if (user.isPresent()) {
            LoginDTO loginDTO = userService.issueNewTokens(user.get(), response);

            return loginDTO;
        } else {
            User newUser = User.builder()
                .username(kakaoUserDTO.getKakaoAccount().getProfile().getNickname())
                .authenticationCode(kakaoUserDTO.getAuthenticationCode())
                .provider("kakao").refreshToken("")
                .build();
            uRepo.save(newUser);

            LoginDTO loginDTO = userService.issueNewTokens(newUser, response);

            return loginDTO;
        }
    }
}
