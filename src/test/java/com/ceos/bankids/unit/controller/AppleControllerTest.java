package com.ceos.bankids.unit.controller;

import com.ceos.bankids.controller.AppleController;
import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.oauth.AppleKeyDTO;
import com.ceos.bankids.dto.oauth.AppleKeyListDTO;
import com.ceos.bankids.dto.oauth.AppleSubjectDTO;
import com.ceos.bankids.dto.oauth.AppleTokenDTO;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.AppleServiceImpl;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.util.MultiValueMap;

public class AppleControllerTest {

    private AppleKeyDTO appleKeyDTO1 = new AppleKeyDTO("RSA", "fh6Bs8C", "sig", "RS256",
        "u704gotMSZc6CSSVNCZ1d0S9dZKwO2BVzfdTKYz8wSNm7R_KIufOQf3ru7Pph1FjW6gQ8zgvhnv4IebkGWsZJlodduTC7c0sRb5PZpEyM6PtO8FPHowaracJJsK1f6_rSLstLdWbSDXeSq7vBvDu3Q31RaoV_0YlEzQwPsbCvD45oVy5Vo5oBePUm4cqi6T3cZ-10gr9QJCVwvx7KiQsttp0kUkHM94PlxbG_HAWlEZjvAlxfEDc-_xZQwC6fVjfazs3j1b2DZWsGmBRdx1snO75nM7hpyRRQB4jVejW9TuZDtPtsNadXTr9I5NjxPdIYMORj9XKEh44Z73yfv0gtw",
        "AQAB");
    private AppleKeyDTO appleKeyDTO2 = new AppleKeyDTO("RSA", "W6WcOKB", "sig", "RS256",
        "2Zc5d0-zkZ5AKmtYTvxHc3vRc41YfbklflxG9SWsg5qXUxvfgpktGAcxXLFAd9Uglzow9ezvmTGce5d3DhAYKwHAEPT9hbaMDj7DfmEwuNO8UahfnBkBXsCoUaL3QITF5_DAPsZroTqs7tkQQZ7qPkQXCSu2aosgOJmaoKQgwcOdjD0D49ne2B_dkxBcNCcJT9pTSWJ8NfGycjWAQsvC8CGstH8oKwhC5raDcc2IGXMOQC7Qr75d6J5Q24CePHj_JD7zjbwYy9KNH8wyr829eO_G4OEUW50FAN6HKtvjhJIguMl_1BLZ93z2KJyxExiNTZBUBQbbgCNBfzTv7JrxMw",
        "AQAB");
    private AppleKeyDTO appleKeyDTO3 = new AppleKeyDTO("RSA", "YuyXoY", "sig", "RS256",
        "1JiU4l3YCeT4o0gVmxGTEK1IXR-Ghdg5Bzka12tzmtdCxU00ChH66aV-4HRBjF1t95IsaeHeDFRgmF0lJbTDTqa6_VZo2hc0zTiUAsGLacN6slePvDcR1IMucQGtPP5tGhIbU-HKabsKOFdD4VQ5PCXifjpN9R-1qOR571BxCAl4u1kUUIePAAJcBcqGRFSI_I1j_jbN3gflK_8ZNmgnPrXA0kZXzj1I7ZHgekGbZoxmDrzYm2zmja1MsE5A_JX7itBYnlR41LOtvLRCNtw7K3EFlbfB6hkPL-Swk5XNGbWZdTROmaTNzJhV-lWT0gGm6V1qWAK2qOZoIDa_3Ud0Gw",
        "AQAB");

    @Test
    @DisplayName("기존 자녀 유저가 애플 로그인 성공 시, 결과 반환하는지 확인")
    public void testIfKidAppleLoginSucceedReturnResult() {
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
        LoginDTO login = new LoginDTO(true, "aT", 1L, "apple");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(user));
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.doReturn("rT").when(jwtTokenServiceImpl).encodeJwtRefreshToken(1L);
        Mockito.doReturn("aT").when(jwtTokenServiceImpl).encodeJwtToken(new TokenDTO(user));
        MultiValueMap<String, String> formData = null;

        AppleRequest appleRequest = new AppleRequest("code",
            "eyJraWQiOiJmaDZCczhDIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiY29tLmJhbmtpZHouYmFua2lkei13ZWIiLCJleHAiOjE2NjE5MzUyNDEsImlhdCI6MTY2MTg0ODg0MSwic3ViIjoiMDAxMzYyLjE0ZjNiODk3ZDY2MTRlZjI4ODZiZDM5NDIyZGE5ZGY0LjA5MzUiLCJub25jZSI6ImhpIiwiY19oYXNoIjoiWnplMVpsOGhXLUFwdDRHbHpBUk9ZUSIsImF1dGhfdGltZSI6MTY2MTg0ODg0MSwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.lwp2PLBkaGm08riMEMWrzZREXfbMMQvxnYppUENgeWCOq76BdrTdLHpEMnYNiTzGSEXgXtFw8TQZIPY4uPJfEmHZ0lxoiHaReloaGHISZBt50wC2eEYI3-0CPM5sB-GMa8rExYxfq8FL6BbRf5g9jIDhdOfJa4X9xqtJFgfbGf8NMTHnVV8YvjmNkWpFotVcjHHUkkjqo_8u8YA-DFDQr46hDvDzIL2Oq2q10EhD9Z3BubfJQV5QgIfot1BMOMmAvyXANzAN1YEJUhkDNlbaY3fiBtyCYWRzbNi8cX5jW69lkIT4Isxxw5Tj3GvlQRjkC_OA3TuzO8jq87bjQjTPcw",
            "ozzing");
        List<AppleKeyDTO> appleKeyDTOList = new ArrayList<>();
        appleKeyDTOList.add(appleKeyDTO1);
        appleKeyDTOList.add(appleKeyDTO2);
        appleKeyDTOList.add(appleKeyDTO3);
        AppleKeyListDTO appleKeyListDTO = new AppleKeyListDTO(appleKeyDTOList);
        AppleSubjectDTO appleSubjectDTO = new AppleSubjectDTO("1234");
        AppleTokenDTO appleTokenDTO = null;

        AppleServiceImpl appleService = Mockito.mock(AppleServiceImpl.class);
        Mockito.doReturn(appleRequest).when(appleService).getAppleRequest(formData);
        Mockito.doReturn(appleKeyListDTO).when(appleService).getAppleIdentityToken();
        Mockito.doReturn(appleSubjectDTO).when(appleService)
            .verifyIdentityToken(appleRequest, appleKeyListDTO);
        Mockito.doReturn(appleTokenDTO).when(appleService).getAppleAccessToken(appleRequest);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            kidRepository,
            parentRepository,
            jwtTokenServiceImpl
        );
        AppleController appleController = new AppleController(
            appleService,
            userService
        );

        // then
        try {
            appleController.postAppleLogin(formData, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("새로운 유저가 애플 로그인 성공 시, 결과 반환하는지 확인")
    public void testIfNewUserAppleLoginSucceedReturnResult() {
        // given
        User user = User.builder()
            .username("홍길동")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(null, "aT", "apple");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(null));
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.doReturn("rT").when(jwtTokenServiceImpl).encodeJwtRefreshToken(1L);
        Mockito.doReturn("aT").when(jwtTokenServiceImpl).encodeJwtToken(new TokenDTO(user));
        MultiValueMap<String, String> formData = null;

        AppleRequest appleRequest = new AppleRequest("code",
            "eyJraWQiOiJmaDZCczhDIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiY29tLmJhbmtpZHouYmFua2lkei13ZWIiLCJleHAiOjE2NjE5MzUyNDEsImlhdCI6MTY2MTg0ODg0MSwic3ViIjoiMDAxMzYyLjE0ZjNiODk3ZDY2MTRlZjI4ODZiZDM5NDIyZGE5ZGY0LjA5MzUiLCJub25jZSI6ImhpIiwiY19oYXNoIjoiWnplMVpsOGhXLUFwdDRHbHpBUk9ZUSIsImF1dGhfdGltZSI6MTY2MTg0ODg0MSwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.lwp2PLBkaGm08riMEMWrzZREXfbMMQvxnYppUENgeWCOq76BdrTdLHpEMnYNiTzGSEXgXtFw8TQZIPY4uPJfEmHZ0lxoiHaReloaGHISZBt50wC2eEYI3-0CPM5sB-GMa8rExYxfq8FL6BbRf5g9jIDhdOfJa4X9xqtJFgfbGf8NMTHnVV8YvjmNkWpFotVcjHHUkkjqo_8u8YA-DFDQr46hDvDzIL2Oq2q10EhD9Z3BubfJQV5QgIfot1BMOMmAvyXANzAN1YEJUhkDNlbaY3fiBtyCYWRzbNi8cX5jW69lkIT4Isxxw5Tj3GvlQRjkC_OA3TuzO8jq87bjQjTPcw",
            user.getUsername());
        List<AppleKeyDTO> appleKeyDTOList = new ArrayList<>();
        appleKeyDTOList.add(appleKeyDTO1);
        appleKeyDTOList.add(appleKeyDTO2);
        appleKeyDTOList.add(appleKeyDTO3);
        AppleKeyListDTO appleKeyListDTO = new AppleKeyListDTO(appleKeyDTOList);
        AppleSubjectDTO appleSubjectDTO = new AppleSubjectDTO("1234");
        AppleTokenDTO appleTokenDTO = null;

        AppleServiceImpl appleService = Mockito.mock(AppleServiceImpl.class);
        Mockito.doReturn(appleRequest).when(appleService).getAppleRequest(formData);
        Mockito.doReturn(appleKeyListDTO).when(appleService).getAppleIdentityToken();
        Mockito.doReturn(appleSubjectDTO).when(appleService)
            .verifyIdentityToken(appleRequest, appleKeyListDTO);
        Mockito.doReturn(appleTokenDTO).when(appleService).getAppleAccessToken(appleRequest);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            kidRepository,
            parentRepository,
            jwtTokenServiceImpl
        );
        AppleController appleController = new AppleController(
            appleService,
            userService
        );

        // then
        try {
            appleController.postAppleLogin(formData, response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(mockUserRepository, Mockito.times(2)).save(uCaptor.capture());
        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(user.getUsername(), uCaptor.getValue().getUsername());
    }

    @Test
    @DisplayName("이름이 긴 새로운 유저가 애플 로그인 성공 시, 결과 반환하는지 확인")
    public void testIfNewUserWithLongNameAppleLoginSucceedReturnResult() {
        // given
        User user = User.builder()
            .username("홍길동그라미")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(null, "aT", "apple");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByAuthenticationCode("1234")).thenReturn(
            Optional.ofNullable(null));
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        MultiValueMap<String, String> formData = null;

        AppleRequest appleRequest = new AppleRequest("code",
            "eyJraWQiOiJmaDZCczhDIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiY29tLmJhbmtpZHouYmFua2lkei13ZWIiLCJleHAiOjE2NjE5MzUyNDEsImlhdCI6MTY2MTg0ODg0MSwic3ViIjoiMDAxMzYyLjE0ZjNiODk3ZDY2MTRlZjI4ODZiZDM5NDIyZGE5ZGY0LjA5MzUiLCJub25jZSI6ImhpIiwiY19oYXNoIjoiWnplMVpsOGhXLUFwdDRHbHpBUk9ZUSIsImF1dGhfdGltZSI6MTY2MTg0ODg0MSwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.lwp2PLBkaGm08riMEMWrzZREXfbMMQvxnYppUENgeWCOq76BdrTdLHpEMnYNiTzGSEXgXtFw8TQZIPY4uPJfEmHZ0lxoiHaReloaGHISZBt50wC2eEYI3-0CPM5sB-GMa8rExYxfq8FL6BbRf5g9jIDhdOfJa4X9xqtJFgfbGf8NMTHnVV8YvjmNkWpFotVcjHHUkkjqo_8u8YA-DFDQr46hDvDzIL2Oq2q10EhD9Z3BubfJQV5QgIfot1BMOMmAvyXANzAN1YEJUhkDNlbaY3fiBtyCYWRzbNi8cX5jW69lkIT4Isxxw5Tj3GvlQRjkC_OA3TuzO8jq87bjQjTPcw",
            user.getUsername());
        List<AppleKeyDTO> appleKeyDTOList = new ArrayList<>();
        appleKeyDTOList.add(appleKeyDTO1);
        appleKeyDTOList.add(appleKeyDTO2);
        appleKeyDTOList.add(appleKeyDTO3);
        AppleKeyListDTO appleKeyListDTO = new AppleKeyListDTO(appleKeyDTOList);
        AppleSubjectDTO appleSubjectDTO = new AppleSubjectDTO("1234");
        AppleTokenDTO appleTokenDTO = null;

        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        Mockito.doReturn("rT").when(jwtTokenServiceImpl).encodeJwtRefreshToken(1L);
        user.setUsername(user.getUsername().substring(0, 3));
        Mockito.doReturn("aT").when(jwtTokenServiceImpl).encodeJwtToken(new TokenDTO(user));
        AppleServiceImpl appleService = Mockito.mock(AppleServiceImpl.class);
        Mockito.doReturn(appleRequest).when(appleService).getAppleRequest(formData);
        Mockito.doReturn(appleKeyListDTO).when(appleService).getAppleIdentityToken();
        Mockito.doReturn(appleSubjectDTO).when(appleService)
            .verifyIdentityToken(appleRequest, appleKeyListDTO);
        Mockito.doReturn(appleTokenDTO).when(appleService).getAppleAccessToken(appleRequest);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            kidRepository,
            parentRepository,
            jwtTokenServiceImpl
        );
        AppleController appleController = new AppleController(
            appleService,
            userService
        );

        // then
        try {
            appleController.postAppleLogin(formData, response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(mockUserRepository, Mockito.times(2)).save(uCaptor.capture());
        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(user.getUsername(), uCaptor.getValue().getUsername());
    }

    @Test
    @DisplayName("유저 탈퇴 전 애플 연동 해제 성공 시, 리다이렉트하는지 확인")
    public void testIfUserRevokeSucceedReturnResult() {
        // given
        User user = User.builder()
            .username("홍길동")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        LoginDTO login = new LoginDTO(null, "aT", "apple");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        KidRepository kidRepository = Mockito.mock(KidRepository.class);
        ParentRepository parentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        MultiValueMap<String, String> formData = null;

        AppleRequest appleRequest = new AppleRequest("code",
            "eyJraWQiOiJmaDZCczhDIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiY29tLmJhbmtpZHouYmFua2lkei13ZWIiLCJleHAiOjE2NjE5MzUyNDEsImlhdCI6MTY2MTg0ODg0MSwic3ViIjoiMDAxMzYyLjE0ZjNiODk3ZDY2MTRlZjI4ODZiZDM5NDIyZGE5ZGY0LjA5MzUiLCJub25jZSI6ImhpIiwiY19oYXNoIjoiWnplMVpsOGhXLUFwdDRHbHpBUk9ZUSIsImF1dGhfdGltZSI6MTY2MTg0ODg0MSwibm9uY2Vfc3VwcG9ydGVkIjp0cnVlfQ.lwp2PLBkaGm08riMEMWrzZREXfbMMQvxnYppUENgeWCOq76BdrTdLHpEMnYNiTzGSEXgXtFw8TQZIPY4uPJfEmHZ0lxoiHaReloaGHISZBt50wC2eEYI3-0CPM5sB-GMa8rExYxfq8FL6BbRf5g9jIDhdOfJa4X9xqtJFgfbGf8NMTHnVV8YvjmNkWpFotVcjHHUkkjqo_8u8YA-DFDQr46hDvDzIL2Oq2q10EhD9Z3BubfJQV5QgIfot1BMOMmAvyXANzAN1YEJUhkDNlbaY3fiBtyCYWRzbNi8cX5jW69lkIT4Isxxw5Tj3GvlQRjkC_OA3TuzO8jq87bjQjTPcw",
            user.getUsername());
        List<AppleKeyDTO> appleKeyDTOList = new ArrayList<>();
        appleKeyDTOList.add(appleKeyDTO1);
        appleKeyDTOList.add(appleKeyDTO2);
        appleKeyDTOList.add(appleKeyDTO3);
        AppleKeyListDTO appleKeyListDTO = new AppleKeyListDTO(appleKeyDTOList);
        AppleSubjectDTO appleSubjectDTO = new AppleSubjectDTO("1234");
        AppleTokenDTO appleTokenDTO = null;
        Object object = null;

        AppleServiceImpl appleService = Mockito.mock(AppleServiceImpl.class);
        Mockito.doReturn(appleRequest).when(appleService).getAppleRequest(formData);
        Mockito.doReturn(appleKeyListDTO).when(appleService).getAppleIdentityToken();
        Mockito.doReturn(appleSubjectDTO).when(appleService)
            .verifyIdentityToken(appleRequest, appleKeyListDTO);
        Mockito.doReturn(appleTokenDTO).when(appleService).getAppleAccessToken(appleRequest);
        Mockito.doReturn(object).when(appleService).revokeAppleAccount(appleTokenDTO);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            kidRepository,
            parentRepository,
            jwtTokenServiceImpl
        );
        AppleController appleController = new AppleController(
            appleService,
            userService
        );

        // then
        try {
            appleController.deleteAppleLogin(formData, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
