package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.FamilyController;
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
        familyUserList.add(familyUser1);

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
}
