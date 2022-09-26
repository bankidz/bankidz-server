package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.UserDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public Optional<User> findUserByAuthenticationCodeNullable(String code);

    public User createNewUser(String username, String code, String provider);

    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest);

    public User updateRefreshToken(User user, String newRefreshToken);

    public void updateUserLogout(User user);

    public void deleteUser(User user);

    public void updateUserExpoToken(User user, ExpoRequest expoRequest);

    public OptInDTO updateNoticeOptIn(User user);

    public OptInDTO updateServiceOptIn(User user);

    public List<User> getAllUserList();

}