package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.UserDTO;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest);

    public LoginDTO issueNewTokens(User user, HttpServletResponse response);

    public void setNewCookie(User user, HttpServletResponse response);

    public MyPageDTO getUserInformation(User user);

    public User getUserByRefreshToken(String refreshToken);

    public UserDTO updateUserLogout(User user);

    public UserDTO deleteUser(User user);

    public User updateUserExpoToken(User user, ExpoRequest expoRequest);
}