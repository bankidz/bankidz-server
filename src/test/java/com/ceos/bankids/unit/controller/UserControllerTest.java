package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.UserController;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

public class UserControllerTest {

    @Test
    @DisplayName("유저 타입 패치 성공시, 결과 반환 하는지 확인")
    public void testIfUserTypePatchSuccessReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(false, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));

        // when
        UserController userController = new UserController(
            mockUserRepository
        );
        CommonResponse<UserDTO> result = userController.patchUserType(user, userTypeRequest);

        // then
        user.setIsKid(false);
        user.setIsFemale(false);
        UserDTO userDTO = new UserDTO(user);
        Assertions.assertEquals(CommonResponse.onSuccess(userDTO), result);
    }

    @Test
    @DisplayName("body 없어서 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithoutArgumentsThrowNullPointerException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(false, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));

        // when
        UserController userController = new UserController(
            mockUserRepository
        );

        // then
        Assertions.assertThrows(NullPointerException.class, () -> {
            userController.patchUserType(user, null);
        });
    }

    @Test
    @DisplayName("인증 유저 없어서 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithoutValidUserThrowNullPointerException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest(false, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(null));

        // when
        UserController userController = new UserController(
            mockUserRepository
        );
        CommonResponse result = userController.patchUserType(user, userTypeRequest);
    
        // then
        Assertions.assertEquals(CommonResponse.onFailure(HttpStatus.BAD_REQUEST, "존재하지 않는 유저입니다."),
            result);
    }

//    @Test
//    @DisplayName("인증 유저 없어서 패치 실패시, 에러 처리 되는지 확인")
//    public void testIfUserTypePatchFailWithoutValidUserThrowNullPointerException() {
//        // given
//        UserTypeRequest userTypeRequest = new UserTypeRequest(false, false);
//        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
//        Mockito.when(mockUserRepository.findById(1L))
//            .thenReturn(null);
//
//        // when
//        UserController userController = new UserController(
//            mockUserRepository
//        );
//
//        // then
//        Assertions.assertThrows(NullPointerException.class, () -> {
//            userController.patchUserType(null, userTypeRequest);
//        });
//    }
}
