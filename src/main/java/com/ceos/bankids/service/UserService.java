package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.mapper.request.ExpoRequest;
import com.ceos.bankids.mapper.request.UserTypeRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public Optional<User> getUserByAuthenticationCodeNullable(String code);

    public User postNewUser(String username, String code, String provider);

    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest);

    public LoginDTO issueNewTokens(User user, String newAccessToken, String newRefreshToken);

//    public void setNewCookie(User user, HttpServletResponse response);

    public MyPageDTO getUserInformation(User user);

    public void updateUserLogout(User user);

    public UserDTO deleteUser(User user);

    public void updateUserExpoToken(User user, ExpoRequest expoRequest);

    public OptInDTO updateNoticeOptIn(User user);

    public OptInDTO updateServiceOptIn(User user);

    public OptInDTO getOptIn(User user);

    public List<User> readAllUserList();

}