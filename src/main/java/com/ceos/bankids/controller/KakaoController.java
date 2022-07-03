package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log
@Controller
@RequestMapping("/kakao")
@RequiredArgsConstructor
public class KakaoController {

    private final UserRepository uRepo;
    private final WebClient webClient;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    @Value("${kakao.key}")
    private String KAKAO_KEY;
    @Value("${kakao.uri}")
    private String KAKAO_URI;

    @PostMapping(value = "/login", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse postKakaoLogin(@Valid @RequestBody KakaoRequest kakaoRequest,
        HttpServletResponse response) {

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + KAKAO_KEY + "&redirect_uri=" + KAKAO_URI + "&code="
                + kakaoRequest.getCode();

        KakaoTokenDTO kakaoTokenDTO = (KakaoTokenDTO) webClient.post().uri(getTokenURL).retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                clientResponse -> Mono.error(new BadRequestException("잘못된 요청입니다.")))
            .bodyToMono(
                ParameterizedTypeReference.forType(KakaoTokenDTO.class))
            .block();

        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        KakaoUserDTO kakaoUserDTO = (KakaoUserDTO) webClient.post().uri(getUserURL)
            .header("Authorization", "Bearer " + kakaoTokenDTO.getAccessToken())
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                clientResponse -> Mono.error(new BadRequestException("잘못된 요청입니다.")))
            .bodyToMono(
                ParameterizedTypeReference.forType(KakaoUserDTO.class))
            .block();

        Optional<User> user = uRepo.findByAuthenticationCode(kakaoUserDTO.getAuthenticationCode());
        if (user.isPresent()) {
            TokenDTO tokenDTO = new TokenDTO(user.get());
            Cookie cookie = new Cookie("refreshToken", user.get().getRefreshToken());

            cookie.setMaxAge(14 * 24 * 60 * 60);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setPath("/");

            response.addCookie(cookie);

            LoginDTO loginDTO = new LoginDTO(true, user.get().getIsKid(),
                jwtTokenServiceImpl.encodeJwtToken(tokenDTO));
            return CommonResponse.onSuccess(loginDTO);
        } else {
            User newUser = User.builder()
                .username(kakaoUserDTO.getKakaoAccount().getProfile().getNickname())
                .authenticationCode(kakaoUserDTO.getAuthenticationCode())
                .provider("kakao").refreshToken("")
                .build();
            uRepo.save(newUser);

            String refreshToken = jwtTokenServiceImpl.encodeJwtRefreshToken(newUser.getId());
            newUser.setRefreshToken(refreshToken);
            uRepo.save(newUser);

            Cookie cookie = new Cookie("refreshToken", refreshToken);

            cookie.setMaxAge(14 * 24 * 60 * 60);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setPath("/");

            response.addCookie(cookie);

            TokenDTO tokenDTO = new TokenDTO(newUser);
            LoginDTO loginDTO = new LoginDTO(false, null,
                jwtTokenServiceImpl.encodeJwtToken(tokenDTO));
            return CommonResponse.onSuccess(loginDTO);
        }
    }
}
