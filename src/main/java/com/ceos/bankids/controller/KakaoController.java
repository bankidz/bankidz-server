package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import com.ceos.bankids.service.KakaoServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/kakao")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoServiceImpl kakaoService;
    private final UserServiceImpl userService;
    private final JwtTokenServiceImpl jwtTokenService;

    @ApiOperation(value = "카카오 로그인")
    @PostMapping(value = "/login", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse<LoginDTO> postKakaoLogin(@Valid @RequestBody KakaoRequest kakaoRequest,
        HttpServletResponse response) {

        log.info("api = 카카오 로그인");

        KakaoTokenDTO kakaoTokenDTO = kakaoService.getKakaoAccessToken(kakaoRequest);

        KakaoUserDTO kakaoUserDTO = kakaoService.getKakaoUserCode(kakaoTokenDTO);

        Optional<User> registeredUser = userService.getUserByAuthenticationCodeNullable(
            kakaoUserDTO.getAuthenticationCode());

        User user;
        if (registeredUser.isPresent()) {
            user = registeredUser.get();
        } else {
            user = userService.postNewUser(
                kakaoUserDTO.getKakaoAccount().getProfile().getNickname(),
                kakaoUserDTO.getAuthenticationCode(),
                "kakao");
        }

        String newRefreshToken = jwtTokenService.encodeJwtRefreshToken(user.getId());
        String newAccessToken = jwtTokenService.encodeJwtToken(new TokenDTO(user));

        LoginDTO loginDTO = userService.issueNewTokens(user, newAccessToken, newRefreshToken);

        return CommonResponse.onSuccess(loginDTO);
    }
}
