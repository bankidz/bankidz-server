package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.KakaoController;
import com.ceos.bankids.controller.request.KakaoRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.oauth.KakaoAccountDTO;
import com.ceos.bankids.dto.oauth.KakaoProfileDTO;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import com.ceos.bankids.service.KakaoServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

public class KakaoControllerTest {

    @Test
    @DisplayName("카카오 로그인 코드 없어 실패 시, 에러 처리 되는지 확인")
    public void testIfKakaoLoginWithoutCodeFailThrowNullPointerException() {
        // given
        HttpServletResponse response = null;
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        WebClient mockWebClient = Mockito.mock(WebClient.class);

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            jwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
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
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        KakaoRequest kakaoRequest = new KakaoRequest("code");

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            jwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
        );

        // then
        Assertions.assertThrows(NullPointerException.class, () -> {
            kakaoController.postKakaoLogin(kakaoRequest, null);
        });
    }

    @Test
    @DisplayName("기존 자녀 유저가 카카오 로그인 성공 시, 결과 반환하는지 확인")
    public void testIfKidKakaoLoginSucceedReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("ozzing")
            .isFemale(true)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(true)
            .refreshToken("rT")
            .build();
        Kid kid = Kid.builder()
            .level(1L)
            .user(user)
            .build();
        user.setKid(kid);
        LoginDTO login = new LoginDTO(true, "aT", 1L, "kakao");

        HttpServletResponse response = null;
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(user));
        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(
            RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.doReturn("rT").when(jwtTokenServiceImpl).encodeJwtRefreshToken(1L);
        Mockito.doReturn("aT").when(jwtTokenServiceImpl).encodeJwtToken(new TokenDTO(user));

        KakaoRequest kakaoRequest = new KakaoRequest("aT");
        KakaoTokenDTO kakaoTokenDTO = new KakaoTokenDTO("aT", "rT");
        KakaoAccountDTO kakaoAccountDTO = new KakaoAccountDTO(new KakaoProfileDTO("ozzing"));
        Timestamp timeStamp = Timestamp.valueOf(LocalDateTime.now());
        KakaoUserDTO kakaoUserDTO = new KakaoUserDTO("1234", timeStamp, kakaoAccountDTO);

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + "null" + "&redirect_uri=" + "null" + "&code="
                + kakaoRequest.getCode();
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getTokenURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoTokenDTO.class))
            .thenReturn(Mono.just(kakaoTokenDTO));

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getUserURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header("Authorization", "Bearer " + "aT"))
            .thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoUserDTO.class))
            .thenReturn(Mono.just(kakaoUserDTO));

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            jwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
        );
        CommonResponse<LoginDTO> result = kakaoController.postKakaoLogin(kakaoRequest, response);

        // then
        LoginDTO loginDTO = new LoginDTO(true, "aT", 1L, "kakao");
        Assertions.assertEquals(loginDTO, result.getData());
    }

    @Test
    @DisplayName("기존 부모 유저가 카카오 로그인 성공 시, 결과 반환하는지 확인")
    public void testIfParentKakaoLoginSucceedReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("ozzing")
            .isFemale(true)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(false)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(false, "aT", "kakao");

        HttpServletResponse response = null;
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(user));
        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(
            RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);
        UserServiceImpl mockUserService = Mockito.mock(UserServiceImpl.class);
        Mockito.when(mockUserService.issueNewTokens(user, "aT", "rT")).thenReturn(login);
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.doReturn("rT").when(jwtTokenServiceImpl).encodeJwtRefreshToken(1L);
        Mockito.doReturn("aT").when(jwtTokenServiceImpl).encodeJwtToken(new TokenDTO(user));

        KakaoRequest kakaoRequest = new KakaoRequest("aT");
        KakaoTokenDTO kakaoTokenDTO = new KakaoTokenDTO("aT", "rT");
        KakaoAccountDTO kakaoAccountDTO = new KakaoAccountDTO(new KakaoProfileDTO("ozzing"));
        Timestamp timeStamp = Timestamp.valueOf(LocalDateTime.now());
        KakaoUserDTO kakaoUserDTO = new KakaoUserDTO("1234", timeStamp, kakaoAccountDTO);

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + "null" + "&redirect_uri=" + "null" + "&code="
                + kakaoRequest.getCode();
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getTokenURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoTokenDTO.class))
            .thenReturn(Mono.just(kakaoTokenDTO));

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getUserURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header("Authorization", "Bearer " + "aT"))
            .thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoUserDTO.class))
            .thenReturn(Mono.just(kakaoUserDTO));

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            jwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
        );
        CommonResponse<LoginDTO> result = kakaoController.postKakaoLogin(kakaoRequest, response);

        // then
        LoginDTO loginDTO = new LoginDTO(false, "aT", "kakao");
        Assertions.assertEquals(loginDTO, result.getData());
    }

    @Test
    @DisplayName("새로운 유저가 카카오 로그인 성공 시, 결과 반환하는지 확인")
    public void testIfNewUserKakaoLoginSucceedReturnResult() {
        // given
        User user = User.builder()
            .username("홍길동")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(null, "aT", null);

        HttpServletResponse response = null;

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(null));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(
            RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

        JwtTokenServiceImpl mockJwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.doReturn("rT").when(mockJwtTokenServiceImpl).encodeJwtRefreshToken(1L);
        Mockito.doReturn("aT").when(mockJwtTokenServiceImpl).encodeJwtToken(new TokenDTO(user));

        KakaoRequest kakaoRequest = new KakaoRequest("aT");
        KakaoTokenDTO kakaoTokenDTO = new KakaoTokenDTO("aT", "rT");
        KakaoAccountDTO kakaoAccountDTO = new KakaoAccountDTO(
            new KakaoProfileDTO(user.getUsername()));
        Timestamp timeStamp = Timestamp.valueOf(LocalDateTime.now());
        KakaoUserDTO kakaoUserDTO = new KakaoUserDTO("1234", timeStamp, kakaoAccountDTO);

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + "null" + "&redirect_uri=" + "null" + "&code="
                + kakaoRequest.getCode();
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getTokenURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoTokenDTO.class))
            .thenReturn(Mono.just(kakaoTokenDTO));

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getUserURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header("Authorization", "Bearer " + "aT"))
            .thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoUserDTO.class))
            .thenReturn(Mono.just(kakaoUserDTO));

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockJwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
        );

        CommonResponse<LoginDTO> result = kakaoController.postKakaoLogin(kakaoRequest, response);
        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(mockUserRepository, Mockito.times(2)).save(uCaptor.capture());
        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(user.getUsername(), uCaptor.getValue().getUsername());

        // then
        LoginDTO loginDTO = new LoginDTO(null, "aT", "kakao");
        Assertions.assertEquals(loginDTO, result.getData());
    }

    @Test
    @DisplayName("이름이 긴 새로운 유저가 카카오 로그인 성공 시, 결과 반환하는지 확인")
    public void testIfNewUserWithLongNameKakaoLoginSucceedReturnResult() {
        // given
        User user = User.builder()
            .username("홍길동그라미")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(null, "aT", null);

        HttpServletResponse response = null;

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(null));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(
            RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);

        KakaoRequest kakaoRequest = new KakaoRequest("aT");
        KakaoTokenDTO kakaoTokenDTO = new KakaoTokenDTO("aT", "rT");
        KakaoAccountDTO kakaoAccountDTO = new KakaoAccountDTO(
            new KakaoProfileDTO(user.getUsername()));
        Timestamp timeStamp = Timestamp.valueOf(LocalDateTime.now());
        KakaoUserDTO kakaoUserDTO = new KakaoUserDTO("1234", timeStamp, kakaoAccountDTO);

        JwtTokenServiceImpl mockJwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.doReturn("rT").when(mockJwtTokenServiceImpl).encodeJwtRefreshToken(1L);
        user.setUsername(user.getUsername().substring(0, 3));
        Mockito.doReturn("aT").when(mockJwtTokenServiceImpl).encodeJwtToken(new TokenDTO(user));

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + "null" + "&redirect_uri=" + "null" + "&code="
                + kakaoRequest.getCode();
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getTokenURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoTokenDTO.class))
            .thenReturn(Mono.just(kakaoTokenDTO));

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getUserURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header("Authorization", "Bearer " + "aT"))
            .thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoUserDTO.class))
            .thenReturn(Mono.just(kakaoUserDTO));

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockJwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
        );

        CommonResponse<LoginDTO> result = kakaoController.postKakaoLogin(kakaoRequest, response);
        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(mockUserRepository, Mockito.times(2)).save(uCaptor.capture());
        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(user.getUsername(), uCaptor.getValue().getUsername());

        // then
        LoginDTO loginDTO = new LoginDTO(null, "aT", "kakao");
        Assertions.assertEquals(loginDTO, result.getData());
    }

    @Test
    @DisplayName("카카오 AccessToken 요청 중 에러 발생 시, 에러 처리 확인")
    public void testIfGetKakaoAccessTokenFailThrowBadRequestException() {
        // given
        User user = User.builder()
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(null, "aT", "kakao");

        HttpServletResponse response = null;
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(null));
        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(
            RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);
        UserServiceImpl mockUserService = Mockito.mock(UserServiceImpl.class);
        Mockito.when(mockUserService.issueNewTokens(user, "aT", "rT")).thenReturn(login);
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        KakaoRequest kakaoRequest = new KakaoRequest("aT");
        KakaoTokenDTO kakaoTokenDTO = new KakaoTokenDTO("aT", "rT");
        KakaoAccountDTO kakaoAccountDTO = new KakaoAccountDTO(new KakaoProfileDTO("ozzing"));
        Timestamp timeStamp = Timestamp.valueOf(LocalDateTime.now());
        KakaoUserDTO kakaoUserDTO = new KakaoUserDTO("1234", timeStamp, kakaoAccountDTO);

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + "null" + "&redirect_uri=" + "null" + "&code="
                + kakaoRequest.getCode();
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getTokenURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoTokenDTO.class))
            .thenReturn(Mono.error(new BadRequestException("잘못된 요청입니다.")));

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getUserURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header("Authorization", "Bearer " + "aT"))
            .thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoUserDTO.class))
            .thenReturn(Mono.just(kakaoUserDTO));

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            jwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            kakaoController.postKakaoLogin(kakaoRequest, response);
        });
    }

    @Test
    @DisplayName("카카오 User 정보 요청 중 에러 발생 시, 에러 처리 확인")
    public void testIfGetKakaoUserInfoFailThrowBadRequestException() {
        // given
        User user = User.builder()
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(null, "aT", "kakao");

        HttpServletResponse response = null;
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(null));
        WebClient mockWebClient = Mockito.mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(
            RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);
        UserServiceImpl mockUserService = Mockito.mock(UserServiceImpl.class);
        Mockito.when(mockUserService.issueNewTokens(user, "aT", "rT")).thenReturn(login);
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        KakaoRequest kakaoRequest = new KakaoRequest("aT");
        KakaoTokenDTO kakaoTokenDTO = new KakaoTokenDTO("aT", "rT");
        KakaoAccountDTO kakaoAccountDTO = new KakaoAccountDTO(new KakaoProfileDTO("ozzing"));
        Timestamp timeStamp = Timestamp.valueOf(LocalDateTime.now());
        KakaoUserDTO kakaoUserDTO = new KakaoUserDTO("1234", timeStamp, kakaoAccountDTO);

        String getTokenURL =
            "https://kauth.kakao.com/oauth/token?grant_type=authorization_code&client_id="
                + "null" + "&redirect_uri=" + "null" + "&code="
                + kakaoRequest.getCode();
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getTokenURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoTokenDTO.class))
            .thenReturn(Mono.just(kakaoTokenDTO));

        Mockito.when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(getUserURL)).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header("Authorization", "Bearer " + "aT"))
            .thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(KakaoUserDTO.class))
            .thenReturn(Mono.error(new BadRequestException("잘못된 요청입니다.")));

        // when
        KakaoServiceImpl kakaoService = new KakaoServiceImpl(
            mockWebClient
        );
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            jwtTokenServiceImpl
        );
        KakaoController kakaoController = new KakaoController(
            kakaoService,
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            kakaoController.postKakaoLogin(kakaoRequest, response);
        });
    }
}
