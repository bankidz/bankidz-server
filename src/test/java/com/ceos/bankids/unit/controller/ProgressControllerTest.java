//package com.ceos.bankids.unit.controller;
//
//import com.ceos.bankids.config.CommonResponse;
//import com.ceos.bankids.constant.ChallengeStatus;
//import com.ceos.bankids.controller.NotificationController;
//import com.ceos.bankids.controller.ProgressController;
//import com.ceos.bankids.controller.request.ChallengeRequest;
//import com.ceos.bankids.domain.AbstractTimestamp;
//import com.ceos.bankids.domain.Challenge;
//import com.ceos.bankids.domain.ChallengeCategory;
//import com.ceos.bankids.domain.ChallengeUser;
//import com.ceos.bankids.domain.Kid;
//import com.ceos.bankids.domain.Parent;
//import com.ceos.bankids.domain.Progress;
//import com.ceos.bankids.domain.TargetItem;
//import com.ceos.bankids.domain.User;
//import com.ceos.bankids.dto.ProgressDTO;
//import com.ceos.bankids.exception.BadRequestException;
//import com.ceos.bankids.exception.ForbiddenException;
//import com.ceos.bankids.repository.ChallengeRepository;
//import com.ceos.bankids.repository.ChallengeUserRepository;
//import com.ceos.bankids.repository.KidRepository;
//import com.ceos.bankids.repository.ProgressRepository;
//import com.ceos.bankids.service.ProgressServiceImpl;
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mockito;
//import org.springframework.test.util.ReflectionTestUtils;
//
//public class ProgressControllerTest {
//
//
//    // Enum ChallengeStatus
//    private static final ChallengeStatus pending = ChallengeStatus.PENDING;
//    private static final ChallengeStatus walking = ChallengeStatus.WALKING;
//    private static final ChallengeStatus achieved = ChallengeStatus.ACHIEVED;
//    private static final ChallengeStatus failed = ChallengeStatus.FAILED;
//    private static final ChallengeStatus rejected = ChallengeStatus.REJECTED;
//    private static final ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기",
//        "전자제품", "테스트용 돈길", 30L, 30000L,
//        10000L, 3L, "test");
//    private static final User son = User.builder().id(1L).username("son").birthday("19990623")
//        .authenticationCode("code")
//        .provider("kakao").isKid(true).isFemale(false).refreshToken("token").build();
//    private static final User mom = User.builder().id(2L).username("mom").birthday("19440505")
//        .authenticationCode("code").provider("kakao").isKid(false).isFemale(true)
//        .refreshToken("token").build();
//    private static final User father = User.builder().id(3L).username("father").isKid(false)
//        .isFemale(false).authenticationCode("code").provider("kakao").refreshToken("token").build();
//    private static final User daughter = User.builder().id(4L).username("daughter").isKid(true)
//        .isFemale(true).authenticationCode("code").provider("kakao").refreshToken("token").build();
//    private static final Kid sonKid = Kid.builder().id(1L).achievedChallenge(0L).totalChallenge(0L)
//        .user(son).level(0L).savings(0L).build();
//    private static final Parent momParent = Parent.builder().id(1L).acceptedRequest(0L)
//        .totalRequest(0L).user(mom).build();
//    private static final Parent fatherParent = Parent.builder().id(2L).acceptedRequest(0L)
//        .totalRequest(0L).user(father).build();
//    ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
//        .category("이자율 받기").build();
//    TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 프로그레스가 정상적으로 업데이트 되는지 테스트")
//    public void testIfSavingsProgressRowUpdate() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice()).successWeeks(0L)
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(walking)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(false)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now()),
//            Timestamp.class
//        );
//
//        List<Progress> progressList = List.of(newProgress);
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress.getWeeks())).thenReturn(Optional.of(newProgress));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//        ProgressDTO progressDTO = new ProgressDTO(newProgress, newChallenge);
//        CommonResponse<ProgressDTO> result = progressController.patchProgress(son,
//            newChallenge.getId());
//
//        //then
//        ArgumentCaptor<Long> pCaptor = ArgumentCaptor.forClass(Long.class);
//        ArgumentCaptor<Long> wCaptor = ArgumentCaptor.forClass(Long.class);
//
//        Mockito.verify(mockProgressRepository, Mockito.times(1))
//            .findByChallengeIdAndWeeks(pCaptor.capture(), wCaptor.capture());
//
//        Assertions.assertEquals(newProgress.getChallenge().getId(), pCaptor.getValue());
//        Assertions.assertEquals(newProgress.getWeeks(), wCaptor.getValue());
//        Assertions.assertEquals(1L, newChallenge.getSuccessWeeks());
//        Assertions.assertEquals(true, newProgress.getIsAchieved());
//
//        Assertions.assertNotEquals(progressDTO, result.getData());
//    }
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 완주되었을 때, db 정상 업데이트 테스트")
//    public void testIfSavingsProgressChallengeSuccessRowUpdate() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        sonKid.setSavings(20000L);
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(walking).successWeeks(2L)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(true)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now().minusDays(15)),
//            Timestamp.class
//        );
//
//        Progress newProgress1 = Progress.builder()
//            .id(2L)
//            .challenge(newChallenge)
//            .weeks(2L)
//            .isAchieved(true)
//            .build();
//
//        Progress newProgress2 = Progress.builder()
//            .id(3L)
//            .challenge(newChallenge)
//            .weeks(3L)
//            .isAchieved(false)
//            .build();
//
//        List<Progress> progressList = new ArrayList<>();
//        progressList.add(newProgress);
//        progressList.add(newProgress1);
//        progressList.add(newProgress2);
//
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress2.getWeeks())).thenReturn(Optional.of(newProgress2));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//        ProgressDTO progressDTO = new ProgressDTO(newProgress2, newChallenge);
//        CommonResponse<ProgressDTO> result = progressController.patchProgress(son,
//            newChallenge.getId());
//
//        //then
//        ArgumentCaptor<Long> pCaptor = ArgumentCaptor.forClass(Long.class);
//        ArgumentCaptor<Long> wCaptor = ArgumentCaptor.forClass(Long.class);
//
//        Mockito.verify(mockProgressRepository, Mockito.times(1))
//            .findByChallengeIdAndWeeks(pCaptor.capture(), wCaptor.capture());
//
//        Assertions.assertEquals(newProgress.getChallenge().getId(), pCaptor.getValue());
//        Assertions.assertEquals(newProgress2.getWeeks(), wCaptor.getValue());
//        Assertions.assertEquals(achieved, newChallenge.getChallengeStatus());
//        Assertions.assertEquals(30000L, sonKid.getSavings());
//        Assertions.assertEquals(3L, newChallenge.getSuccessWeeks());
//        Assertions.assertEquals(true, newProgress2.getIsAchieved());
//
//        Assertions.assertNotEquals(progressDTO, result.getData());
//    }
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 부모 유저가 접근했을 때, 403 에러")
//    public void testIfSavingsProgressParentUserForbiddenErr() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(walking)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(false)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now()),
//            Timestamp.class
//        );
//
//        List<Progress> progressList = List.of(newProgress);
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress.getWeeks())).thenReturn(Optional.of(newProgress));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//
//        //then
//        Assertions.assertThrows(ForbiddenException.class,
//            () -> progressController.patchProgress(mom, newChallenge.getId()));
//    }
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 걷고 있는 돈길이 아닐 때, 400 에러")
//    public void testIfSavingsProgressNotRunningChallengeForbiddenErr() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(pending)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(false)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now()),
//            Timestamp.class
//        );
//
//        List<Progress> progressList = List.of(newProgress);
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress.getWeeks())).thenReturn(Optional.of(newProgress));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//
//        //then
//        Assertions.assertThrows(BadRequestException.class,
//            () -> progressController.patchProgress(son, newChallenge.getId()));
//    }
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 돈길의 주차를 넘어서는 주차에 접근할 때, 400 에러")
//    public void testIfSavingsProgressBetterThanChallengeWeeksBadRequestErr() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(pending)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(true)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now().minusDays(120)),
//            Timestamp.class
//        );
//
//        Progress newProgress1 = Progress.builder()
//            .id(2L)
//            .challenge(newChallenge)
//            .weeks(2L)
//            .isAchieved(true)
//            .build();
//
//        Progress newProgress2 = Progress.builder()
//            .id(3L)
//            .challenge(newChallenge)
//            .weeks(3L)
//            .isAchieved(false)
//            .build();
//
//        List<Progress> progressList = new ArrayList<>();
//        progressList.add(newProgress);
//        progressList.add(newProgress1);
//        progressList.add(newProgress2);
//
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress2.getWeeks())).thenReturn(Optional.of(newProgress2));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//
//        //then
//        Assertions.assertThrows(BadRequestException.class,
//            () -> progressController.patchProgress(son,
//                newChallenge.getId()));
//    }
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 이미 걸은 주차일 때, 400 에러")
//    public void testIfSavingsProgressAlreadyRunningWeeksBadRequestErr() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(walking)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(true)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now().minusDays(15)),
//            Timestamp.class
//        );
//
//        Progress newProgress1 = Progress.builder()
//            .id(2L)
//            .challenge(newChallenge)
//            .weeks(2L)
//            .isAchieved(true)
//            .build();
//
//        Progress newProgress2 = Progress.builder()
//            .id(3L)
//            .challenge(newChallenge)
//            .weeks(3L)
//            .isAchieved(true)
//            .build();
//
//        List<Progress> progressList = new ArrayList<>();
//        progressList.add(newProgress);
//        progressList.add(newProgress1);
//        progressList.add(newProgress2);
//
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress2.getWeeks())).thenReturn(Optional.of(newProgress2));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//
//        //then
//        Assertions.assertThrows(BadRequestException.class,
//            () -> progressController.patchProgress(son,
//                newChallenge.getId()));
//    }
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 권한이 없는 유저가 접근했을 때, 403 에러")
//    public void testIfSavingsProgressNotAuthUserForbiddenErr() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(walking)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(true)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now().minusDays(15)),
//            Timestamp.class
//        );
//
//        Progress newProgress1 = Progress.builder()
//            .id(2L)
//            .challenge(newChallenge)
//            .weeks(2L)
//            .isAchieved(true)
//            .build();
//
//        Progress newProgress2 = Progress.builder()
//            .id(3L)
//            .challenge(newChallenge)
//            .weeks(3L)
//            .isAchieved(true)
//            .build();
//
//        List<Progress> progressList = new ArrayList<>();
//        progressList.add(newProgress);
//        progressList.add(newProgress1);
//        progressList.add(newProgress2);
//
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress2.getWeeks())).thenReturn(Optional.of(newProgress2));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//
//        //then
//        Assertions.assertThrows(ForbiddenException.class,
//            () -> progressController.patchProgress(daughter,
//                newChallenge.getId()));
//    }
//
//    @Test
//    @DisplayName("돈길 걷기 요청 시, 주차가 한참 지났을 때, 400에러")
//    public void testIfSavingsProgressNotExistWalkingProgressBadRequestErr() {
//
//        //given
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        NotificationController mockNotificationController = Mockito.mock(
//            NotificationController.class);
//
//        son.setKid(sonKid);
//        mom.setParent(momParent);
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(challengeRequest.getIsMom() ? mom : father)
//            .totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeStatus(walking)
//            .interestRate(challengeRequest.getInterestRate())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
//            .filename(challengeRequest.getFileName()).build();
//
//        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
//            .member("parent").user(son).build();
//
//        Progress newProgress = Progress.builder()
//            .id(1L)
//            .challenge(newChallenge)
//            .weeks(1L)
//            .isAchieved(true)
//            .build();
//
//        ReflectionTestUtils.setField(
//            newProgress,
//            AbstractTimestamp.class,
//            "createdAt",
//            Timestamp.valueOf(LocalDateTime.now().minusDays(40)),
//            Timestamp.class
//        );
//
//        Progress newProgress1 = Progress.builder()
//            .id(2L)
//            .challenge(newChallenge)
//            .weeks(2L)
//            .isAchieved(true)
//            .build();
//
//        Progress newProgress2 = Progress.builder()
//            .id(3L)
//            .challenge(newChallenge)
//            .weeks(3L)
//            .isAchieved(false)
//            .build();
//
//        List<Progress> progressList = new ArrayList<>();
//        progressList.add(newProgress);
//        progressList.add(newProgress1);
//        progressList.add(newProgress2);
//
//        newChallenge.setProgressList(progressList);
//
//        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallenge));
//        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
//            .thenReturn(Optional.of(newChallengeUser));
//        Mockito.when(mockProgressRepository.findByChallengeIdAndWeeks(newChallenge.getId(),
//            newProgress2.getWeeks())).thenReturn(Optional.of(newProgress2));
//
//        //when
//        ProgressServiceImpl progressService = new ProgressServiceImpl(mockProgressRepository,
//            mockChallengeUserRepository, mockChallengeRepository,
//            mockKidRepository, mockNotificationController);
//        ProgressController progressController = new ProgressController(progressService);
//
//        //then
//        Assertions.assertThrows(BadRequestException.class,
//            () -> progressController.patchProgress(son,
//                newChallenge.getId()));
//    }
//}
