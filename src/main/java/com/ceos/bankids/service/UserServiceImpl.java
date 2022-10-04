package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByAuthenticationCodeNullable(String code) {
        return userRepository.findByAuthenticationCode(code);
    }

    @Override
    @Transactional
    public User createNewUser(String username, String code, String provider) {
        if (username.getBytes().length > 6) {
            username = username.substring(0, 3);
        }
        User user = User.builder()
            .username(username)
            .authenticationCode(code)
            .provider(provider).refreshToken("")
            .noticeOptIn(false).serviceOptIn(false)
            .build();
        userRepository.save(user);

        return user;
    }

    @Override
    @Transactional
    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest) {
        user.setBirthday(userTypeRequest.getBirthday());
        user.setIsFemale(userTypeRequest.getIsFemale());
        user.setIsKid(userTypeRequest.getIsKid());
        userRepository.save(user);

        return new UserDTO(user);
    }

    @Override
    @Transactional
    public User updateRefreshToken(User user, String newRefreshToken) {
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return user;
    }

    @Override
    @Transactional
    public void updateUserLogout(User user) {
        user.setRefreshToken("");
        user.setExpoToken("");
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void updateUserExpoToken(User user, ExpoRequest expoRequest) {
        user.setExpoToken(expoRequest.getExpoToken());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public OptInDTO updateNoticeOptIn(User user) {
        user.setNoticeOptIn(!user.getNoticeOptIn());
        userRepository.save(user);

        return new OptInDTO(user);
    }

    @Override
    @Transactional
    public OptInDTO updateServiceOptIn(User user) {
        user.setServiceOptIn(!user.getServiceOptIn());
        userRepository.save(user);

        return new OptInDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUserList() {
        return userRepository.findAll();
    }
}
