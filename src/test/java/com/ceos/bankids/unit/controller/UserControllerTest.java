package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.NotificationController;
import com.ceos.bankids.controller.UserController;
import com.ceos.bankids.controller.request.ExpoRequest;
import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.controller.request.UserTypeRequest;
import com.ceos.bankids.controller.request.WithdrawalRequest;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.KidBackup;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.ParentBackup;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidDTO;
import com.ceos.bankids.dto.LoginDTO;
import com.ceos.bankids.dto.MyPageDTO;
import com.ceos.bankids.dto.ParentDTO;
import com.ceos.bankids.dto.TokenDTO;
import com.ceos.bankids.dto.UserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.FamilyRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.KidBackupRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentBackupRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.JwtTokenServiceImpl;
import com.ceos.bankids.service.KidBackupServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentBackupServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import com.ceos.bankids.service.SlackServiceImpl;
import com.ceos.bankids.service.UserServiceImpl;
import java.util.ArrayList;
import java.util.List;
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );
        CommonResponse result = userController.refreshUserToken("rT", response);

        // then
        LoginDTO loginDTO = new LoginDTO(false, "aT", user.getProvider());
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.refreshUserToken("rT", response);

        // then
        LoginDTO loginDTO = new LoginDTO(true, "aT", 1L, user.getProvider());
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.refreshUserToken("rT", response);

        // then
        LoginDTO loginDTO = new LoginDTO(null, "aT", user.getProvider());
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
            .acceptedRequest(0L)
            .totalRequest(0L)
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            userController.getUserInfo(user);
        });
    }

    @Test
    @DisplayName("유저 로그아웃 성공 시, null 반환하는지 확인")
    public void testIfUserLogoutSucceedThenReturnResult() {
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

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
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
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.patchUserLogout(user);

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(mockUserRepository, Mockito.times(1)).save(uCaptor.capture());
        user.setRefreshToken("");
        user.setExpoToken("");
        Assertions.assertEquals(user.getRefreshToken(), uCaptor.getValue().getRefreshToken());
        Assertions.assertEquals(user.getExpoToken(), uCaptor.getValue().getExpoToken());

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("가족 없는 부모 탈퇴 성공 시, 삭제 유저 반환하는지 확인")
    public void testIfParentUserWithoutFamilyDeleteAccountSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .birthday("12345678")
            .provider("kakao")
            .isKid(false)
            .refreshToken("token")
            .build();

        Parent parent = Parent.builder().id(1L).acceptedRequest(0L).totalRequest(0L).user(user1)
            .build();
        user1.setParent(parent);
        ParentBackup parentBackup = ParentBackup.builder()
            .birthYear(user1.getBirthday().substring(0, 4))
            .isKid(user1.getIsKid())
            .acceptedRequest(user1.getParent().getAcceptedRequest())
            .totalRequest(user1.getParent().getTotalRequest())
            .build();
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest("탈퇴맨!");

        // mock
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);

        ParentBackupRepository mockParentBackupRepository = Mockito.mock(
            ParentBackupRepository.class);

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        NotificationController mockNotificationController = Mockito.mock(
            NotificationController.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository,
            mockNotificationController
        );
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = new ParentBackupServiceImpl(
            mockParentBackupRepository);
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        SlackServiceImpl slackService = Mockito.mock(SlackServiceImpl.class);
        Mockito.doNothing().when(slackService)
            .sendWithdrawalMessage("ParentBackup ", parentBackup.getId(),
                withdrawalRequest.getMessage());

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.deleteUserAccount(user1, withdrawalRequest);

        ArgumentCaptor<ParentBackup> parentBackupCaptor = ArgumentCaptor.forClass(
            ParentBackup.class);
        Mockito.verify(mockParentBackupRepository, Mockito.times(1))
            .save(parentBackupCaptor.capture());
        Assertions.assertEquals(parentBackup, parentBackupCaptor.getValue());

        ArgumentCaptor<Parent> parentCaptor = ArgumentCaptor.forClass(
            Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1))
            .delete(parentCaptor.capture());
        Assertions.assertEquals(user1.getParent(), parentCaptor.getValue());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(
            User.class);
        Mockito.verify(mockUserRepository, Mockito.times(1))
            .delete(userCaptor.capture());
        Assertions.assertEquals(user1, userCaptor.getValue());

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(new UserDTO(user1)), result);
    }

    @Test
    @DisplayName("가족 없는 자녀 탈퇴 성공 시, 삭제 유저 반환하는지 확인")
    public void testIfKidUserWithoutFamilyDeleteAccountSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .birthday("12345678")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(0L)
            .user(user1)
            .build();
        user1.setKid(kid);
        KidBackup kidBackup = KidBackup.builder()
            .birthYear(user1.getBirthday().substring(0, 4))
            .isKid(user1.getIsKid())
            .savings(user1.getKid().getSavings())
            .achievedChallenge(user1.getKid().getAchievedChallenge())
            .totalChallenge(user1.getKid().getTotalChallenge())
            .level(user1.getKid().getLevel())
            .build();
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest("탈퇴맨!");

        // mock
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);

        KidBackupRepository mockKidBackupRepository = Mockito.mock(
            KidBackupRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        NotificationController mockNotificationController = Mockito.mock(
            NotificationController.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository,
            mockNotificationController
        );
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = new KidBackupServiceImpl(mockKidBackupRepository);
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository);
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = Mockito.mock(SlackServiceImpl.class);
        Mockito.doNothing().when(slackService)
            .sendWithdrawalMessage("KidBackup ", kidBackup.getId(),
                withdrawalRequest.getMessage());

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.deleteUserAccount(user1, withdrawalRequest);

        ArgumentCaptor<KidBackup> kidBackupCaptor = ArgumentCaptor.forClass(
            KidBackup.class);
        Mockito.verify(mockKidBackupRepository, Mockito.times(1))
            .save(kidBackupCaptor.capture());
        Assertions.assertEquals(kidBackup, kidBackupCaptor.getValue());

        ArgumentCaptor<Kid> kidCaptor = ArgumentCaptor.forClass(
            Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(1))
            .delete(kidCaptor.capture());
        Assertions.assertEquals(user1.getKid(), kidCaptor.getValue());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(
            User.class);
        Mockito.verify(mockUserRepository, Mockito.times(1))
            .delete(userCaptor.capture());
        Assertions.assertEquals(user1, userCaptor.getValue());

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(new UserDTO(user1)), result);
    }

    @Test
    @DisplayName("가족 있는 부모 탈퇴 성공 시, 삭제 유저 반환하는지 확인")
    public void testIfParentUserWithFamilyDeleteAccountSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .birthday("12345678")
            .provider("kakao")
            .isKid(false)
            .refreshToken("token")
            .build();

        Parent parent = Parent.builder().id(1L).acceptedRequest(0L).totalRequest(0L).user(user1)
            .build();
        user1.setParent(parent);
        ParentBackup parentBackup = ParentBackup.builder()
            .birthYear(user1.getBirthday().substring(0, 4))
            .isKid(user1.getIsKid())
            .acceptedRequest(user1.getParent().getAcceptedRequest())
            .totalRequest(user1.getParent().getTotalRequest())
            .build();

        Family family = Family.builder().id(1L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        FamilyRequest familyRequest = new FamilyRequest("test");
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest("탈퇴맨!");

        // mock
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));

        ParentBackupRepository mockParentBackupRepository = Mockito.mock(
            ParentBackupRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        NotificationController mockNotificationController = Mockito.mock(
            NotificationController.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository,
            mockNotificationController
        );
        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = new ParentBackupServiceImpl(
            mockParentBackupRepository);
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        SlackServiceImpl slackService = Mockito.mock(SlackServiceImpl.class);
        Mockito.doNothing().when(slackService)
            .sendWithdrawalMessage("ParentBackup ", parentBackup.getId(),
                withdrawalRequest.getMessage());
        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.deleteUserAccount(user1, withdrawalRequest);

        ArgumentCaptor<FamilyUser> familyUserCaptor = ArgumentCaptor.forClass(
            FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1))
            .delete(familyUserCaptor.capture());
        Assertions.assertEquals(familyUser1, familyUserCaptor.getValue());

        ArgumentCaptor<Family> familyCaptor = ArgumentCaptor.forClass(
            Family.class);
        Mockito.verify(mockFamilyRepository, Mockito.times(1))
            .delete(familyCaptor.capture());
        Assertions.assertEquals(family, familyCaptor.getValue());

        ArgumentCaptor<ParentBackup> parentBackupCaptor = ArgumentCaptor.forClass(
            ParentBackup.class);
        Mockito.verify(mockParentBackupRepository, Mockito.times(1))
            .save(parentBackupCaptor.capture());
        Assertions.assertEquals(parentBackup, parentBackupCaptor.getValue());

        ArgumentCaptor<Parent> parentCaptor = ArgumentCaptor.forClass(
            Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1))
            .delete(parentCaptor.capture());
        Assertions.assertEquals(user1.getParent(), parentCaptor.getValue());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(
            User.class);
        Mockito.verify(mockUserRepository, Mockito.times(1))
            .delete(userCaptor.capture());
        Assertions.assertEquals(user1, userCaptor.getValue());

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(new UserDTO(user1)), result);
    }

    @Test
    @DisplayName("가족 있는 자녀 탈퇴 성공 시, 삭제 유저 반환하는지 확인")
    public void testIfKidUserWithFamilyDeleteAccountSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .birthday("12345678")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(0L)
            .user(user1)
            .build();
        user1.setKid(kid);
        KidBackup kidBackup = KidBackup.builder()
            .birthYear(user1.getBirthday().substring(0, 4))
            .isKid(user1.getIsKid())
            .savings(user1.getKid().getSavings())
            .achievedChallenge(user1.getKid().getAchievedChallenge())
            .totalChallenge(user1.getKid().getTotalChallenge())
            .level(user1.getKid().getLevel())
            .build();

        Family family = Family.builder().id(1L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        FamilyRequest familyRequest = new FamilyRequest("test");
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest("탈퇴맨!");

        // mock
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));

        KidBackupRepository mockKidBackupRepository = Mockito.mock(
            KidBackupRepository.class);
        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);
        NotificationController mockNotificationController = Mockito.mock(
            NotificationController.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository,
            mockNotificationController
        );
        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        KidBackupServiceImpl kidBackupService = new KidBackupServiceImpl(mockKidBackupRepository);
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository);
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = Mockito.mock(SlackServiceImpl.class);
        Mockito.doNothing().when(slackService)
            .sendWithdrawalMessage("KidBackup ", kidBackup.getId(),
                withdrawalRequest.getMessage());

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.deleteUserAccount(user1, withdrawalRequest);

        ArgumentCaptor<FamilyUser> familyUserCaptor = ArgumentCaptor.forClass(
            FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1))
            .delete(familyUserCaptor.capture());
        Assertions.assertEquals(familyUser1, familyUserCaptor.getValue());

        ArgumentCaptor<Family> familyCaptor = ArgumentCaptor.forClass(
            Family.class);
        Mockito.verify(mockFamilyRepository, Mockito.times(1))
            .delete(familyCaptor.capture());
        Assertions.assertEquals(family, familyCaptor.getValue());

        ArgumentCaptor<KidBackup> kidBackupCaptor = ArgumentCaptor.forClass(
            KidBackup.class);
        Mockito.verify(mockKidBackupRepository, Mockito.times(1))
            .save(kidBackupCaptor.capture());
        Assertions.assertEquals(kidBackup, kidBackupCaptor.getValue());

        ArgumentCaptor<Kid> kidCaptor = ArgumentCaptor.forClass(
            Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(1))
            .delete(kidCaptor.capture());
        Assertions.assertEquals(user1.getKid(), kidCaptor.getValue());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(
            User.class);
        Mockito.verify(mockUserRepository, Mockito.times(1))
            .delete(userCaptor.capture());
        Assertions.assertEquals(user1, userCaptor.getValue());

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(new UserDTO(user1)), result);
    }

    @Test
    @DisplayName("유저 엑스포 토큰 패치 성공 시, null 반환하는지 확인")
    public void testIfUserExpoTokenPatchSucceedThenReturnResult() {
        // given
        User user = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .expoToken("ExponentPushToken[dd]")
            .build();
        ExpoRequest expoRequest = new ExpoRequest("ExponentPushToken[dd]");

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        JwtTokenServiceImpl jwtTokenServiceImpl = Mockito.mock(JwtTokenServiceImpl.class);

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // when
        UserServiceImpl userService = new UserServiceImpl(
            mockUserRepository,
            mockKidRepository,
            mockParentRepository,
            jwtTokenServiceImpl
        );
        FamilyServiceImpl familyService = null;
        ChallengeServiceImpl challengeService = null;
        KidBackupServiceImpl kidBackupService = null;
        ParentBackupServiceImpl parentBackupService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        SlackServiceImpl slackService = null;

        UserController userController = new UserController(
            userService,
            familyService,
            challengeService,
            kidBackupService,
            parentBackupService,
            kidService,
            parentService,
            slackService
        );

        CommonResponse result = userController.patchExpoToken(user, expoRequest, response);

        ArgumentCaptor<User> uCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(mockUserRepository, Mockito.times(1)).save(uCaptor.capture());
        Assertions.assertEquals(user.getExpoToken(), uCaptor.getValue().getExpoToken());

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }
}
