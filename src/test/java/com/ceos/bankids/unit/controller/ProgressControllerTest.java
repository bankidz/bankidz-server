package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.ProgressController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.AbstractTimestamp;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.service.ProgressServiceImpl;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L,
            150000L, 10000L, 15L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newParent = User.builder().id(2L).username("parent1").isFemale(true)
            .birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(false).refreshToken("token")
            .build();

        Kid newKid = Kid.builder().user(newUser).savings(0L).build();
        newUser.setKid(newKid);

        Parent parent = Parent.builder().user(newParent).savings(0L).build();
        newParent.setParent(parent);

        newParent.setParent(parent);

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(newParent)
            .isAchieved(1L).totalPrice(challengeRequest.getTotalPrice())
            .successWeeks(0L)
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

        ReflectionTestUtils.setField(
            newProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        List<Progress> progressList = List.of(newProgress);
        newChallenge.setProgressList(progressList);

        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
            newProgress.getWeeks())).thenReturn(Optional.of(newProgress));

        //when
        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
            mockChallengeUserRepository, mockChallengeRepository, mockFamilyUserRepository,
            mockKidRepository, mockParentRepository);
        ProgressController progressController = new ProgressController(progressService);
        ProgressDTO progressDTO = new ProgressDTO(newProgress);
        CommonResponse result = progressController.patchProgress(newUser, newChallenge.getId());

        //then
        ArgumentCaptor<Long> pCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> wCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.verify(mockProgressRepository, Mockito.times(1))
            .findByChallengeIdAndWeeks(pCaptor.capture(), wCaptor.capture());

        Assertions.assertEquals(newProgress.getChallenge().getId(), pCaptor.getValue());
        Assertions.assertEquals(newProgress.getWeeks(), wCaptor.getValue());
        Assertions.assertEquals(1L, newChallenge.getSuccessWeeks());
        Assertions.assertEquals(true, newProgress.getIsAchieved());

        Assertions.assertNotEquals(progressDTO, result.getData());
    }

    @Test
    @DisplayName("돈길 걷기 요청 시, 완주되었을 때, db 정상 업데이트 테스트")
    public void testIfSavingsProgressChallengeSuccessRowUpdate() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L,
            30000L, 10000L, 3L);

        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();

        User newParent = User.builder().id(2L).username("parent1").isFemale(true)
            .birthday("19990521")
            .authenticationCode("code").provider("kakao").isKid(false).refreshToken("token")
            .build();

        Kid newKid = Kid.builder().user(newUser).savings(0L).build();
        newUser.setKid(newKid);

        Parent parent = Parent.builder().user(newParent).savings(0L).build();
        newParent.setParent(parent);

        newParent.setParent(parent);

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();

        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(newParent)
            .isAchieved(1L).totalPrice(challengeRequest.getTotalPrice())
            .successWeeks(2L)
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(2L)
            .interestRate(challengeRequest.getInterestRate()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(newUser).build();

        Progress newProgress = Progress.builder()
            .id(1L)
            .challenge(newChallenge)
            .weeks(1L)
            .isAchieved(true)
            .build();

        ReflectionTestUtils.setField(
            newProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusDays(15)),
            Timestamp.class
        );

        Progress newProgress1 = Progress.builder()
            .id(2L)
            .challenge(newChallenge)
            .weeks(2L)
            .isAchieved(true)
            .build();

        Progress newProgress2 = Progress.builder()
            .id(3L)
            .challenge(newChallenge)
            .weeks(3L)
            .isAchieved(false)
            .build();

        List<Progress> progressList = new ArrayList<>();
        progressList.add(newProgress);
        progressList.add(newProgress1);
        progressList.add(newProgress2);

        newChallenge.setProgressList(progressList);

        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
            newProgress2.getWeeks())).thenReturn(Optional.of(newProgress2));

        //when
        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
            mockChallengeUserRepository, mockChallengeRepository, mockFamilyUserRepository,
            mockKidRepository, mockParentRepository);
        ProgressController progressController = new ProgressController(progressService);
        ProgressDTO progressDTO = new ProgressDTO(newProgress2);
        CommonResponse result = progressController.patchProgress(newUser, newChallenge.getId());

        //then
        ArgumentCaptor<Long> pCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> wCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.verify(mockProgressRepository, Mockito.times(1))
            .findByChallengeIdAndWeeks(pCaptor.capture(), wCaptor.capture());

        Assertions.assertEquals(newProgress.getChallenge().getId(), pCaptor.getValue());
        Assertions.assertEquals(newProgress2.getWeeks(), wCaptor.getValue());
        Assertions.assertEquals(2L, newChallenge.getIsAchieved());
        Assertions.assertEquals(0L, newChallenge.getStatus());
        Assertions.assertEquals(39000L, newKid.getSavings());
        Assertions.assertEquals(3L, newChallenge.getSuccessWeeks());
        Assertions.assertEquals(true, newProgress2.getIsAchieved());

        Assertions.assertNotEquals(progressDTO, result.getData());
    }

}
