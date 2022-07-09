package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.ProgressController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.ProgressRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.service.ProgressServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ProgressControllerTest {

    @Test
    @DisplayName("돈길 걷기 요청 시, 프로그레스의 row가 정상적으로 업데이트 되는지 테스트")
    public void testIfSavingsProgressRowUpdate() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);

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

        Progress newProgress = Progress.builder()
            .id(1L)
            .challenge(newChallenge)
            .weeks(1L)
            .isAchieved(false)
            .build();

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
        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(), 1L))
            .thenReturn(Optional.ofNullable(newProgress));

        //when
        ProgressRequest progressRequest = new ProgressRequest(1L);
        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
            mockChallengeUserRepository);
        ProgressController progressController = new ProgressController(progressService);
        ProgressDTO progressDTO = new ProgressDTO(newProgress);
        CommonResponse result = progressController.patchProgress(newUser, newChallenge.getId(),
            progressRequest);

        //then
        ArgumentCaptor<Long> pCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> wCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.verify(mockProgressRepository, Mockito.times(1))
            .findByChallengeIdAndWeeks(pCaptor.capture(), wCaptor.capture());

        Assertions.assertEquals(newProgress.getChallenge().getId(), pCaptor.getValue());
        Assertions.assertEquals(newProgress.getWeeks(), wCaptor.getValue());

        Assertions.assertNotEquals(progressDTO, result.getData());
    }

    @Test
    @DisplayName("돈길 걷기 요청 시, 챌린지를 만든 유저가 아닐 때 403 에러 테스트")
    public void testIfSavingsNotProgressUserForbbidenErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest("이자율 받기", "전자제품", "에어팟 사기", 30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newUser1 = User.builder().id(2L).username("user").isFemale(true).birthday("19990623")
            .authenticationCode("code1").provider("kakao").isKid(true).refreshToken("token1")
            .build();

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

        Progress newProgress = Progress.builder()
            .id(1L)
            .challenge(newChallenge)
            .weeks(1L)
            .isAchieved(false)
            .build();

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
        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(), 1L))
            .thenReturn(Optional.ofNullable(newProgress));

        //when
        ProgressRequest progressRequest = new ProgressRequest(1L);
        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
            mockChallengeUserRepository);
        ProgressController progressController = new ProgressController(progressService);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> progressController.patchProgress(newUser1, newChallenge.getId(),
                progressRequest));
    }

    @Test
    @DisplayName("돈길 걷기 요청 시, 아직 생성되지 않은 주차의 프로그레스라면 400 에러 테스트")
    public void testIfSavingsNotExistProgressBadRequestErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);

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

        Progress newProgress = Progress.builder()
            .id(1L)
            .challenge(newChallenge)
            .weeks(1L)
            .isAchieved(false)
            .build();

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
        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(), 1L))
            .thenReturn(Optional.ofNullable(newProgress));

        //when
        ProgressRequest progressRequest = new ProgressRequest(2L);
        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
            mockChallengeUserRepository);
        ProgressController progressController = new ProgressController(progressService);

        //then
        Assertions.assertThrows(BadRequestException.class,
            () -> progressController.patchProgress(newUser, newChallenge.getId(),
                progressRequest));
    }
}
