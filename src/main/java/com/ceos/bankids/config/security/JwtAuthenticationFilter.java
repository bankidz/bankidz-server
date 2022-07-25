package com.ceos.bankids.config.security;

import com.ceos.bankids.exception.UnauthorizedException;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenServiceImpl jwtTokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String token = jwtTokenService.getToken((HttpServletRequest) request);
        if (token != null) {
            if (jwtTokenService.validateToken(token)) {
                Authentication authentication = jwtTokenService.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                throw new UnauthorizedException("유효하지 않은 토큰입니다.");
            }
        }
        chain.doFilter(request, response);
    }

}
