package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.UserDTO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public interface UserService {

    public UserDTO updateUserType(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody UserTypeRequest userTypeRequest);

    public String issueNewTokens(@AuthenticationPrincipal User authUser,
        String refreshToken, HttpServletResponse response);
}
