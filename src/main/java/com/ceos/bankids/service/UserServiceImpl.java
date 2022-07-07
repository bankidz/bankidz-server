package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
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
        if (user.isEmpty()) {
            throw new BadRequestException("존재하지 않는 유저입니다.");
        } else if (user.get().getIsFemale() != null) {
            throw new BadRequestException("이미 유저 타입을 선택한 유저입니다.");
        } else {
            user.get().setBirthday(userTypeRequest.getBirthday());
            user.get().setIsFemale(userTypeRequest.getIsFemale());
            user.get().setIsKid(userTypeRequest.getIsKid());
            uRepo.save(user.get());

            if (user.get().getIsKid() == true) {
                Kid newKid = Kid.builder()
                    .savings(0L)
                    .user(user.get())
                    .level(1L)
                    .build();
                kRepo.save(newKid);
            } else if (user.get().getIsKid() == false) {
                Parent newParent = Parent.builder()
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
    public String issueNewTokens(User user, String prevRefreshToken,
        HttpServletResponse response) {
        TokenDTO tokenDTO = new TokenDTO(user);

        String newRefreshToken = jwtTokenServiceImpl.encodeJwtRefreshToken(user.getId());
        user.setRefreshToken(newRefreshToken);
        uRepo.save(user);

        Cookie cookie = new Cookie("refreshToken", user.getRefreshToken());

        cookie.setMaxAge(14 * 24 * 60 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);

        return jwtTokenServiceImpl.encodeJwtToken(tokenDTO);
    }
}
