package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidDTO;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.ParentDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import java.util.Calendar;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository uRepo;
    private final KidRepository kRepo;
    private final ParentRepository pRepo;
    private final JwtTokenServiceImpl jwtTokenServiceImpl;

    @Override
    @Transactional
    public UserDTO updateUserType(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody UserTypeRequest userTypeRequest) {
        Long userId = authUser.getId();
        Optional<User> user = uRepo.findById(userId);

        Calendar cal = Calendar.getInstance();
        Integer currYear = cal.get(Calendar.YEAR);
        Integer birthYear = Integer.parseInt(userTypeRequest.getBirthday()) / 10000;
        if (user.isEmpty()) {
            throw new BadRequestException("존재하지 않는 유저입니다.");
        } else if (user.get().getIsFemale() != null) {
            throw new BadRequestException("이미 유저 타입을 선택한 유저입니다.");
        } else if (birthYear > currYear || birthYear <= currYear - 100) {
            throw new BadRequestException("유효하지 않은 생년월일입니다.");
        } else {
            user.get().setBirthday(userTypeRequest.getBirthday());
            user.get().setIsFemale(userTypeRequest.getIsFemale());
            user.get().setIsKid(userTypeRequest.getIsKid());
            uRepo.save(user.get());

            if (user.get().getIsKid() == true) {
                Kid newKid = Kid.builder()
                    .savings(0L)
                    .achievedChallenge(0L)
                    .totalChallenge(0L)
                    .level(1L)
                    .user(user.get())
                    .build();
                kRepo.save(newKid);
            } else {
                Parent newParent = Parent.builder()
                    .totalChallenge(0L)
                    .acceptedRequest(0L)
                    .totalRequest(0L)
                    .savings(0L)
                    .user(user.get())
                    .build();
                pRepo.save(newParent);
            }
            UserDTO userDTO = new UserDTO(user.get());
            return userDTO;
        }
    }

    @Override
    @Transactional
    public LoginDTO issueNewTokens(User user, HttpServletResponse response) {
        String newRefreshToken = jwtTokenServiceImpl.encodeJwtRefreshToken(user.getId());
        user.setRefreshToken(newRefreshToken);
        uRepo.save(user);

        TokenDTO tokenDTO = new TokenDTO(user);

        Cookie cookie = new Cookie("refreshToken", user.getRefreshToken());
        cookie.setMaxAge(14 * 24 * 60 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);

        LoginDTO loginDTO;
        if (user.getIsKid() == null || user.getIsKid() == false) {
            loginDTO = new LoginDTO(user.getIsKid(),
                jwtTokenServiceImpl.encodeJwtToken(tokenDTO));
        } else {
            loginDTO = new LoginDTO(user.getIsKid(),
                jwtTokenServiceImpl.encodeJwtToken(tokenDTO),
                user.getKid().getLevel());
        }
        return loginDTO;
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
            throw new BadRequestException("유저 타입이 선택되지 않은 유저입니다.");
        } else if (user.getIsKid() == true) {
            KidDTO kidDTO = new KidDTO(user.getKid());
            myPageDTO = new MyPageDTO(userDTO, kidDTO);
        } else {
            ParentDTO parentDTO = new ParentDTO(user.getParent());
            myPageDTO = new MyPageDTO(userDTO, parentDTO);
        }
        return myPageDTO;
    }
}
