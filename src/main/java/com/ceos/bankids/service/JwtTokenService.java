package com.ceos.bankids.service;

import com.ceos.bankids.dto.TokenDTO;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public interface JwtTokenService {

    public String encodeJwtToken(TokenDTO tokenDTO);

    public String encodeJwtRefreshToken(Long id);

    public Long getUserIdFromJwtToken(String token);

    public Authentication getAuthentication(String token);

    public Boolean validateToken(String token);

    public String getToken(HttpServletRequest request);
}
