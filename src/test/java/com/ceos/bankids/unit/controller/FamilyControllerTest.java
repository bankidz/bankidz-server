package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.FamilyController;
import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.dto.KidListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.FamilyRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.service.FamilyServiceImpl;
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.postFamily(user1);
        });
    }

    @Test
    @DisplayName("생성 시 기존 가족 있을 때, 가족 정보 반환하는지 확인")
    public void testIfFamilyExistThenReturnPostResult() {
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<FamilyDTO> result = familyController.postFamily(user1);

        // then
        List<FamilyUserDTO> familyUserDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .filter(u -> !u.getId().equals(1L))
            .map(FamilyUserDTO::new).collect(Collectors.toList());
        FamilyDTO familyDTO = new FamilyDTO(family, familyUserDTOList);
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<FamilyDTO> result = familyController.postFamily(user1);
        String code = result.getData().getCode();
        family.setCode(code);

        // then
        ArgumentCaptor<Family> fCaptor = ArgumentCaptor.forClass(Family.class);
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        Mockito.verify(mockFamilyRepository, Mockito.times(1)).save(fCaptor.capture());
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());

        Assertions.assertEquals(family, fCaptor.getValue());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        List<FamilyUserDTO> familyUserDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .map(FamilyUserDTO::new).collect(Collectors.toList());
        FamilyDTO familyDTO = new FamilyDTO(family, familyUserDTOList);

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<FamilyDTO> result = familyController.getFamily(user1);

        // then
        List<FamilyUserDTO> familyUserDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .map(FamilyUserDTO::new).collect(Collectors.toList());
        FamilyDTO familyDTO = new FamilyDTO(family, familyUserDTOList);
        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("조회 시 기존 가족 없을 때, 빈 가족 정보 반환하는지 확인")
    public void testIfFamilyNotExistThenReturnGetResult() {
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<FamilyDTO> result = familyController.getFamily(user1);

        // then
        FamilyDTO familyDTO = new FamilyDTO(new Family(), new ArrayList<>());

        Assertions.assertEquals(CommonResponse.onSuccess(familyDTO), result);
    }

    @Test
    @DisplayName("조회 시 기존 가족 있으나, 삭제되었을 때 에러 처리 하는지 확인")
    public void testIfFamilyExistedButDeletedWhenGetThenThrowBadRequestException() {
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.getFamily(user1);
        });
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

        // then
        Assertions.assertThrows(ForbiddenException.class, () -> {
            familyController.getFamilyKidList(user1);
        });
    }

    @Test
    @DisplayName("아이 조회 시 기존 가족 있으나, 삭제되었을 때 에러 처리 하는지 확인")
    public void testIfFamilyExistedButDeletedWhenGetKidListThenThrowBadRequestException() {
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
            .isKid(true)
            .isFemale(true)
            .build();
        Family family = Family.builder().id(1L).code("code").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family).build();
        List<FamilyUser> familyUserList = new ArrayList<FamilyUser>();
        familyUserList.add(familyUser1);
        familyUserList.add(familyUser2);

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family)).thenReturn(familyUserList);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<List<KidListDTO>> result = familyController.getFamilyKidList(user1);

        // then
        Assertions.assertEquals(CommonResponse.onSuccess(new ArrayList()), result);
    }

    @Test
    @DisplayName("아이 조회 시 가족 없을 때, 빈 리스트 반환하는지 확인")
    public void testIfFamilyNotExistThenReturnEmptyList() {
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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L)).thenReturn(
            Optional.ofNullable(null));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
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

        Family family1 = Family.builder().id(1L).code("code").build();
        Family family2 = Family.builder().id(2L).code("test").build();
        FamilyUser familyUser1 = FamilyUser.builder().user(user1).family(family1).build();
        FamilyUser familyUser2 = FamilyUser.builder().user(user2).family(family2).build();
        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(familyUser2);
        FamilyRequest familyRequest = new FamilyRequest("test");

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family1));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
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

        List<FamilyUserDTO> familyUserDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .map(FamilyUserDTO::new).collect(Collectors.toList());
        FamilyDTO familyDTO = new FamilyDTO(family2, familyUserDTOList);

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<FamilyDTO> result = familyController.postFamilyUser(user1, familyRequest);
        String code = result.getData().getCode();
        family2.setCode(code);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        familyUser1.setFamily(family2);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        List<FamilyUserDTO> familyUserDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .map(FamilyUserDTO::new).collect(Collectors.toList());
        FamilyDTO familyDTO = new FamilyDTO(family2, familyUserDTOList);

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(family2));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family1));
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(family2));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<FamilyDTO> result = familyController.postFamilyUser(user1, familyRequest);
        String code = result.getData().getCode();
        family2.setCode(code);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        familyUser1.setFamily(family2);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        List<FamilyUserDTO> familyUserDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .map(FamilyUserDTO::new).collect(Collectors.toList());
        FamilyDTO familyDTO = new FamilyDTO(family2, familyUserDTOList);

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(1L)).thenReturn(Optional.ofNullable(family1));
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(family2));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(familyUser1));
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );
        CommonResponse<FamilyDTO> result = familyController.postFamilyUser(user1, familyRequest);
        String code = result.getData().getCode();
        family2.setCode(code);

        // then
        ArgumentCaptor<FamilyUser> fuCaptor = ArgumentCaptor.forClass(FamilyUser.class);
        familyUser1.setFamily(family2);
        Mockito.verify(mockFamilyUserRepository, Mockito.times(1)).save(fuCaptor.capture());
        Assertions.assertEquals(familyUser1, fuCaptor.getValue());

        List<FamilyUserDTO> familyUserDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .map(FamilyUserDTO::new).collect(Collectors.toList());
        FamilyDTO familyDTO = new FamilyDTO(family2, familyUserDTOList);

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findById(2L)).thenReturn(Optional.ofNullable(null));
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(null));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

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

        FamilyRepository mockFamilyRepository = Mockito.mock(FamilyRepository.class);
        Mockito.when(mockFamilyRepository.findByCode("test"))
            .thenReturn(Optional.ofNullable(family2));
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        Mockito.when(mockFamilyUserRepository.findByUserId(1L))
            .thenReturn(Optional.ofNullable(null));
        familyUser1.setFamily(family2);
        Mockito.when(mockFamilyUserRepository.findByFamily(family2)).thenReturn(familyUserList);

        // when
        FamilyServiceImpl familyService = new FamilyServiceImpl(
            mockFamilyRepository,
            mockFamilyUserRepository
        );
        FamilyController familyController = new FamilyController(
            familyService
        );

        // then
        Assertions.assertThrows(BadRequestException.class, () -> {
            familyController.postFamilyUser(user1, familyRequest);
        });
    }
}
