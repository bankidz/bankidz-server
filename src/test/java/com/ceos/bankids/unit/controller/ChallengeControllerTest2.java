package com.ceos.bankids.unit.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.controller.ChallengeController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.mapper.ChallengeMapper;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.NotificationRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.ChallengeUserServiceImpl;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    public void testPostChallengeReqSuccessResponse() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);
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
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
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
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

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
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            challengeRequest.getChallengeCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(challengeRequest.getItemName()))
            .thenReturn(newTargetItem);
        Mockito.when(mockFamilyUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(Optional.of(familyUser));
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, kidUser))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = null;
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        // then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.postChallenge(
            kidUser, challengeRequest);
        // response 데이터 검증
        Assertions.assertEquals(challengeDTOCommonResponse.getData(), challengeDTO);
        // 만들어진 돈길 status 검증
        Assertions.assertEquals(challengeDTOCommonResponse.getData().getChallengeStatus(), pending);
        // 부모의 totalRequest + 1 검증
        Assertions.assertEquals(parent.getTotalRequest(), 1L);
    }

    @Test
    @DisplayName("돈길 생성하기 요청 성공 시, challengeUser row 정상 생성 검증")
    public void testPostChallengeIfSuccessMakeChallengeUserRow() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);

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
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
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
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
            .build();

        Parent parent = Parent.builder().id(1L)
            .acceptedRequest(0L)
            .totalRequest(0L)
            .user(parentUser)
            .build();

        parentUser.setParent(parent);

        Family family = Family.builder().id(1L).code("asdfasfd").build();

        FamilyUser familyUser = FamilyUser.builder().id(1L).family(family).user(kidUser).build();
        FamilyUser familyUser1 = FamilyUser.builder().id(2L).family(family).user(parentUser)
            .build();

        List<FamilyUser> familyUserList = List.of(familyUser1);

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
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            challengeRequest.getChallengeCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(challengeRequest.getItemName()))
            .thenReturn(newTargetItem);
        Mockito.when(mockFamilyUserRepository.findByUserId(kidUser.getId()))
            .thenReturn(Optional.of(familyUser));
        Mockito.when(mockFamilyUserRepository.findByFamilyAndUserNot(family, kidUser))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = null;
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        // then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        CommonResponse<ChallengeDTO> challengeDTOCommonResponse = challengeController.postChallenge(
            kidUser, challengeRequest);
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).save(cuCaptor.capture());

        Assertions.assertEquals(newChallenge, cuCaptor.getValue().getChallenge());
    }

    @Test
    @DisplayName("돈길 생성하기 요청 시, 걷고 있는 돈길의 개수가 5개 이상이면 403 에러")
    public void testPostChallengeIfWalkingChallengeHigherThan5ForbiddenException() {
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        NotificationRepository mockNotificationRepository = Mockito.mock(
            NotificationRepository.class);

        //given
        User kidUser = User.builder().id(1L)
            .username("user1")
            .isKid(true)
            .isFemale(true)
            .birthday("19990623")
            .authenticationCode("code")
            .provider("kakao")
            .refreshToken("token")
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
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
            .expoToken("expotoken")
            .serviceOptIn(true)
            .noticeOptIn(true)
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

        Challenge challenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge1 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge2 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge3 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge challenge4 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(parentUser)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .interestPrice(challengeRequest.getInterestPrice())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser challengeUser = ChallengeUser.builder().user(kidUser).challenge(challenge)
            .member("parent").build();

        ChallengeUser challengeUser1 = ChallengeUser.builder().user(kidUser).challenge(challenge1)
            .member("parent").build();

        ChallengeUser challengeUser2 = ChallengeUser.builder().user(kidUser).challenge(challenge2)
            .member("parent").build();

        ChallengeUser challengeUser3 = ChallengeUser.builder().user(kidUser).challenge(challenge3)
            .member("parent").build();

        ChallengeUser challengeUser4 = ChallengeUser.builder().user(kidUser).challenge(challenge4)
            .member("parent").build();

        List<ChallengeUser> challengeUserList = List.of(challengeUser, challengeUser1,
            challengeUser2, challengeUser3, challengeUser4);

        //when
        Mockito.when(mockChallengeUserRepository.findByUserIdAndChallenge_ChallengeStatus(
            kidUser.getId(), walking)).thenReturn(challengeUserList);

        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(
            mockChallengeRepository,
            mockChallengeCategoryRepository,
            mockTargetItemRepository,
            mockProgressRepository,
            mockCommentRepository
        );
        ChallengeUserServiceImpl challengeUserService = new ChallengeUserServiceImpl(
            mockChallengeUserRepository);
        FamilyUserServiceImpl familyUserService = new FamilyUserServiceImpl(
            mockFamilyUserRepository);
        ParentServiceImpl parentService = new ParentServiceImpl(mockParentRepository);
        ExpoNotificationServiceImpl notificationService = new ExpoNotificationServiceImpl(
            mockNotificationRepository);
        KidServiceImpl kidService = null;
        ChallengeMapper challengeMapper = new ChallengeMapper(challengeService, familyUserService,
            challengeUserService, notificationService, parentService, kidService);
        ChallengeController challengeController = new ChallengeController(challengeMapper);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> challengeController.postChallenge(kidUser, challengeRequest));
    }
}
