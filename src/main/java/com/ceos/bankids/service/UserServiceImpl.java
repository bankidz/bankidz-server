package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.request.AppleRequest;
import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidDTO;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.OptInDTO;
import com.ceos.bankids.dto.ParentDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.dto.oauth.KakaoUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import java.util.Calendar;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository uRepo;
    private final KidRepository kRepo;
    private final ParentRepository pRepo;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    @Override
    @Transactional
    public LoginDTO loginWithKakaoAuthenticationCode(KakaoUserDTO kakaoUserDTO) {
        String provider = "kakao";
        Optional<User> user = uRepo.findByAuthenticationCode(kakaoUserDTO.getAuthenticationCode());
        if (user.isPresent()) {
            LoginDTO loginDTO = this.issueNewTokens(user.get(), provider);

            return loginDTO;
        } else {
            String username = kakaoUserDTO.getKakaoAccount().getProfile().getNickname();
            if (username.getBytes().length > 6) {
                username = username.substring(0, 3);
            }
            User newUser = User.builder()
                .username(username)
                .authenticationCode(kakaoUserDTO.getAuthenticationCode())
                .provider(provider).refreshToken("")
                .build();
            uRepo.save(newUser);

            LoginDTO loginDTO = this.issueNewTokens(newUser, provider);

            return loginDTO;
        }
    }


    @Override
    @Transactional
    public LoginDTO loginWithAppleAuthenticationCode(String authenticationCode,
        AppleRequest appleRequest) {
        String provider = "apple";
        Optional<User> user = uRepo.findByAuthenticationCode(authenticationCode);
        if (user.isPresent()) {
            LoginDTO loginDTO = this.issueNewTokens(user.get(), provider);

            return loginDTO;
        } else {
            String username = appleRequest.getUsername();
            if (username.getBytes().length > 6) {
                username = username.substring(0, 3);
            }
            User newUser = User.builder()
                .username(username)
                .authenticationCode(authenticationCode)
                .provider("apple").refreshToken("")
                .build();
            uRepo.save(newUser);

            LoginDTO loginDTO = this.issueNewTokens(newUser, provider);

            return loginDTO;
        }
    }

    @Override
    @Transactional
    public UserDTO updateUserType(User user, UserTypeRequest userTypeRequest) {

        Calendar cal = Calendar.getInstance();
        Integer currYear = cal.get(Calendar.YEAR);
        Integer birthYear = Integer.parseInt(userTypeRequest.getBirthday()) / 10000;
        if (user.getIsFemale() != null) {
            throw new BadRequestException(ErrorCode.USER_ALREADY_HAS_TYPE.getErrorCode());
        } else if (birthYear >= currYear || birthYear <= currYear - 100) {
            throw new BadRequestException(ErrorCode.INVALID_BIRTHDAY.getErrorCode());
        } else {
            user.setBirthday(userTypeRequest.getBirthday());
            user.setIsFemale(userTypeRequest.getIsFemale());
            user.setIsKid(userTypeRequest.getIsKid());
            uRepo.save(user);

            if (user.getIsKid() == true) {
                Kid newKid = Kid.builder()
                    .savings(0L)
                    .achievedChallenge(0L)
                    .totalChallenge(0L)
                    .level(1L)
                    .user(user)
                    .build();
                kRepo.save(newKid);
            } else {
                Parent newParent = Parent.builder()
                    .acceptedRequest(0L)
                    .totalRequest(0L)
                    .user(user)
                    .build();
                pRepo.save(newParent);
            }
            UserDTO userDTO = new UserDTO(user);
            return userDTO;
        }
    }

    @Override
    @Transactional
    public LoginDTO issueNewTokens(User user, String provider) {
        String newRefreshToken = jwtTokenServiceImpl.encodeJwtRefreshToken(user.getId());
        user.setRefreshToken(newRefreshToken);
        uRepo.save(user);

        TokenDTO tokenDTO = new TokenDTO(user);

        LoginDTO loginDTO;
        if (user.getIsKid() == null || user.getIsKid() == false) {
            loginDTO = new LoginDTO(user.getIsKid(),
                jwtTokenServiceImpl.encodeJwtToken(tokenDTO), provider);
        } else {
            loginDTO = new LoginDTO(user.getIsKid(),
                jwtTokenServiceImpl.encodeJwtToken(tokenDTO),
                user.getKid().getLevel(), provider);
        }
        return loginDTO;
    }

    @Override
    @Transactional
    public void setNewCookie(User user, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", user.getRefreshToken());
        cookie.setMaxAge(14 * 24 * 60 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    @Override
    @Transactional
    public User getUserByRefreshToken(String refreshToken) {
        String userId = jwtTokenServiceImpl.getUserIdFromJwtToken(refreshToken);
        Optional<User> user = uRepo.findById(Long.parseLong(userId));
        return user.get();
    }

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
    public UserDTO updateUserLogout(User user) {
        user.setRefreshToken("");
        user.setExpoToken("");
        uRepo.save(user);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(14 * 24 * 60 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return null;
    }

    @Override
    @Transactional
    public UserDTO deleteUser(User user) {
        UserDTO userDTO = new UserDTO(user);
        uRepo.delete(user);
        return userDTO;
    }

    @Override
    @Transactional
    public User updateUserExpoToken(User user, ExpoRequest expoRequest) {
        user.setExpoToken(expoRequest.getExpoToken());
        uRepo.save(user);

        return user;
    }

    @Override
    @Transactional
    public OptInDTO updateNoticeOptIn(User user) {
        user.setNoticeOptIn(!user.getNoticeOptIn());
        uRepo.save(user);

        return new OptInDTO(user);
    }

    @Override
    @Transactional
    public OptInDTO updateServiceOptIn(User user) {
        user.setServiceOptIn(!user.getServiceOptIn());
        uRepo.save(user);

        return new OptInDTO(user);
    }
}
