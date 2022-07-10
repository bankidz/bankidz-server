package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.ChallengeController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Comment;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.exception.NotFoundException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
    @DisplayName("챌린지 생성 시, 챌린지에 걸리는 progress 정상 생성 테스트")
    public void testMakeProgress() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            5000L, 1000L, 5L);

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

        List<Progress> progressList = new ArrayList<>();
        for (int i = 0; i < challengeRequest.getWeeks(); i++) {
            Progress newProgress = Progress.builder().challenge(newChallenge).isAchieved(false)
                .weeks(Long.valueOf(i)).build();
            progressList.add(newProgress);
        }

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
        Mockito.when(mockProgressRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(progressList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse result = challengeController.postChallenge(newUser, challengeRequest,
            mockBindingResult);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge);

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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(2L)
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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse result = challengeController.getListChallenge(newUser, "pending");
        CommonResponse result1 = challengeController.getListChallenge(newUser, "accept");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<ChallengeDTO> challengeDTOList1 = new ArrayList<>();
        for (ChallengeUser r : challengeUserList) {
            if (r.getChallenge().getStatus() != 2L) {
                challengeDTOList.add(new ChallengeDTO(r.getChallenge()));

            } else {
                challengeDTOList1.add(new ChallengeDTO(r.getChallenge()));
            }
        }

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList).getData(),
            result.getData());
        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList1).getData(),
            result1.getData());

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
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

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
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
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
    @DisplayName("자녀 돈길 리스트 조회 시, 정상 response 테스트")
    public void testIfGetListChildChallengeList() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        ChallengeRequest challengeRequest1 = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            1500L, 100L, 15L);

        User newUser = User.builder().id(1L).username("user").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newParent = User.builder().id(2L).username("user1").isFemale(true).birthday("19990623")
            .authenticationCode("code1").provider("kakao").isKid(false).refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest1.getTitle())
            .isAchieved(false).totalPrice(challengeRequest1.getTotalPrice())
            .weekPrice(challengeRequest1.getWeekPrice()).weeks(challengeRequest1.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(2L)
            .interestRate(challengeRequest1.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(newUser).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent").user(newUser).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(newParent).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(newUser).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        challengeUserList.forEach(challengeUser -> {
            challengeDTOList.add(new ChallengeDTO(challengeUser.getChallenge()));
        });

        Mockito.when(mockChallengeUserRepository.findByUserId(newUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockFmailyUserRepository.findByUserId(newParent.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFmailyUserRepository.findByUserId(newUser.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFmailyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse result = challengeController.getListKidChallenge(newParent);

        //then
        List<KidChallengeListDTO> kidChallengeListDTOList = new ArrayList<>();
        kidChallengeListDTOList.add(new KidChallengeListDTO(newUser, challengeDTOList));
        kidChallengeListDTOList.stream()
            .forEach(s -> {
                System.out.println("s.getChallengeList() = " + s.getChallengeList());
                System.out.println("s.getUserName() = " + s.getUserName());
            });

        Assertions.assertEquals(CommonResponse.onSuccess(kidChallengeListDTOList).getData(),
            result.getData());
    }

    @Test
    @DisplayName("자녀 돈길 요청 수락 / 거절 시 , db 업데이트 테스트")
    public void testIfUpdateChallengeStatusIsSuccess() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        ChallengeRequest challengeRequest1 = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            1500L, 100L, 15L);

        KidChallengeRequest successKidChallengeRequest = new KidChallengeRequest(true, null);
        KidChallengeRequest falseKidChallengeRequest = new KidChallengeRequest(false, "아쉽다");

        User newUser = User.builder().id(1L).username("user").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newParent = User.builder().id(2L).username("user1").isFemale(true).birthday("19990623")
            .authenticationCode("code1").provider("kakao").isKid(false).refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest1.getTitle())
            .isAchieved(false).totalPrice(challengeRequest1.getTotalPrice())
            .weekPrice(challengeRequest1.getWeekPrice()).weeks(challengeRequest1.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest1.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(newUser).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(newUser).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(newParent).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(newUser).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        Comment newComment = Comment.builder().content(falseKidChallengeRequest.getComment())
            .challenge(newChallenge1).user(newParent).build();

        Mockito.when(mockChallengeUserRepository.findByUserId(newUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallenge1));
        Mockito.when(mockFmailyUserRepository.findByUserId(newParent.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFmailyUserRepository.findByUserId(newUser.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFmailyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse successResult = challengeController.patchChallengeStatus(newParent,
            newChallenge.getId(), successKidChallengeRequest);
        CommonResponse falseResult = challengeController.patchChallengeStatus(newParent,
            newChallenge1.getId(), falseKidChallengeRequest);

        //then

        newChallenge.setStatus(2L);
        newChallenge1.setStatus(0L);
        newChallenge1.setComment(newComment);
        ChallengeDTO successChallengeDTO = new ChallengeDTO(newChallenge);
        ChallengeDTO falseChallengeDTO = new ChallengeDTO(newChallenge1);
        Assertions.assertEquals(CommonResponse.onSuccess(successChallengeDTO), successResult);
        Assertions.assertEquals(CommonResponse.onSuccess(falseChallengeDTO), falseResult);

    }

    @Test
    @DisplayName("자녀 돈길 요청 수락 / 거절 시 , 존재하지 않는 돈길일 때 400에러 테스트")
    public void testIfUpdateChallengeStatusNotExistChallengeBadRequestErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        ChallengeRequest challengeRequest1 = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            1500L, 100L, 15L);

        KidChallengeRequest kidChallengeRequest = new KidChallengeRequest(true, null);

        User newUser = User.builder().id(1L).username("user").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newParent = User.builder().id(2L).username("user1").isFemale(true).birthday("19990623")
            .authenticationCode("code1").provider("kakao").isKid(false).refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest1.getTitle())
            .isAchieved(false).totalPrice(challengeRequest1.getTotalPrice())
            .weekPrice(challengeRequest1.getWeekPrice()).weeks(challengeRequest1.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest1.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(newUser).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(newUser).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(newParent).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(newUser).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        Mockito.when(mockChallengeUserRepository.findByUserId(newUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallenge1));
        Mockito.when(mockFmailyUserRepository.findByUserId(newParent.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFmailyUserRepository.findByUserId(newUser.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFmailyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        //then

        Assertions.assertThrows(BadRequestException.class, () -> {
            challengeController.patchChallengeStatus(newParent, 150L,
                kidChallengeRequest);
        });
    }

    @Test
    @DisplayName("자녀 돈길 요청 수락 / 거절 시 , 권한이 없을 때 403에러 테스트")
    public void testIfUpdateChallengeStatusNotAuthUserForbbidenErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        ChallengeRequest challengeRequest1 = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            1500L, 100L, 15L);

        KidChallengeRequest kidChallengeRequest = new KidChallengeRequest(true, null);

        User newUser = User.builder().id(1L).username("user").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newParent = User.builder().id(2L).username("user1").isFemale(true).birthday("19990623")
            .authenticationCode("code1").provider("kakao").isKid(false).refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest1.getTitle())
            .isAchieved(false).totalPrice(challengeRequest1.getTotalPrice())
            .weekPrice(challengeRequest1.getWeekPrice()).weeks(challengeRequest1.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest1.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(newUser).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(newUser).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(newParent).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(newUser).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        Mockito.when(mockChallengeUserRepository.findByUserId(newUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallenge1));
        Mockito.when(mockFmailyUserRepository.findByUserId(newParent.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFmailyUserRepository.findByUserId(newUser.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFmailyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        //then

        Assertions.assertThrows(ForbiddenException.class, () -> {
            challengeController.patchChallengeStatus(newUser, newChallenge.getId(),
                kidChallengeRequest);
        });
    }

    @Test
    @DisplayName("자녀 돈길 요청 수락 / 거절 시 , 이미 처리된 돈길일 때 400에러 테스트")
    public void testIfUpdateChallengeStatusAleadyChallengeBadRequestErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFmailyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        ChallengeRequest challengeRequest1 = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            1500L, 100L, 15L);

        KidChallengeRequest kidChallengeRequest = new KidChallengeRequest(true, null);

        User newUser = User.builder().id(1L).username("user").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newParent = User.builder().id(2L).username("user1").isFemale(true).birthday("19990623")
            .authenticationCode("code1").provider("kakao").isKid(false).refreshToken("token")
            .build();

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .isAchieved(false).totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(2L)
            .interestRate(challengeRequest.getInterestRate()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest1.getTitle())
            .isAchieved(false).totalPrice(challengeRequest1.getTotalPrice())
            .weekPrice(challengeRequest1.getWeekPrice()).weeks(challengeRequest1.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
            .interestRate(challengeRequest1.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(newUser).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(newUser).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(newParent).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(newUser).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        Mockito.when(mockChallengeUserRepository.findByUserId(newUser.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallenge1));
        Mockito.when(mockFmailyUserRepository.findByUserId(newParent.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFmailyUserRepository.findByUserId(newUser.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFmailyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFmailyUserRepository, mockCommentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        //then

        Assertions.assertThrows(BadRequestException.class, () -> {
            challengeController.patchChallengeStatus(newParent, newChallenge.getId(),
                kidChallengeRequest);
        });
    }
}
