package com.ceos.bankids.service;

import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.oauth.KakaoTokenDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final CustomUserDetailServiceImpl customUserDetailsService;
    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Override
    public String encodeJwtToken(TokenDTO tokenDTO) {
        Date now = new Date();

        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer("bankids")
            .setIssuedAt(now)
            .setSubject(tokenDTO.getId().toString())
            .setExpiration(new Date(now.getTime() + Duration.ofMinutes(4320).toMillis()))
            .claim("id", tokenDTO.getId())
            .claim("roles", "USER")
            .signWith(SignatureAlgorithm.HS256,
                Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(
                    StandardCharsets.UTF_8)))
            .compact();
    }

    @Override
    public String getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(
                StandardCharsets.UTF_8)))
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }

    @Override
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(
            this.getUserIdFromJwtToken(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }


    @Override
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(
                    StandardCharsets.UTF_8))).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getToken(HttpServletRequest request) {
        return request.getHeader("X-AUTH-TOKEN");
    }

    @Override
    public String encodeKakaoToken(KakaoTokenDTO kakaoTokenDTO) {
        Date now = new Date();

        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer("bankids")
            .setIssuedAt(now)
            .setSubject(kakaoTokenDTO.getAccessToken())
            .setSubject(kakaoTokenDTO.getRefreshToken())
            .setExpiration(new Date(now.getTime() + Duration.ofMinutes(4320).toMillis()))
            .signWith(SignatureAlgorithm.HS256,
                Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(
                    StandardCharsets.UTF_8)))
            .compact();
    }

    @Override
    public boolean decodeKakaoToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(
                    StandardCharsets.UTF_8))).parseClaimsJws(token);
            System.out.println(claims);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
