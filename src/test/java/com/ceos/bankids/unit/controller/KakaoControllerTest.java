package com.ceos.bankids.unit.controller;

import com.ceos.bankids.controller.KakaoController;
import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.KakaoServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

public class KakaoControllerTest {

    @Test
    @DisplayName("카카오 로그인 코드 없어 실패 시, 에러 처리 되는지 확인")
    public void testIfKakaoLoginWithoutCodeFailThrowNullPointerException() {
        // given
        HttpServletResponse response = null;
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        WebClient mockWebClient = Mockito.mock(WebClient.class);
        UserServiceImpl mockUserService = Mockito.mock(UserServiceImpl.class);

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockUserRepository,
            mockWebClient,
            mockUserService
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService
        );

        // then
        Assertions.assertThrows(NullPointerException.class, () -> {
            kakaoController.postKakaoLogin(null, response);
        });
    }

    @Test
    @DisplayName("카카오 로그인 코드 에러로 실패 시, 에러 처리 되는지 확인")
    public void testIfKakaoLoginWithWrongCodeFailThrowBadRequestException() {
        // given
        HttpServletResponse response = null;
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        WebClient mockWebClient = Mockito.mock(WebClient.class);
        UserServiceImpl mockUserService = Mockito.mock(UserServiceImpl.class);
        KakaoRequest kakaoRequest = new KakaoRequest("code");

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockUserRepository,
            mockWebClient,
            mockUserService
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService
        );

        // then
        Assertions.assertThrows(NullPointerException.class, () -> {
            kakaoController.postKakaoLogin(kakaoRequest, null);
        });
    }
}
