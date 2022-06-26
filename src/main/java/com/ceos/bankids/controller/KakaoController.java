package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log
@Controller
@RequestMapping("/kakao")
@RequiredArgsConstructor
public class KakaoController {

    private final UserRepository uRepo;
    private final KidRepository kRepo;
    private final ParentRepository pRepo;
    private final WebClient webClient;
    @Value("${kakao.key}")
    private String KAKAO_KEY;
    @Value("${kakao.uri}")
    private String KAKAO_URI;

    @PostMapping(value = "/login", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse postKakaoLogin(@RequestParam("code") String code) {

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + KAKAO_KEY + "&redirect_uri=" + KAKAO_URI + "&code=" + code;

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
        if (user.isEmpty()) {
            return CommonResponse.onSuccess(kakaoTokenDTO);
        }
        UserDTO userDTO = new UserDTO(user.get());

        return CommonResponse.onSuccess(userDTO);
    }

    @PostMapping(value = "/register", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<UserDTO> postKakaoRegister(
        @Valid @RequestBody KakaoRequest kakaoRequest) {

        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        KakaoUserDTO kakaoUserDTO = (KakaoUserDTO) webClient.post().uri(getUserURL)
            .header("Authorization", "Bearer " + kakaoRequest.getAccessToken())
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                clientResponse -> Mono.error(new BadRequestException("잘못된 요청입니다.")))
            .bodyToMono(
                ParameterizedTypeReference.forType(KakaoUserDTO.class))
            .block();

        Optional<User> checkUser = uRepo.findByAuthenticationCode(
            kakaoUserDTO.getAuthenticationCode());
        if (checkUser.isPresent()) {
            return CommonResponse.onFailure(HttpStatus.BAD_REQUEST, "이미 존재하는 유저입니다.");
        }

        User newUser = User.builder()
            .username(kakaoUserDTO.getKakaoAccount().getProfile().getNickname())
            .image(kakaoUserDTO.getKakaoAccount().getProfile().getImageUrl())
            .authenticationCode(kakaoUserDTO.getAuthenticationCode())
            .provider("kakao").isKid(kakaoRequest.getIsKid())
            .build();
        if (kakaoRequest.getIsKid()) {
            Kid newKid = Kid.builder()
                .period(kakaoRequest.getPeriod())
                .allowance(kakaoRequest.getAllowance())
                .user(newUser)
                .build();
            uRepo.save(newUser);
            kRepo.save(newKid);
        }
        if (kakaoRequest.getIsKid() != null) {
            Parent newParent = Parent.builder()
                .educationLevel(0L)
                .lifeLevel(0L)
                .user(newUser)
                .build();
            uRepo.save(newUser);
            pRepo.save(newParent);
        }

        UserDTO newUserDTO = new UserDTO(newUser);
        return CommonResponse.onSuccess(newUserDTO);
    }
}
