package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public User loginWithKakaoAuthenticationCode(KakaoUserDTO kakaoUserDTO);

    public User loginWithAppleAuthenticationCode(String authenticationCode,
        AppleRequest appleRequest);

    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest);

    public LoginDTO issueNewTokens(User user, String newAccessToken, String newRefreshToken);

//    public void setNewCookie(User user, HttpServletResponse response);

    public MyPageDTO getUserInformation(User user);

    public User getUserById(Long userId);

    public void updateUserLogout(User user);

    public UserDTO deleteUser(User user);

    public void updateUserExpoToken(User user, ExpoRequest expoRequest);

    public OptInDTO updateNoticeOptIn(User user);

    public OptInDTO updateServiceOptIn(User user);

    public OptInDTO getOptIn(User user);
}