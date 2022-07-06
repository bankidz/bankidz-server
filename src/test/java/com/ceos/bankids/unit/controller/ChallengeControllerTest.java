package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.ChallengeController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;

public class ChallengeControllerTest {

    @Test
    @DisplayName("챌린지 생성 성공 시, 결과 반환과 디비에 정상 저장되는지 확인")
    public void testIfPostChallengeSuccessReturnResultAndSaveDb() {

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);
        //given
        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기",
            30L, 150000L, 10000L, 15L);

        User newUser = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder()
            .id(1L)
            .category("이자율 받기")
            .build();

        TargetItem newTargetItem = TargetItem.builder()
            .id(1L)
            .itemName("전자제품")
            .build();

        Challenge newChallenge = Challenge.builder()
            .title(challengeRequest.getTitle())
            .isAchieved(false)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice())
            .weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory)
            .targetItem(newTargetItem)
            .status(1L)
            .interestRate(challengeRequest.getInterestRate())
            .build();

        Mockito.when(mockChallengeRepository.save(newChallenge))
            .thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockTargetItemRepository.findByItemName(newTargetItem.getItemName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockChallengeUserRepository
        );
        ChallengeController challengeController = new ChallengeController(
            challengeService
        );
        CommonResponse result = challengeController.postChallenge(newUser, challengeRequest,
            mockBindingResult);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).save(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 생성 시, 챌린지-유저 미들 테이블 로우 정상 생성 확인")
    public void testMakeChallengeUserRow() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기",
            30L, 150000L, 10000L, 15L);

        User newUser = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder()
            .id(1L)
            .category("이자율 받기")
            .build();

        TargetItem newTargetItem = TargetItem.builder()
            .id(1L)
            .itemName("전자제품")
            .build();

        Challenge newChallenge = Challenge.builder()
            .title(challengeRequest.getTitle())
            .isAchieved(false)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice())
            .weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory)
            .targetItem(newTargetItem)
            .status(1L)
            .interestRate(challengeRequest.getInterestRate())
            .build();

        ChallengeUser newChallengeUser = ChallengeUser.builder()
            .challenge(newChallenge)
            .member("parent")
            .user(newUser)
            .build();

        Mockito.when(mockChallengeRepository.save(newChallenge))
            .thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockTargetItemRepository.findByItemName(newTargetItem.getItemName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockChallengeUserRepository
        );
        ChallengeController challengeController = new ChallengeController(
            challengeService
        );
        CommonResponse result = challengeController.postChallenge(newUser, challengeRequest,
            mockBindingResult);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).save(cCaptor.capture());
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).save(cuCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 정보 가져오기 테스트")
    public void testGetChallengeInfo() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기",
            30L, 150000L, 10000L, 15L);

        User newUser = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder()
            .id(1L)
            .category("이자율 받기")
            .build();

        TargetItem newTargetItem = TargetItem.builder()
            .id(1L)
            .itemName("전자제품")
            .build();

        Challenge newChallenge = Challenge.builder()
            .title(challengeRequest.getTitle())
            .isAchieved(false)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice())
            .weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory)
            .targetItem(newTargetItem)
            .status(1L)
            .interestRate(challengeRequest.getInterestRate())
            .build();

        ChallengeUser newChallengeUser = ChallengeUser.builder()
            .challenge(newChallenge)
            .member("parent")
            .user(newUser)
            .build();

        Mockito.when(mockChallengeRepository.save(newChallenge))
            .thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockTargetItemRepository.findByItemName(newTargetItem.getItemName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockChallengeUserRepository
        );
        ChallengeController challengeController = new ChallengeController(
            challengeService
        );
        Long challengeId = newChallenge.getId();
        CommonResponse result = challengeController.getChallenge(newUser, challengeId);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge);
        ArgumentCaptor<Long> cuCaptor = ArgumentCaptor.forClass(Long.class);
        System.out.println(cuCaptor);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1))
            .findByChallengeId(cuCaptor.capture());

        Assertions.assertEquals(newChallenge.getId(), cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 생성 시, 400 에러 테스트")
    public void testIfMakeChallengeBadRequestErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기",
            30L, 150000L, 10000L, 11L);

        User newUser = User.builder()
            .id(1L)
            .username("user1")
            .isFemale(true)
            .birthday("19990521")
            .authenticationCode("code")
            .provider("kakao")
            .isKid(true)
            .refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder()
            .id(1L)
            .category("이자율 받기")
            .build();

        TargetItem newTargetItem = TargetItem.builder()
            .id(1L)
            .itemName("전자제품")
            .build();

        Challenge newChallenge = Challenge.builder()
            .title(challengeRequest.getTitle())
            .isAchieved(false)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice())
            .weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory)
            .targetItem(newTargetItem)
            .status(1L)
            .interestRate(challengeRequest.getInterestRate())
            .build();

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockChallengeUserRepository
        );
        ChallengeController challengeController = new ChallengeController(
            challengeService
        );

        //then
        Assertions.assertThrows(BadRequestException.class, () -> {
            challengeController.postChallenge(newUser, challengeRequest, mockBindingResult);
        });
        
    }
}
