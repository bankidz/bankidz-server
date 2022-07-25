package com.ceos.bankids.config.security;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, FilterChain chain)
        throws ServletException, IOException {
        try {
            chain.doFilter(httpServletRequest, httpServletResponse);
        } catch (UnauthorizedException e) {
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(httpServletResponse.getWriter(),
                CommonResponse.onFailure(e.getMessage()));
        }
    }
}