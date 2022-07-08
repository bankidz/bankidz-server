package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.ChallengeController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.exception.NotFoundException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);
        //given
        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
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
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
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
    @DisplayName("챌린지 생성 시, 목표 아이템 입력 400 에러 테스트")
    public void testIfMakeChallengeTargetItemBadRequestErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "선물", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();
        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            newChallenge.getChallengeCategory().getCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(newChallenge.getTargetItem().getName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () -> {
            challengeController.postChallenge(newUser, challengeRequest, mockBindingResult);
        });
    }

    @Test
    @DisplayName("챌린지 생성 시, 챌린지 카테고리 400 에러 테스트")
    public void testIfMakeChallengeChallengeCategoryBadRequestErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("형제와 경쟁 하기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            newChallenge.getChallengeCategory().getCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(newChallenge.getTargetItem().getName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () -> {
            challengeController.postChallenge(newUser, challengeRequest, mockBindingResult);
        });

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
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 11L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            newChallenge.getChallengeCategory().getCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(newChallenge.getTargetItem().getName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () -> {
            challengeController.postChallenge(newUser, challengeRequest, mockBindingResult);
        });

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
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse result = challengeController.getChallenge(newUser, challengeId);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge);
        ArgumentCaptor<Long> cuCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1))
            .findByChallengeId(cuCaptor.capture());

        Assertions.assertEquals(newChallenge.getId(), cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 정보 조회 시, 챌린지를 생성한 유저가 아닌 경우 403 에러 테스트")
    public void testIfNotAuthUserForDetailChallengeIsForbbiden() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser1 = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newUser2 = User.builder().id(2L).username("user2").isFemale(true).birthday("19990623")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser1).build();

        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge);

        Assertions.assertThrows(ForbiddenException.class, () -> {
            challengeController.getChallenge(newUser2, challengeId);
        });
    }

    @Test
    @DisplayName("챌린지 조회 시, 챌린지 아이디로 챌린지를 못찾으면 404 에러 테스트")
    public void testIfGetChallengeIsNullNotFoundErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser1 = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser1).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Progress newProgress2 = Progress.builder().id(2L).weeks(2L).isAchieved(true)
            .challenge(newChallenge).build();

        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();

        //then
        Assertions.assertThrows(NotFoundException.class, () -> {
            challengeController.getChallenge(newUser1, 2L);
        });
    }

    @Test
    @DisplayName("챌린지 삭제 시, 정상적으로 없어지는지 테스트")
    public void testIfDeleteChallengeIsNull() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        List<Progress> progressList = Arrays.asList(newProgress);
        newChallenge.setProgressList(progressList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse result = challengeController.deleteChallenge(newUser, challengeId);

        //then
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).delete(cuCaptor.capture());
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(null), result);
    }

    @Test
    @DisplayName("챌린지 삭제 시, 챌린지를 생성한 유저가 아닌 경우 403 에러 테스트")
    public void testIfNotAuthUserDeleteChallengeForbbiden() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser1 = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newUser2 = User.builder().id(2L).username("user2").isFemale(true).birthday("19990623")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser1).build();

        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge);

        Assertions.assertThrows(ForbiddenException.class, () -> {
            challengeController.deleteChallenge(newUser2, challengeId);
        });
    }

    @Test
    @DisplayName("생성한지 일주일이 넘은 돈길 삭제 시도 시, 400 에러 테스트")
    public void testIfOverOneWeekChallengeTryDeleteBadRequest() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser1 = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser1).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Progress newProgress2 = Progress.builder().id(2L).weeks(2L).isAchieved(true)
            .challenge(newChallenge).build();

        List<Progress> progressList = Arrays.asList(newProgress, newProgress2);
        newChallenge.setProgressList(progressList);
        int size = newChallenge.getProgressList().size();
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();

        //then
        Assertions.assertThrows(BadRequestException.class, () -> {
            challengeController.deleteChallenge(newUser1, challengeId);
        });
    }

    @Test
    @DisplayName("챌린지 삭제 시, 챌린지 아이디로 챌린지를 못찾으면 404 에러 테스트")
    public void testIfDeleteChallengeIsNullNotFoundErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser1 = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser1).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Progress newProgress2 = Progress.builder().id(2L).weeks(2L).isAchieved(true)
            .challenge(newChallenge).build();

        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();

        //then
        Assertions.assertThrows(NotFoundException.class, () -> {
            challengeController.deleteChallenge(newUser1, 2L);
        });
    }

    @Test
    @DisplayName("챌린지 리스트 가져오기 테스트")
    public void testIfGetListChallengeTest() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        ChallengeRequest challengeRequest1 = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 펜슬 사기",
            10L, 100000L, 10000L, 10L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Challenge newChallenge1 = Challenge.builder().title(challengeRequest1.getTitle())
            .isAchieved(false).totalPrice(challengeRequest1.getTotalPrice())
            .weekPrice(challengeRequest1.getWeekPrice()).weeks(challengeRequest1.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest1.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().challenge(newChallenge1)
            .member("parent").user(newUser).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.save(newChallenge1)).thenReturn(newChallenge1);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser1))
            .thenReturn(newChallengeUser1);
        Mockito.when(mockChallengeUserRepository.findByUserId(newUser.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse result = challengeController.getListChallenge(newUser, "pending");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        for (ChallengeUser r : challengeUserList) {
            challengeDTOList.add(new ChallengeDTO(r.getChallenge()));
        }

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList).getData(),
            result.getData());
    }

    @Test
    @DisplayName("챌린지 리스트 조회 시, 생성한 챌린지가 한 개도 없으면 빈 배열 반환")
    public void testIfNotCreateChallengeReturnEmptyList() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();

        Mockito.when(mockChallengeUserRepository.findByUserId(newUser.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse result = challengeController.getListChallenge(newUser, "pending");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        for (ChallengeUser r : challengeUserList) {
            challengeDTOList.add(new ChallengeDTO(r.getChallenge()));
        }

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList).getData(),
            result.getData());
    }
}
