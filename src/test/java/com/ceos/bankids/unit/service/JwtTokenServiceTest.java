package com.ceos.bankids.unit.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.service.CustomUserDetailServiceImpl;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

public class JwtTokenServiceTest {

    @Test
    @DisplayName("유저 aT로 유저 조회 성공 시, id 반환하는지 확인")
    public void testIfLoadByUsernameSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        CustomUserDetailServiceImpl customUserDetailsService = Mockito.mock(
            CustomUserDetailServiceImpl.class);

        // when
        JwtTokenServiceImpl jwtTokenServiceImpl = new JwtTokenServiceImpl(
            customUserDetailsService
        );

        String token = jwtTokenServiceImpl.encodeJwtToken(new TokenDTO(user));
        String result = jwtTokenServiceImpl.getUserIdFromJwtToken(token);

        // then
        Assertions.assertEquals("1", result);
    }

    @Test
    @DisplayName("유저 rT로 유저 조회 성공 시, authentication 반환하는지 확인")
    public void testIfGetAuthenticationSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();
        CustomUserDetailServiceImpl customUserDetailsService = Mockito.mock(
            CustomUserDetailServiceImpl.class);
        Mockito.when(customUserDetailsService.loadUserByUsername("1")).thenReturn(user);

        // when
        JwtTokenServiceImpl jwtTokenServiceImpl = new JwtTokenServiceImpl(
            customUserDetailsService
        );

        String token = jwtTokenServiceImpl.encodeJwtRefreshToken(user.getId());
        Authentication result = jwtTokenServiceImpl.getAuthentication(token);

        // then
        Assertions.assertEquals(user, result.getPrincipal());
        Assertions.assertEquals("", result.getCredentials());
        Assertions.assertEquals(true, result.isAuthenticated());
        Assertions.assertEquals(null, result.getDetails());
        Assertions.assertEquals("[USER]", result.getAuthorities().toString());
    }

    @Test
    @DisplayName("aT 조회 및 무효 토큰 validate")
    public void testIfGetTokenSucceedAndValidateInvalidTokenThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();

        CustomUserDetailServiceImpl customUserDetailsService = Mockito.mock(
            CustomUserDetailServiceImpl.class);
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("X-AUTH-TOKEN")).thenReturn("aT");

        // when
        JwtTokenServiceImpl jwtTokenServiceImpl = new JwtTokenServiceImpl(
            customUserDetailsService
        );

        String token = jwtTokenServiceImpl.getToken(mockRequest);
        boolean result = jwtTokenServiceImpl.validateToken(token);

        // then
        Assertions.assertEquals("aT", token);
        Assertions.assertEquals(false, result);
    }

    @Test
    @DisplayName("aT 조회 및 유효 토큰 validate")
    public void testIfGetTokenSucceedAndValidateValidTokenThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();

        CustomUserDetailServiceImpl customUserDetailsService = Mockito.mock(
            CustomUserDetailServiceImpl.class);
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        // when
        JwtTokenServiceImpl jwtTokenServiceImpl = new JwtTokenServiceImpl(
            customUserDetailsService
        );

        String aT = jwtTokenServiceImpl.encodeJwtToken(new TokenDTO(user));
        Mockito.when(mockRequest.getHeader("X-AUTH-TOKEN")).thenReturn(aT);

        String token = jwtTokenServiceImpl.getToken(mockRequest);
        boolean result = jwtTokenServiceImpl.validateToken(token);

        // then
        Assertions.assertEquals(aT, token);
        Assertions.assertEquals(true, result);
    }

    @Test
    @DisplayName("aT 조회 및 만료 토큰 validate")
    public void testIfGetTokenSucceedAndValidateExpiredTokenThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("ozzing")
            .isFemale(null)
            .authenticationCode("1234")
            .provider("kakao")
            .isKid(null)
            .refreshToken("rT")
            .build();

        CustomUserDetailServiceImpl customUserDetailsService = Mockito.mock(
            CustomUserDetailServiceImpl.class);

        // when
        JwtTokenServiceImpl jwtTokenServiceImpl = new JwtTokenServiceImpl(
            customUserDetailsService
        );

        Date now = new Date();
        TokenDTO tokenDTO = new TokenDTO(user);
        String aT = Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer("bankids")
            .setIssuedAt(now)
            .setSubject(tokenDTO.getId().toString())
            .setExpiration(new Date(now.getTime() + Duration.ofMillis(1000).toMillis()))
            .claim("id", tokenDTO.getId())
            .claim("roles", "USER")
            .signWith(SignatureAlgorithm.HS256,
                Base64.getEncoder().encodeToString(("null").getBytes(
                    StandardCharsets.UTF_8)))
            .compact();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = jwtTokenServiceImpl.validateToken(aT);

        // then
        Assertions.assertEquals(false, result);
    }
}
