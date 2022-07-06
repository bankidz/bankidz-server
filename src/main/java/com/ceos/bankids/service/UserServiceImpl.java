package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import java.util.Optional;
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

    @Override
    @Transactional
    public UserDTO updateUserType(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody UserTypeRequest userTypeRequest) {
        Long userId = authUser.getId();
        Optional<User> user = uRepo.findById(userId);
        if (user.isEmpty()) {
            throw new BadRequestException("존재하지 않는 유저입니다.");
        } else {
            user.get().setBirthday(userTypeRequest.getBirthday());
            user.get().setIsFemale(userTypeRequest.getIsFemale());
            user.get().setIsKid(userTypeRequest.getIsKid());
            uRepo.save(user.get());

            if (user.get().getIsKid()) {
                Kid newKid = Kid.builder()
                    .savings(0L)
                    .user(user.get())
                    .build();
                kRepo.save(newKid);
            } else {
                System.out.println("PARENT!!!");
                Parent newParent = Parent.builder()
                    .user(user.get())
                    .build();
                pRepo.save(newParent);
            }
            UserDTO userDTO = new UserDTO(user.get());
            return userDTO;
        }
    }
}
