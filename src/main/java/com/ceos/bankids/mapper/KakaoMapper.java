package com.ceos.bankids.mapper;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.service.KakaoServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/kakao")
@RequiredArgsConstructor
public class KakaoMapper {

    private final KakaoServiceImpl kakaoService;
    private final UserServiceImpl userService;

    @Transactional
    public User postKakaoLogin(KakaoRequest kakaoRequest) {

        KakaoTokenDTO kakaoTokenDTO = kakaoService.getKakaoAccessToken(kakaoRequest);
        KakaoUserDTO kakaoUserDTO = kakaoService.getKakaoUserCode(kakaoTokenDTO);

        Optional<User> registeredUser = userService.readUserByAuthenticationCodeNullable(
            kakaoUserDTO.getAuthenticationCode());

        User user;
        if (registeredUser.isPresent()) {
            user = registeredUser.get();
        } else {
            user = userService.createNewUser(
                kakaoUserDTO.getKakaoAccount().getProfile().getNickname(),
                kakaoUserDTO.getAuthenticationCode(),
                "kakao");
        }

        return user;
    }
}
