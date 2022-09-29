package com.ceos.bankids.unit.controller;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ChallengeControllerTest2 {

    // Enum ChallengeStatus
    private static final ChallengeStatus pending = ChallengeStatus.PENDING;
    private static final ChallengeStatus walking = ChallengeStatus.WALKING;
    private static final ChallengeStatus achieved = ChallengeStatus.ACHIEVED;
    private static final ChallengeStatus failed = ChallengeStatus.FAILED;
    private static final ChallengeStatus rejected = ChallengeStatus.REJECTED;

    @Test
    @DisplayName("돈길 생성하기 요청 시, 정상 response 확인")
    public void testIfPostChallengeReqSuccessResponse() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        //given

        // kid
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();
        Kid kid = Kid.builder().id(1L)
            .achievedChallenge(0L)
            .level(1L)
            .savings(0L)
            .user(kidUser)
            .build();

        kidUser.setKid(kid);

        // parent
        User parentUser = User.builder().id(2L)
            .username("user2")
            .isKid(false)
            .isFemale(true)
            .birthday("19660101")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
            30L, 3000L, 18000L, 3000L, 5L, "test");

        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
            .category("이자율 받기").build();
        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));

        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            challengeRequest.getChallengeCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(challengeRequest.getItemName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        FamilyServiceImpl familyService = new FamilyServiceImpl()



    }
}
