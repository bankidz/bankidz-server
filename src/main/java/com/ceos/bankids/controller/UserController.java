package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import java.util.Optional;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Log
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository uRepo;
    private final KidRepository kRepo;
    private final ParentRepository pRepo;

    @PatchMapping(value = "", produces = "application/json; charset=utf-8")
    @ResponseBody
    public CommonResponse patchUserType(@AuthenticationPrincipal User authUser,
        @Valid @RequestBody UserTypeRequest userTypeRequest) {

        Long userId = authUser.getId();
        Optional<User> user = uRepo.findById(userId);

        if (user.isEmpty()) {
            return CommonResponse.onFailure("존재하지 않는 유저입니다.");
        } else {
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
                Parent newParent = Parent.builder()
                    .user(user.get())
                    .build();
                pRepo.save(newParent);
            }

            UserDTO userDTO = new UserDTO(user.get());
            return CommonResponse.onSuccess(userDTO);
        }
    }
}
