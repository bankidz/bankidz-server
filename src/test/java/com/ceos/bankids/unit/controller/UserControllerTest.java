package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.UserController;
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
import com.ceos.bankids.service.JwtTokenServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class UserControllerTest {

    @Test
    @DisplayName("유저 타입 패치 성공시, 결과 반환 하는지 확인")
    public void testIfUserTypePatchSucceedReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("19990521", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );
        CommonResponse<UserDTO> result = userController.patchUserType(user, userTypeRequest);

        // then
        user.setBirthday("19990521");
        user.setIsFemale(false);
        user.setIsKid(true);
        UserDTO userDTO = new UserDTO(user);
        Assertions.assertEquals(CommonResponse.onSuccess(userDTO), result);
    }

    @Test
    @DisplayName("유저 정보 이미 있어 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWhenAlreadyPatchedThrowBadRequestException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("19990521", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.patchUserType(user, userTypeRequest);
        });
    }

    @Test
    @DisplayName("body 없어서 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithoutArgumentsThrowNullPointerException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("19990521", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        // then
        Assertions.assertThrows(NullPointerException.class, () -> {
            userController.patchUserType(user, null);
        });
    }

    @Test
    @DisplayName("인증 유저 없어서 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithoutValidUserThrowBadRequestException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("19990521", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(null));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.patchUserType(user, userTypeRequest);
        });
    }

    @Test
    @DisplayName("100세 초과 생년월일 입력으로 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithTooEarlyBirthdayThrowBadRequestException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("18880818", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.patchUserType(user, userTypeRequest);
        });
    }

    @Test
    @DisplayName("0세 미만 생년월일 입력으로 패치 실패시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWithTooLateBirthdayThrowBadRequestException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("23450818", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.patchUserType(user, userTypeRequest);
        });
    }

    @Test
    @DisplayName("자녀 지정시, 자녀 row 생성 확인")
    public void testIfKidInsertSucceedWhenUserTypePatchSucceed() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        Kid kid = Kid.builder()
            .savings(0L)
            .user(user)
            .level(1L)
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("19990521", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        Mockito.when(mockUserRepository.save(user)).thenReturn(user);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        Mockito.when(mockKidRepository.save(kid)).thenReturn(kid);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );
        CommonResponse result = userController.patchUserType(user, userTypeRequest);

        // then
        user.setBirthday("19990521");
        user.setIsFemale(false);
        user.setIsKid(true);
        UserDTO userDTO = new UserDTO(user);

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockUserRepository, Mockito.times(1)).save(uCaptor.capture());
        Mockito.verify(mockKidRepository, Mockito.times(1)).save(kCaptor.capture());

        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(kid, kCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(userDTO), result);
    }

    @Test
    @DisplayName("부모 지정시, 부모 row 생성 확인")
    public void testIfParentInsertSucceedWhenUserTypePatchSucceed() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        Parent parent = Parent.builder()
            .user(user)
            .savings(0L)
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("19990521", true, false);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        Mockito.when(mockUserRepository.save(user)).thenReturn(user);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        Mockito.when(mockParentRepository.save(parent)).thenReturn(parent);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );
        CommonResponse result = userController.patchUserType(user, userTypeRequest);

        // then
        user.setBirthday("19990521");
        user.setIsFemale(true);
        user.setIsKid(false);
        UserDTO userDTO = new UserDTO(user);

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockUserRepository, Mockito.times(1)).save(uCaptor.capture());
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());

        Assertions.assertEquals(user, uCaptor.getValue());
        Assertions.assertEquals(parent, pCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(userDTO), result);
    }

    @Test
    @DisplayName("이미 타입을 선택한 유저 접근시, 에러 처리 되는지 확인")
    public void testIfUserTypePatchFailWhenAlreadyRegisteredThrowBadRequestException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(false)
            .refreshToken("token")
            .build();
        UserTypeRequest userTypeRequest = new UserTypeRequest("19990521", false, true);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(null));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.patchUserType(user, userTypeRequest);
        });
    }

    @Test
    @DisplayName("부모 유저 토큰과 쿠키 정상 입력 시, 성공 결과 반환하는지 확인")
    public void testIfParentTokenRefreshSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(false)
            .refreshToken("token")
            .build();
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        TokenDTO tokenDTO = new TokenDTO(user);
        Mockito.when(jwtTokenServiceImpl.encodeJwtRefreshToken(1L)).thenReturn("rT");
        Mockito.when(jwtTokenServiceImpl.encodeJwtToken(tokenDTO)).thenReturn("aT");
        Mockito.when(jwtTokenServiceImpl.getUserIdFromJwtToken("rT")).thenReturn("1");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        CommonResponse result = userController.refreshUserToken("rT", response);

        // then
        LoginDTO loginDTO = new LoginDTO(false, "aT");
        Assertions.assertEquals(CommonResponse.onSuccess(loginDTO), result);
    }

    @Test
    @DisplayName("자녀 유저 토큰과 쿠키 정상 입력 시, 성공 결과 반환하는지 확인")
    public void testIfKidTokenRefreshSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        Kid kid = Kid.builder()
            .level(1L)
            .user(user)
            .build();
        user.setKid(kid);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        TokenDTO tokenDTO = new TokenDTO(user);
        Mockito.when(jwtTokenServiceImpl.encodeJwtRefreshToken(1L)).thenReturn("rT");
        Mockito.when(jwtTokenServiceImpl.encodeJwtToken(tokenDTO)).thenReturn("aT");
        Mockito.when(jwtTokenServiceImpl.getUserIdFromJwtToken("rT")).thenReturn("1");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        CommonResponse result = userController.refreshUserToken("rT", response);

        // then
        LoginDTO loginDTO = new LoginDTO(true, "aT", 1L);
        Assertions.assertEquals(CommonResponse.onSuccess(loginDTO), result);
    }

    @Test
    @DisplayName("타입 미선택 유저 토큰과 쿠키 정상 입력 시, 성공 결과 반환하는지 확인")
    public void testIfUserTokenRefreshSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(null)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(null)
            .refreshToken("token")
            .build();

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        TokenDTO tokenDTO = new TokenDTO(user);
        Mockito.when(jwtTokenServiceImpl.encodeJwtRefreshToken(1L)).thenReturn("rT");
        Mockito.when(jwtTokenServiceImpl.encodeJwtToken(tokenDTO)).thenReturn("aT");
        Mockito.when(jwtTokenServiceImpl.getUserIdFromJwtToken("rT")).thenReturn("1");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        CommonResponse result = userController.refreshUserToken("rT", response);

        // then
        LoginDTO loginDTO = new LoginDTO(null, "aT");
        Assertions.assertEquals(CommonResponse.onSuccess(loginDTO), result);
    }

    @Test
    @DisplayName("부모 유저 정보 조회 성공 시, 부모 결과 반환하는지 확인")
    public void testIfGetParentInfoSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(false)
            .refreshToken("token")
            .build();
        Parent parent = Parent.builder()
            .totalChallenge(0L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .savings(0L)
            .user(user)
            .build();
        user.setParent(parent);

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        CommonResponse result = userController.getUserInfo(user);

        // then
        MyPageDTO myPageDTO = new MyPageDTO(new UserDTO(user), new ParentDTO(parent));
        Assertions.assertEquals(CommonResponse.onSuccess(myPageDTO), result);
    }

    @Test
    @DisplayName("자녀 유저 정보 조회 성공 시, 자녀 결과 반환하는지 확인")
    public void testIfGetKidInfoSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();
        Kid kid = Kid.builder()
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user)
            .build();
        user.setKid(kid);

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        CommonResponse result = userController.getUserInfo(user);

        // then
        MyPageDTO myPageDTO = new MyPageDTO(new UserDTO(user), new KidDTO(kid));
        Assertions.assertEquals(CommonResponse.onSuccess(myPageDTO), result);
    }

    @Test
    @DisplayName("유저 타입 선택 없이 유저 정보 조회 시, 에러 처리하는지 확인")
    public void testIfGetUserInfoFailWithoutUserTypeThenThrowBadRequestException() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(null)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(null)
            .refreshToken("token")
            .build();
        Kid kid = Kid.builder()
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user)
            .build();
        user.setKid(kid);

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findById(1L))
            .thenReturn(Optional.ofNullable(user));
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        UserController userController = new UserController(
            userService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.getUserInfo(user);
        });
    }
}
