package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.controller.FamilyController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Comment;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.dto.KidListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.mapper.FamilyMapper;
import com.ceos.bankids.mapper.NotificationMapper;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.NotificationRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.ChallengeUserServiceImpl;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class FamilyControllerTest {

    @Test
    @DisplayName("생성 시 기존 가족 있으나, 삭제되었을 때 에러 처리 하는지 확인")
    public void testIfFamilyExistedButDeletedWhenPostThenThrowBadRequestException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser2);

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(null));
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.postNewFamily(user1);
        });
    }

    @Test
    @DisplayName("생성 시 기존 가족 있을 때, 에러 처리 하는지 확인")
    public void testIfFamilyExistThenThrowBadRequestException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser2);

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, user1))
            .thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.postNewFamily(user1);
        });
    }

    @Test
    @DisplayName("생성 시 기존 가족 없을 때, 가족 생성 후 정보 반환하는지 확인")
    public void testIfFamilyNotExistThenPostAndReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();

        Family family = Family.builder().code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<>();

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(null));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, user1))
            .thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.postNewFamily(user1);
        String code = result.getData().getCode();
        family.setCode(code);

        // then
        ArgumentCaptor<Family> fCaptor = ArgumentCaptor.forClass(Family.class);
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyRepository, Mockito.times(1)).save(fCaptor.capture());
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());

        Assertions.assertEquals(family, fCaptor.getValue());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        FamilyDTO familyDTO = FamilyDTO.builder()
            .family(family)
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();

        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("조회 시 기존 가족 있을 때, 가족 정보 반환하는지 확인")
    public void testIfFamilyExistThenReturnGetResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser2);

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, user1))
            .thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.getFamily(user1);

        // then
        FamilyDTO familyDTO = FamilyDTO.builder()
            .family(family)
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("조회 시 기존 가족 없을 때, 빈 가족 정보 리턴 하는지 확인")
    public void testIfFamilyNotExistThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser2);

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(null));
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.getFamily(user1);

        // then
        FamilyDTO familyDTO = FamilyDTO.builder()
            .family(new Family())
            .familyUserList(List.of()).build();
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("아이 조회 시 자녀일 경우, 에러 처리 하는지 확인")
    public void testIfAKidTryToGetKidListThenThrowForbiddenException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser2);

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(null));
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(ForbiddenException.class, () -> {
            familyController.getFamilyKidList(user1);
        });
    }

    @Test
    @DisplayName("아이 조회 시 결과 있을 때, 아이 리스트 정보 가나다순으로 반환하는지 확인")
    public void testIfFamilyExistThenReturnKidListResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("성우")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("민준")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user3 = User.builder()
            .id(3L)
            .username("어진")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user4 = User.builder()
            .id(4L)
            .username("규진")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        Kid kid2 = Kid.builder().level(1L).user(user2).build();
        Kid kid3 = Kid.builder().level(2L).user(user3).build();
        Kid kid4 = Kid.builder().level(3L).user(user4).build();
        user2.setKid(kid2);
        user3.setKid(kid3);
        user4.setKid(kid4);
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(user3).family(family).build();
        FamilyUser familyUser4 = FamilyUser.builder().user(user4).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser4);
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<List<KidListDTO>> result = familyController.getFamilyKidList(user1);

        // then
        List<KidListDTO> kidListDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .filter(User::getIsKid).map(KidListDTO::new).collect(
                Collectors.toList());
        Assertions.assertEquals(CommonResponse.onSuccess(kidListDTOList), result);
    }

    @Test
    @DisplayName("아이 조회 시 결과 없을 때, 빈 리스트 반환하는지 확인")
    public void testIfFamilyExistButNotKidThenReturnEmptyList() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser2);

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<List<KidListDTO>> result = familyController.getFamilyKidList(user1);

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(new ArrayList()), result);
    }

    @Test
    @DisplayName("아이 조회 시 가족 없을 때, 빈 리스트 결과 반환 하는지 확인")
    public void testIfFamilyNotExistThenReturnEmptyListResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(null));
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<List<KidListDTO>> result = familyController.getFamilyKidList(user1);

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(new ArrayList()), result);
    }

    @Test
    @DisplayName("가족 참여 시 기존 가족 있을 때, 삭제 후 새 가족 참여하는지 확인")
    public void testIfLeaveFamilyAndJoinNewFamilyThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .serviceOptIn(false)
            .noticeOptIn(false)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .serviceOptIn(false)
            .build();

        Family family1 = Family.builder().id(1L).code("code").build();
        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family1).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family1));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.postFamilyUser(user1, familyRequest);
        String code = result.getData().getCode();
        family1.setCode(code);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());
        familyUser1.setFamily(family2);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);

        FamilyDTO familyDTO = FamilyDTO.builder()
            .family(family2)
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("가족 참여 시 기존 가족 없을 때, 새 가족 참여하는지 확인")
    public void testIfJoinNewFamilyThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.postFamilyUser(user1, familyRequest);
        String code = result.getData().getCode();
        family2.setCode(code);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        familyUser1.setFamily(family2);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        FamilyDTO familyDTO = FamilyDTO.builder()
            .family(family2)
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("가족 참여 시 해당 가족 구성원일 때, 에러 처리 하는지 확인")
    public void testIfUserIsAlreadyInFamilyThenThrowForbiddenException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser1);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(family2));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(ForbiddenException.class, () -> {
            familyController.postFamilyUser(user1, familyRequest);
        });
    }

    @Test
    @DisplayName("엄마로 가족 참여 시 엄마가 이미 존재할 때, 에러 처리 하는지 확인")
    public void testIfUserIsMomAndMomAlreadyInFamilyThenThrowForbiddenException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User user3 = User.builder()
            .id(3L)
            .username("user3")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();
        User user4 = User.builder()
            .id(4L)
            .username("user4")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User user5 = User.builder()
            .id(5L)
            .username("user5")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();

        Family family1 = Family.builder().id(1L).code("code").build();
        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family1).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(user3).family(family2).build();
        FamilyUser familyUser4 = FamilyUser.builder().user(user4).family(family2).build();
        FamilyUser familyUser5 = FamilyUser.builder().user(user5).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        familyUserList.add(familyUser4);
        familyUserList.add(familyUser5);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family1));
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(family2));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(ForbiddenException.class, () -> {
            familyController.postFamilyUser(user1, familyRequest);
        });
    }

    @Test
    @DisplayName("엄마로 가족 참여 시 엄마 존재하지 않을 때, 결과 반환 하는지 확인")
    public void testIfUserIsMomAndMomNotInFamilyThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .serviceOptIn(false)
            .noticeOptIn(false)
            .expoToken("ExponentPushToken[dfdf]")
            .build();
        User user3 = User.builder()
            .id(3L)
            .username("user3")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .serviceOptIn(true)
            .noticeOptIn(true)
            .expoToken("ExponentPushToken[dfdf]")
            .build();
        User user4 = User.builder()
            .id(4L)
            .username("user4")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .serviceOptIn(false)
            .noticeOptIn(true)
            .expoToken("ExponentPushToken[dfdf]")
            .build();
        User user5 = User.builder()
            .id(5L)
            .username("user5")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .serviceOptIn(true)
            .noticeOptIn(false)
            .expoToken("ExponentPushToken[dfdf]")
            .build();

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(user3).family(family2).build();
        FamilyUser familyUser4 = FamilyUser.builder().user(user4).family(family2).build();
        FamilyUser familyUser5 = FamilyUser.builder().user(user5).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser3);
        familyUserList.add(familyUser4);
        familyUserList.add(familyUser5);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.postFamilyUser(user1, familyRequest);
        String code = result.getData().getCode();
        family2.setCode(code);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        familyUser1.setFamily(family2);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        FamilyDTO familyDTO = FamilyDTO.builder()
            .family(family2)
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("아빠로 가족 참여 시 아빠가 이미 존재할 때, 에러 처리 하는지 확인")
    public void testIfUserIsDadAndDadAlreadyInFamilyThenThrowForbiddenException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .serviceOptIn(false)
            .noticeOptIn(true)
            .build();
        User user2 = User.builder()
            .id(2L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();
        User user3 = User.builder()
            .id(3L)
            .username("user3")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User user4 = User.builder()
            .id(4L)
            .username("user4")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User user5 = User.builder()
            .id(5L)
            .username("user5")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();

        Family family1 = Family.builder().id(1L).code("code").build();
        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family1).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(user3).family(family2).build();
        FamilyUser familyUser4 = FamilyUser.builder().user(user4).family(family2).build();
        FamilyUser familyUser5 = FamilyUser.builder().user(user5).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        familyUserList.add(familyUser4);
        familyUserList.add(familyUser5);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family1));
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(family2));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(ForbiddenException.class, () -> {
            familyController.postFamilyUser(user1, familyRequest);
        });
    }

    @Test
    @DisplayName("아빠로 가족 참여 시 아빠 존재하지 않을 때, 결과 반환 하는지 확인")
    public void testIfUserIsDadAndDadNotInFamilyThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .serviceOptIn(true)
            .noticeOptIn(true)
            .expoToken("ExponentPushToken[dfdf]")
            .build();
        User user3 = User.builder()
            .id(3L)
            .username("user3")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .serviceOptIn(false)
            .noticeOptIn(true)
            .expoToken("ExponentPushToken[dfdf]")
            .build();
        User user4 = User.builder()
            .id(4L)
            .username("user4")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .serviceOptIn(true)
            .noticeOptIn(false)
            .expoToken("ExponentPushToken[dfdf]")
            .build();
        User user5 = User.builder()
            .id(5L)
            .username("user5")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .serviceOptIn(false)
            .noticeOptIn(false)
            .expoToken("ExponentPushToken[dfdf]")
            .build();

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(user3).family(family2).build();
        FamilyUser familyUser4 = FamilyUser.builder().user(user4).family(family2).build();
        FamilyUser familyUser5 = FamilyUser.builder().user(user5).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser3);
        familyUserList.add(familyUser4);
        familyUserList.add(familyUser5);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.postFamilyUser(user1, familyRequest);
        String code = result.getData().getCode();
        family2.setCode(code);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        familyUser1.setFamily(family2);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        FamilyDTO familyDTO = FamilyDTO.builder()
            .family(family2)
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("가족 참여 시 참여하려는 가족이 없을 때, 에러 처리 하는지 확인")
    public void testIfFamilyToJoinNotExistThenThrowBadRequestException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();

        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(null));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(null));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.postFamilyUser(user1, familyRequest);
        });
    }

    @Test
    @DisplayName("예외) 부모로 가족 참여 시 성별 선택 안되었을 때, 에러 처리 하는지 확인")
    public void testIfUserIsParentButSexUnknownThenThrowBadRequestException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(null)
            .build();
        User user3 = User.builder()
            .id(3L)
            .username("user3")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User user4 = User.builder()
            .id(4L)
            .username("user4")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User user5 = User.builder()
            .id(5L)
            .username("user5")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(user3).family(family2).build();
        FamilyUser familyUser4 = FamilyUser.builder().user(user4).family(family2).build();
        FamilyUser familyUser5 = FamilyUser.builder().user(user5).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser3);
        familyUserList.add(familyUser4);
        familyUserList.add(familyUser5);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);
        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.postFamilyUser(user1, familyRequest);
        });
    }

    @Test
    @DisplayName("부모 가족 나가기 성공 시, 결과 반환 하는지 확인")
    public void testIfParentFamilyUserDeleteSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(null)
            .build();
        User mom = User.builder()
            .id(3L)
            .username("mom")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User dad = User.builder()
            .id(4L)
            .username("dad")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user1)
            .build();
        user1.setKid(kid);

        Parent parent = Parent.builder().id(1L).acceptedRequest(0L).totalRequest(0L).user(user1)
            .build();

        user1.setKid(kid);
        mom.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(mom).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(dad).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "테스트용 돈길",
            30L, 3000L, 50000L, 10000L, 3L, "test");
        ChallengeCategory challengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem targetItem = TargetItem.builder().id(1L).name("전자제품").build();
        Challenge walkingChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.WALKING)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .successWeeks(0L)
            .totalPrice(0L)
            .filename(challengeRequest.getFileName()).build();
        Challenge rejectedChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.REJECTED)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .successWeeks(0L)
            .totalPrice(0L)
            .filename(challengeRequest.getFileName()).build();
        Challenge pendingChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.PENDING)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .successWeeks(0L)
            .totalPrice(0L)
            .filename(challengeRequest.getFileName()).build();
        Challenge achievedChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.ACHIEVED)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .successWeeks(0L)
            .totalPrice(0L)
            .filename(challengeRequest.getFileName()).build();
        Comment comment = Comment.builder().id(1L).challenge(rejectedChallenge).user(mom)
            .content("아쉽구나").build();
        rejectedChallenge.setComment(comment);

        Progress progress1 = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(walkingChallenge).build();
        Progress progress2 = Progress.builder().id(2L).weeks(2L).isAchieved(false)
            .challenge(walkingChallenge).build();
        Progress progress3 = Progress.builder().id(3L).weeks(3L).isAchieved(false)
            .challenge(walkingChallenge).build();
        Progress progress4 = Progress.builder().id(4L).weeks(1L).isAchieved(true)
            .challenge(rejectedChallenge).build();
        Progress progress5 = Progress.builder().id(5L).weeks(2L).isAchieved(true)
            .challenge(rejectedChallenge).build();
        Progress progress6 = Progress.builder().id(6L).weeks(3L).isAchieved(true)
            .challenge(rejectedChallenge).build();
        List<Progress> progressList1 = new ArrayList<>();
        progressList1.add(progress1);
        progressList1.add(progress2);
        progressList1.add(progress3);
        List<Progress> progressList2 = new ArrayList<>();
        progressList2.add(progress4);
        progressList2.add(progress5);
        progressList2.add(progress6);
        walkingChallenge.setProgressList(progressList1);
        achievedChallenge.setProgressList(progressList2);
        List<List<Progress>> progressList = new ArrayList<>();
        progressList.add(progressList1);
        progressList.add(progressList2);

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(user1)
            .challenge(walkingChallenge)
            .member("parent").build();
        ChallengeUser challengeUser2 = ChallengeUser.builder().user(user1)
            .challenge(rejectedChallenge)
            .member("parent").build();
        ChallengeUser challengeUser3 = ChallengeUser.builder().user(user1)
            .challenge(pendingChallenge)
            .member("parent").build();
        ChallengeUser challengeUser4 = ChallengeUser.builder().user(user1)
            .challenge(achievedChallenge)
            .member("parent").build();
        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(challengeUser1);
        challengeUserList.add(challengeUser2);
        challengeUserList.add(challengeUser3);
        challengeUserList.add(challengeUser4);

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(mom))
            .thenReturn(Optional.ofNullable(familyUser2));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        Mockito.when(mockChallengeUserRepository.findByChallenge_ContractUserId(3L))
            .thenReturn(challengeUserList);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);

        ExpoNotificationServiceImpl expoNotificationService = Mockito.mock(
            ExpoNotificationServiceImpl.class);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, expoNotificationService);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            null, null, mockProgressRepository, mockCommentRepository);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.deleteFamilyUser(mom, familyRequest);

        // then
        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(4)).save(kCaptor.capture());
        Assertions.assertEquals(kid, kCaptor.getValue());

        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());
        Assertions.assertEquals(parent, pCaptor.getValue());

        ArgumentCaptor<Progress> prgCaptor = ArgumentCaptor.forClass(Progress.class);
        Mockito.verify(mockProgressRepository, Mockito.times(2))
            .deleteAll((Iterable<? extends Progress>) prgCaptor.capture());
        Assertions.assertEquals(progressList, prgCaptor.getAllValues());

        ArgumentCaptor<Comment> cmtCaptor = ArgumentCaptor.forClass(Comment.class);
        Mockito.verify(mockCommentRepository, Mockito.times(1))
            .delete(cmtCaptor.capture());
        Assertions.assertEquals(comment, cmtCaptor.getValue());

        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(4)).delete(cCaptor.capture());
        List<Challenge> capturedChallengeList = cCaptor.getAllValues();
        Assertions.assertEquals(walkingChallenge, capturedChallengeList.get(0));
        Assertions.assertEquals(rejectedChallenge, capturedChallengeList.get(1));

        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("자녀(엄마와 가족, 성공한 돈길 있을 때) 가족 나가기 성공 시, 결과 반환 하는지 확인")
    public void testIfKidFamilyUserWithMomAndAchievedChallengeDeleteSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(1L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User mom = User.builder()
            .id(3L)
            .username("mom")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User dad = User.builder()
            .id(4L)
            .username("dad")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user1)
            .build();
        user1.setKid(kid);

        Parent parent = Parent.builder().id(1L).acceptedRequest(5L).totalRequest(5L).user(mom)
            .build();
        mom.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(mom).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "테스트용 돈길",
            30L, 3000L, 50000L, 10000L, 5L, "test");
        ChallengeCategory challengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem targetItem = TargetItem.builder().id(1L).name("전자제품").build();
        Challenge challenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.ACHIEVED)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        Comment comment = Comment.builder().id(1L).challenge(challenge).user(mom)
            .content("아쉽구나").build();
        challenge.setComment(comment);

        Progress progress1 = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress2 = Progress.builder().id(2L).weeks(2L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress3 = Progress.builder().id(3L).weeks(3L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress4 = Progress.builder().id(4L).weeks(4L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress5 = Progress.builder().id(5L).weeks(5L).isAchieved(false)
            .challenge(challenge).build();
        List<Progress> progressList = new ArrayList<>();
        progressList.add(progress1);
        progressList.add(progress2);
        progressList.add(progress3);
        progressList.add(progress4);
        progressList.add(progress5);
        challenge.setProgressList(progressList);

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(user1).challenge(challenge)
            .member("parent").build();
        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(challengeUser1);

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        Mockito.when(mockChallengeUserRepository.findByUserId(1L)).thenReturn(challengeUserList);

        ExpoNotificationServiceImpl mockNotificationMapper = Mockito.mock(
            ExpoNotificationServiceImpl.class);
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, mockNotificationMapper);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            null, null, mockProgressRepository, mockCommentRepository);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.deleteFamilyUser(user1, familyRequest);

        // then
        ArgumentCaptor<Progress> prgCaptor = ArgumentCaptor.forClass(Progress.class);
        Mockito.verify(mockProgressRepository, Mockito.times(1))
            .deleteAll((Iterable<? extends Progress>) prgCaptor.capture());
        Assertions.assertEquals(progressList, prgCaptor.getValue());

        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());
        Assertions.assertEquals(challenge, cCaptor.getValue());

        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).deleteAll(
            (Iterable<? extends ChallengeUser>) cuCaptor.capture());
        Assertions.assertEquals(challengeUserList, cuCaptor.getValue());

        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(1)).save(kCaptor.capture());
        Assertions.assertEquals(kid, kCaptor.getValue());

        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());
        Assertions.assertEquals(parent, pCaptor.getValue());

        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("자녀(엄마와 가족, 걷고 있는 돈길 있을 때) 가족 나가기 성공 시, 결과 반환 하는지 확인")
    public void testIfKidFamilyUserWithMomAndWalkingChallengeDeleteSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(1L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User mom = User.builder()
            .id(3L)
            .username("mom")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User dad = User.builder()
            .id(4L)
            .username("dad")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user1)
            .build();
        user1.setKid(kid);

        Parent parent = Parent.builder().id(1L).acceptedRequest(5L).totalRequest(5L).user(mom)
            .build();
        mom.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(mom).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "테스트용 돈길",
            30L, 3000L, 50000L, 10000L, 5L, "test");
        ChallengeCategory challengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem targetItem = TargetItem.builder().id(1L).name("전자제품").build();
        Challenge challenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.WALKING)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        Comment comment = Comment.builder().id(1L).challenge(challenge).user(mom)
            .content("아쉽구나").build();
        challenge.setComment(comment);

        Progress progress1 = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress2 = Progress.builder().id(2L).weeks(2L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress3 = Progress.builder().id(3L).weeks(3L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress4 = Progress.builder().id(4L).weeks(4L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress5 = Progress.builder().id(5L).weeks(5L).isAchieved(false)
            .challenge(challenge).build();
        List<Progress> progressList = new ArrayList<>();
        progressList.add(progress1);
        progressList.add(progress2);
        progressList.add(progress3);
        progressList.add(progress4);
        progressList.add(progress5);
        challenge.setProgressList(progressList);

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(user1).challenge(challenge)
            .member("parent").build();
        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(challengeUser1);

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        Mockito.when(mockChallengeUserRepository.findByUserId(1L)).thenReturn(challengeUserList);

        ExpoNotificationServiceImpl mockNotificationMapper = Mockito.mock(
            ExpoNotificationServiceImpl.class);
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, mockNotificationMapper);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            null, null, mockProgressRepository, mockCommentRepository);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.deleteFamilyUser(user1, familyRequest);

        // then
        ArgumentCaptor<Progress> prgCaptor = ArgumentCaptor.forClass(Progress.class);
        Mockito.verify(mockProgressRepository, Mockito.times(1))
            .deleteAll((Iterable<? extends Progress>) prgCaptor.capture());
        Assertions.assertEquals(progressList, prgCaptor.getValue());

        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());
        Assertions.assertEquals(challenge, cCaptor.getValue());

        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).deleteAll(
            (Iterable<? extends ChallengeUser>) cuCaptor.capture());
        Assertions.assertEquals(challengeUserList, cuCaptor.getValue());

        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(1)).save(kCaptor.capture());
        Assertions.assertEquals(kid, kCaptor.getValue());

        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());
        Assertions.assertEquals(parent, pCaptor.getValue());

        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("자녀(엄마와 가족, 제안한 돈길 있을 때) 가족 나가기 성공 시, 결과 반환 하는지 확인")
    public void testIfKidFamilyUserWithMomAndPendingChallengeDeleteSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(1L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User mom = User.builder()
            .id(3L)
            .username("mom")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User dad = User.builder()
            .id(4L)
            .username("dad")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user1)
            .build();
        user1.setKid(kid);

        Parent parent = Parent.builder().id(1L).acceptedRequest(5L).totalRequest(5L).user(mom)
            .build();
        mom.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(mom).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "테스트용 돈길",
            30L, 3000L, 50000L, 10000L, 5L, "test");
        ChallengeCategory challengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem targetItem = TargetItem.builder().id(1L).name("전자제품").build();
        Challenge challenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.PENDING)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        Comment comment = Comment.builder().id(1L).challenge(challenge).user(mom)
            .content("아쉽구나").build();
        challenge.setComment(comment);

        Progress progress1 = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress2 = Progress.builder().id(2L).weeks(2L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress3 = Progress.builder().id(3L).weeks(3L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress4 = Progress.builder().id(4L).weeks(4L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress5 = Progress.builder().id(5L).weeks(5L).isAchieved(false)
            .challenge(challenge).build();
        List<Progress> progressList = new ArrayList<>();
        progressList.add(progress1);
        progressList.add(progress2);
        progressList.add(progress3);
        progressList.add(progress4);
        progressList.add(progress5);
        challenge.setProgressList(progressList);

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(user1).challenge(challenge)
            .member("parent").build();
        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(challengeUser1);

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        Mockito.when(mockChallengeUserRepository.findByUserId(1L)).thenReturn(challengeUserList);

        ExpoNotificationServiceImpl mockNotificationMapper = Mockito.mock(
            ExpoNotificationServiceImpl.class);
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, mockNotificationMapper);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            null, null, mockProgressRepository, mockCommentRepository);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.deleteFamilyUser(user1, familyRequest);

        // then
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());
        Assertions.assertEquals(challenge, cCaptor.getValue());

        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).deleteAll(
            (Iterable<? extends ChallengeUser>) cuCaptor.capture());
        Assertions.assertEquals(challengeUserList, cuCaptor.getValue());

        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(1)).save(kCaptor.capture());
        Assertions.assertEquals(kid, kCaptor.getValue());

        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());
        Assertions.assertEquals(parent, pCaptor.getValue());

        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("자녀(아빠와 가족, 거절된 돈길 있을 때) 가족 나가기 성공 시, 결과 반환 하는지 확인")
    public void testIfKidFamilyUserWithDadAndRejectedChallengeDeleteSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(1L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User mom = User.builder()
            .id(3L)
            .username("mom")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User dad = User.builder()
            .id(4L)
            .username("dad")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user1)
            .build();
        user1.setKid(kid);

        Parent parent = Parent.builder().id(1L).acceptedRequest(5L).totalRequest(5L).user(dad)
            .build();
        dad.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(dad).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeRequest challengeRequest = new ChallengeRequest(false, "이자율 받기", "전자제품", "테스트용 돈길",
            30L, 3000L, 50000L, 10000L, 5L, "test");
        ChallengeCategory challengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem targetItem = TargetItem.builder().id(1L).name("전자제품").build();
        Challenge challenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.REJECTED)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        Comment comment = Comment.builder().id(1L).challenge(challenge).user(dad)
            .content("아쉽구나").build();
        challenge.setComment(comment);

        Progress progress1 = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress2 = Progress.builder().id(2L).weeks(2L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress3 = Progress.builder().id(3L).weeks(3L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress4 = Progress.builder().id(4L).weeks(4L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress5 = Progress.builder().id(5L).weeks(5L).isAchieved(false)
            .challenge(challenge).build();
        List<Progress> progressList = new ArrayList<>();
        progressList.add(progress1);
        progressList.add(progress2);
        progressList.add(progress3);
        progressList.add(progress4);
        progressList.add(progress5);
        challenge.setProgressList(progressList);

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(user1).challenge(challenge)
            .member("parent").build();
        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(challengeUser1);

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        Mockito.when(mockChallengeUserRepository.findByUserId(1L)).thenReturn(challengeUserList);

        ExpoNotificationServiceImpl mockNotificationMapper = Mockito.mock(
            ExpoNotificationServiceImpl.class);
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, mockNotificationMapper);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            null, null, mockProgressRepository, mockCommentRepository);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.deleteFamilyUser(user1, familyRequest);

        // then
        ArgumentCaptor<Comment> cmtCaptor = ArgumentCaptor.forClass(Comment.class);
        Mockito.verify(mockCommentRepository, Mockito.times(1)).delete(cmtCaptor.capture());
        Assertions.assertEquals(comment, cmtCaptor.getValue());

        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());
        Assertions.assertEquals(challenge, cCaptor.getValue());

        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).deleteAll(
            (Iterable<? extends ChallengeUser>) cuCaptor.capture());
        Assertions.assertEquals(challengeUserList, cuCaptor.getValue());

        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(1)).save(kCaptor.capture());
        Assertions.assertEquals(kid, kCaptor.getValue());

        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());
        Assertions.assertEquals(parent, pCaptor.getValue());

        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("자녀(아빠와 가족, 실패한 돈길 있을 때) 가족 나가기 성공 시, 결과 반환 하는지 확인")
    public void testIfKidFamilyUserWithDadAndFailedChallengeDeleteSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(true)
            .build();
        User user2 = User.builder()
            .id(1L)
            .username("user2")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(false)
            .build();
        User mom = User.builder()
            .id(3L)
            .username("mom")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(true)
            .build();
        User dad = User.builder()
            .id(4L)
            .username("dad")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(false)
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user1)
            .build();
        user1.setKid(kid);

        Parent parent = Parent.builder().id(1L).acceptedRequest(5L).totalRequest(5L).user(dad)
            .build();
        dad.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        FamilyUser familyUser3 = FamilyUser.builder().user(dad).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        familyUserList.add(familyUser3);
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeRequest challengeRequest = new ChallengeRequest(false, "이자율 받기", "전자제품", "테스트용 돈길",
            30L, 3000L, 50000L, 10000L, 5L, "test");
        ChallengeCategory challengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem targetItem = TargetItem.builder().id(1L).name("전자제품").build();
        Challenge challenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : dad)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(ChallengeStatus.FAILED)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(challengeCategory).targetItem(targetItem)
            .filename(challengeRequest.getFileName()).build();
        Comment comment = Comment.builder().id(1L).challenge(challenge).user(dad)
            .content("아쉽구나").build();
        challenge.setComment(comment);

        Progress progress1 = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress2 = Progress.builder().id(2L).weeks(2L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress3 = Progress.builder().id(3L).weeks(3L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress4 = Progress.builder().id(4L).weeks(4L).isAchieved(false)
            .challenge(challenge).build();
        Progress progress5 = Progress.builder().id(5L).weeks(5L).isAchieved(false)
            .challenge(challenge).build();
        List<Progress> progressList = new ArrayList<>();
        progressList.add(progress1);
        progressList.add(progress2);
        progressList.add(progress3);
        progressList.add(progress4);
        progressList.add(progress5);
        challenge.setProgressList(progressList);

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(user1).challenge(challenge)
            .member("parent").build();
        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(challengeUser1);

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        Mockito.when(mockChallengeUserRepository.findByUserId(1L)).thenReturn(challengeUserList);

        ExpoNotificationServiceImpl mockNotificationMapper = Mockito.mock(
            ExpoNotificationServiceImpl.class);
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        KidServiceImpl kidService = new KidServiceImpl(mockKidRepository, mockNotificationMapper);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            null, null, mockProgressRepository, mockCommentRepository);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.deleteFamilyUser(user1, familyRequest);

        // then
        ArgumentCaptor<Progress> prgCaptor = ArgumentCaptor.forClass(Progress.class);
        Mockito.verify(mockProgressRepository, Mockito.times(1))
            .deleteAll((Iterable<? extends Progress>) prgCaptor.capture());
        Assertions.assertEquals(progressList, prgCaptor.getValue());

        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());
        Assertions.assertEquals(challenge, cCaptor.getValue());

        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).deleteAll(
            (Iterable<? extends ChallengeUser>) cuCaptor.capture());
        Assertions.assertEquals(challengeUserList, cuCaptor.getValue());

        ArgumentCaptor<Kid> kCaptor = ArgumentCaptor.forClass(Kid.class);
        Mockito.verify(mockKidRepository, Mockito.times(1)).save(kCaptor.capture());
        Assertions.assertEquals(kid, kCaptor.getValue());

        ArgumentCaptor<Parent> pCaptor = ArgumentCaptor.forClass(Parent.class);
        Mockito.verify(mockParentRepository, Mockito.times(1)).save(pCaptor.capture());
        Assertions.assertEquals(parent, pCaptor.getValue());

        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }


    @Test
    @DisplayName("유저 가족 나가기 성공 후 가족 없어 삭제 성공 시, 결과 반환 하는지 확인")
    public void testIfFamilyUserDeleteSucceedAndFamilyDeleteSucceedThenReturnResult() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(null)
            .build();

        Parent parent = Parent.builder().id(1L).acceptedRequest(0L).totalRequest(0L).user(user1)
            .build();

        user1.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family2, user1))
            .thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = Mockito.mock(
            ChallengeUserServiceImpl.class);
        KidServiceImpl kidService = Mockito.mock(KidServiceImpl.class);
        ParentServiceImpl parentService = Mockito.mock(ParentServiceImpl.class);
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        CommonResponse<FamilyDTO> result = familyController.deleteFamilyUser(user1, familyRequest);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).delete(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        ArgumentCaptor<Family> fCaptor = ArgumentCaptor.forClass(Family.class);
        Mockito.verify(mockFamilyRepository, Mockito.times(1)).delete(fCaptor.capture());
        Assertions.assertEquals(family2, fCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("유저가 가입된 가족이 없는 경우, 에러 처리 하는지 확인")
    public void testIfFamilyUserDeleteFailBecauseFamilyUserNotExistThenThrowBadRequestException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(null)
            .build();

        Parent parent = Parent.builder().id(1L).acceptedRequest(0L).totalRequest(0L).user(user1)
            .build();

        user1.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        FamilyRequest familyRequest = new FamilyRequest("test");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.deleteFamilyUser(user1, familyRequest);
        });
    }

    @Test
    @DisplayName("자녀가 가입 된 가족과 다른 가족을 탈퇴하고자 할 때, 에러 처리 하는지 확인")
    public void testIfKidFamilyUserDeleteFailDueToDifferentRequestCodeThenThrowBadRequestException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(true)
            .isFemale(null)
            .build();

        Kid kid = Kid.builder()
            .id(1L)
            .savings(0L)
            .achievedChallenge(0L)
            .totalChallenge(0L)
            .level(1L)
            .user(user1)
            .build();

        user1.setKid(kid);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        FamilyRequest familyRequest = new FamilyRequest("code");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.deleteFamilyUser(user1, familyRequest);
        });
    }

    @Test
    @DisplayName("부모가 가입 된 가족과 다른 가족을 탈퇴하고자 할 때, 에러 처리 하는지 확인")
    public void testIfParentFamilyUserDeleteFailDueToDifferentRequestCodeThenThrowBadRequestException() {
        // given
        User user1 = User.builder()
            .id(1L)
            .username("user1")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .isKid(false)
            .isFemale(null)
            .build();

        Parent parent = Parent.builder().id(1L).acceptedRequest(0L).totalRequest(0L).user(user1)
            .build();

        user1.setParent(parent);

        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        FamilyRequest familyRequest = new FamilyRequest("code");

        ChallengeServiceImpl challengeService = Mockito.mock(ChallengeServiceImpl.class);
        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUser(user1))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);
        NotificationMapper mockNotificationMapper = Mockito.mock(
            NotificationMapper.class);
        ChallengeUserServiceImpl challengeUserService = null;
        KidServiceImpl kidService = null;
        ParentServiceImpl parentService = null;
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(mockFamilyRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            notificationRepository);
        FamilyMapper familyMapper = new FamilyMapper(
            familyService,
            familyUserService,
            notificationService,
            challengeService,
            challengeUserService,
            kidService,
            parentService
        );
        FamilyController familyController = new FamilyController(familyMapper);

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.deleteFamilyUser(user1, familyRequest);
        });
    }

}
