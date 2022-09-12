package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidDTO;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.ParentDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.UserRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User loginWithKakaoAuthenticationCode(KakaoUserDTO kakaoUserDTO) {

        Optional<User> user = userRepository.findByAuthenticationCode(
            kakaoUserDTO.getAuthenticationCode());
        if (user.isPresent()) {
            return user.get();
        } else {
            String username = kakaoUserDTO.getKakaoAccount().getProfile().getNickname();
            if (username.getBytes().length > 6) {
                username = username.substring(0, 3);
            }
            User newUser = User.builder()
                .username(username)
                .authenticationCode(kakaoUserDTO.getAuthenticationCode())
                .provider("kakao").refreshToken("")
                .noticeOptIn(false).serviceOptIn(false)
                .build();
            userRepository.save(newUser);
            return newUser;
        }
    }


    @Override
    @Transactional
    public User loginWithAppleAuthenticationCode(String authenticationCode,
        AppleRequest appleRequest) {

        Optional<User> user = userRepository.findByAuthenticationCode(authenticationCode);
        if (user.isPresent()) {
            return user.get();
        } else {
            String username = appleRequest.getUsername();
            if (username.getBytes().length > 6) {
                username = username.substring(0, 3);
            }
            User newUser = User.builder()
                .username(username)
                .authenticationCode(authenticationCode)
                .provider("apple").refreshToken("")
                .noticeOptIn(false).serviceOptIn(false)
                .build();
            userRepository.save(newUser);
            return newUser;
        }
    }

    @Override
    @Transactional
    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest) {

        if (user.getIsFemale() != null) {
            throw new BadRequestException(ErrorCode.USER_ALREADY_HAS_TYPE.getErrorCode());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(userTypeRequest.getBirthday());
        } catch (ParseException e) {
            throw new BadRequestException(ErrorCode.INVALID_BIRTHDAY.getErrorCode());
        }

        Calendar cal = Calendar.getInstance();
        Integer currYear = cal.get(Calendar.YEAR);
        Integer birthYear = Integer.parseInt(userTypeRequest.getBirthday()) / 10000;
        if (birthYear >= currYear || birthYear <= currYear - 100) {
            throw new BadRequestException(ErrorCode.INVALID_BIRTHDAY.getErrorCode());
        }

        user.setBirthday(userTypeRequest.getBirthday());
        user.setIsFemale(userTypeRequest.getIsFemale());
        user.setIsKid(userTypeRequest.getIsKid());
        userRepository.save(user);

        return new UserDTO(user);
    }

    @Override
    @Transactional
    public LoginDTO issueNewTokens(User user, String newAccessToken, String newRefreshToken) {
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        LoginDTO loginDTO;
        if (user.getIsKid() == null || user.getIsKid() == false) {
            loginDTO = new LoginDTO(user.getIsKid(), newAccessToken, user.getProvider());
        } else {
            loginDTO = new LoginDTO(user.getIsKid(), newAccessToken, user.getKid().getLevel(),
                user.getProvider());
        }
        return loginDTO;
    }

//    @Override
//    @Transactional
//    public void setNewCookie(User user, HttpServletResponse response) {
//        Cookie cookie = new Cookie("refreshToken", user.getRefreshToken());
//        cookie.setMaxAge(14 * 24 * 60 * 60);
//        cookie.setSecure(true);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//
//        response.addCookie(cookie);
//    }

    @Override
    @Transactional
    public MyPageDTO getUserInformation(User user) {
        MyPageDTO myPageDTO;
        UserDTO userDTO = new UserDTO(user);
        if (user.getIsKid() == null) {
            throw new BadRequestException(ErrorCode.USER_TYPE_NOT_CHOSEN.getErrorCode());
        } else if (user.getIsKid() == true) {
            KidDTO kidDTO = new KidDTO(user.getKid());
            myPageDTO = new MyPageDTO(userDTO, kidDTO);
        } else {
            ParentDTO parentDTO = new ParentDTO(user.getParent());
            myPageDTO = new MyPageDTO(userDTO, parentDTO);
        }
        return myPageDTO;
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
    public UserDTO deleteUser(User user) {
        UserDTO userDTO = new UserDTO(user);
        userRepository.delete(user);
        return userDTO;
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
    @Transactional
    public OptInDTO getOptIn(User user) {
        return new OptInDTO(user);
    }
}
