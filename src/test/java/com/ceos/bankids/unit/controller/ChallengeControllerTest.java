package com.ceos.bankids.unit.controller;

import com.ceos.bankids.Enum.ChallengeStatus;
import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.controller.ChallengeController;
import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.AbstractTimestamp;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Comment;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Kid;
import com.ceos.bankids.domain.Parent;
import com.ceos.bankids.domain.Progress;
import com.ceos.bankids.domain.TargetItem;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.dto.WeekDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.ChallengeCategoryRepository;
import com.ceos.bankids.repository.ChallengeRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.ceos.bankids.repository.CommentRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import com.ceos.bankids.repository.KidRepository;
import com.ceos.bankids.repository.ParentRepository;
import com.ceos.bankids.repository.ProgressRepository;
import com.ceos.bankids.repository.TargetItemRepository;
import com.ceos.bankids.service.ChallengeServiceImpl;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class ChallengeControllerTest {

    // Enum ChallengeStatus
    private static final ChallengeStatus pending = ChallengeStatus.PENDING;
    private static final ChallengeStatus walking = ChallengeStatus.WALKING;
    private static final ChallengeStatus achieved = ChallengeStatus.ACHIEVED;
    private static final ChallengeStatus failed = ChallengeStatus.FAILED;
    private static final ChallengeStatus rejected = ChallengeStatus.REJECTED;
    private static final ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기",
        "전자제품", "테스트용 돈길", 30L, 50000L,
        10000L, 5L, "test");
    private static final User son = User.builder().id(1L).username("son").birthday("19990623")
        .authenticationCode("code")
        .provider("kakao").isKid(true).isFemale(false).refreshToken("token").build();
    private static final User mom = User.builder().id(2L).username("mom").birthday("19440505")
        .authenticationCode("code").provider("kakao").isKid(false).isFemale(true)
        .refreshToken("token").build();
    private static final User father = User.builder().id(3L).username("father").isKid(false)
        .isFemale(false).authenticationCode("code").provider("kakao").refreshToken("token").build();
    private static final User daughter = User.builder().id(4L).username("daughter").isKid(true)
        .isFemale(true).authenticationCode("code").provider("kakao").refreshToken("token").build();
    private static final Kid sonKid = Kid.builder().id(1L).achievedChallenge(0L).totalChallenge(0L)
        .user(son).level(0L).savings(0L).deleteChallenge(null).build();
    private static final Parent momParent = Parent.builder().id(1L).acceptedRequest(0L)
        .totalRequest(0L).user(mom).build();
    private static final Parent fatherParent = Parent.builder().id(2L).acceptedRequest(0L)
        .totalRequest(0L).user(father).build();
    ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
        .category("이자율 받기").build();
    TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();

    @Test
    @DisplayName("챌린지 생성 성공 시, 결과 반환과 디비에 정상 저장되는지 확인")
    public void testIfPostChallengeSuccessReturnResultAndSaveDb() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        //given

        sonKid.setDeleteChallenge(null);
        son.setKid(sonKid);
        mom.setParent(momParent);
        father.setParent(fatherParent);

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("family").build();

        FamilyUser newFamilySon = FamilyUser.builder().user(son).family(newFamily).build();

        FamilyUser newFamilyMom = FamilyUser.builder().user(mom).family(newFamily).build();

        FamilyUser newFamilyFather = FamilyUser.builder().user(father).family(newFamily).build();

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilySon);
        familyUserList.add(newFamilyMom);
        familyUserList.add(newFamilyFather);

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilySon));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<ChallengeDTO> result = challengeController.postChallenge(son,
            challengeRequest);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).save(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 생성 요청 시, 부모가 접근했을 때 403 에러 확인")
    public void testIfPostChallengeIsKidFalseForbiddenErr() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        //given

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().user(son).family(newFamily).build();

        FamilyUser newFamilyFather = FamilyUser.builder().user(father).family(newFamily).build();

        FamilyUser newFamilyMom = FamilyUser.builder().user(mom).family(newFamily).build();

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyFather);
        familyUserList.add(newFamilyMom);

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> challengeController.postChallenge(mom, challengeRequest));
    }

//    @Test
//    @DisplayName("챌린지 생성 요청 시, 일요일에 접근했을 때, 에러 확인")
//    public void testIfPostChallengeSunDayForbiddenErr() {
//
//        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
//        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
//            ChallengeCategoryRepository.class);
//        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
//        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
//        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
//            ChallengeUserRepository.class);
//        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
//        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
//        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
//        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
//        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
//        BindingResult mockBindingResult = Mockito.mock(BindingResult.class);
//        //given
//        ChallengeRequest challengeRequest = new ChallengeRequest(true, "이자율 받기", "전자제품", "에어팟 사기",
//            30L,
//            150000L, 10000L, 15L);
//
//        User newUser = User.builder().id(1L).username("user1").isFemale(true).birthday("19990521")
//            .authenticationCode("code").provider("kakao").isKid(true).refreshToken("token").build();
//
//        User newParent = User.builder().id(2L).username("parent1").isFemale(true)
//            .birthday("19990623")
//            .authenticationCode("code1").provider("kakao").isKid(false).refreshToken("token1")
//            .build();
//
//        Parent parent = Parent.builder().user(newParent).totalChallenge(0L).totalRequest(0L)
//            .acceptedRequest(0L).savings(0L).build();
//        newParent.setParent(parent);
//
//        User newFather = User.builder().id(3L).username("parent2").isFemale(false)
//            .birthday("19990623")
//            .authenticationCode("code1").provider("kakao").isKid(false).refreshToken("token1")
//            .build();
//
//        ChallengeCategory newChallengeCategory = ChallengeCategory.builder().id(1L)
//            .category("이자율 받기").build();
//
//        TargetItem newTargetItem = TargetItem.builder().id(1L).name("전자제품").build();
//
//        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
//            .contractUser(newParent)
//            .isAchieved(1L).totalPrice(challengeRequest.getTotalPrice())
//            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
//            .challengeCategory(newChallengeCategory).targetItem(newTargetItem).status(1L)
//            .interestRate(challengeRequest.getInterestRate()).build();
//
//        Family newFamily = Family.builder().code("asdfasdf").build();
//
//        FamilyUser newFamilyUser = FamilyUser.builder().user(newUser).family(newFamily).build();
//
//        FamilyUser newFamilyFather = FamilyUser.builder().user(newFather).family(newFamily).build();
//
//        FamilyUser newFamilyParent = FamilyUser.builder().user(newParent).family(newFamily).build();
//
//        List<FamilyUser> familyUserList = new ArrayList<>();
//        familyUserList.add(newFamilyFather);
//        familyUserList.add(newFamilyParent);
//
//        Mockito.when(mockFamilyUserRepository.findByUserId(newUser.getId()))
//            .thenReturn(Optional.ofNullable(newFamilyUser));
//        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
//            .thenReturn(familyUserList);
//
//        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
//        Mockito.when(mockChallengeRepository.findById(1L))
//            .thenReturn(Optional.ofNullable(newChallenge));
//        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
//            .thenReturn(newTargetItem);
//        Mockito.when(
//                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
//            .thenReturn(newChallengeCategory);
//
//        //when
//        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
//            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
//            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
//            mockKidRepository, mockParentRepository);
//        ChallengeController challengeController = new ChallengeController(challengeService);
//
//        //then
//        Assertions.assertThrows(ForbiddenException.class,
//            () -> challengeController.postChallenge(newUser, challengeRequest,
//                mockBindingResult));
//    }

    @Test
    @DisplayName("챌린지 생성 개수 제한 도달 시, 403 에러 테스트")
    public void testIfPostChallengeMaxCountForbiddenErr() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        //given

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge2 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge3 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge4 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge5 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge6 = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(failed)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("asdfasdf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().user(son).family(newFamily).build();

        FamilyUser newFamilyFather = FamilyUser.builder().user(father).family(newFamily).build();

        FamilyUser newFamilyParent = FamilyUser.builder().user(mom).family(newFamily).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().user(son)
            .challenge(newChallenge1).member("parent").build();

        ChallengeUser newChallengeUser2 = ChallengeUser.builder().user(son)
            .challenge(newChallenge2).member("parent").build();

        ChallengeUser newChallengeUser3 = ChallengeUser.builder().user(son)
            .challenge(newChallenge3).member("parent").build();

        ChallengeUser newChallengeUser4 = ChallengeUser.builder().user(son)
            .challenge(newChallenge4).member("parent").build();

        ChallengeUser newChallengeUser5 = ChallengeUser.builder().user(son)
            .challenge(newChallenge5).member("parent").build();

        ChallengeUser newChallengeUser6 = ChallengeUser.builder().user(son)
            .challenge(newChallenge6).member("parent").build();

        List<ChallengeUser> challengeUserList = List.of(newChallengeUser1, newChallengeUser2,
            newChallengeUser3, newChallengeUser4, newChallengeUser5, newChallengeUser6);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyFather);
        familyUserList.add(newFamilyParent);

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> challengeController.postChallenge(son, challengeRequest));
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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        mom.setParent(momParent);

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().user(son).family(newFamily).build();

        FamilyUser newFamilyParent = FamilyUser.builder().user(mom).family(newFamily).build();

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyParent);

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);
        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<ChallengeDTO> result = challengeController.postChallenge(son,
            challengeRequest);

        //then
        ChallengeDTO challengeDTO = new ChallengeDTO(newChallenge, null, null);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).save(cCaptor.capture());
        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).save(cuCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());
        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTO).getData(), result.getData());
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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        TargetItem notExistItem = TargetItem.builder().id(2L).name("없는 아이템").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(notExistItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().user(son).family(newFamily).build();

        FamilyUser newFamilyParent = FamilyUser.builder().user(mom).family(newFamily).build();

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyParent);

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            newChallenge.getChallengeCategory().getCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(newChallenge.getTargetItem().getName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () ->
            challengeController.postChallenge(son, challengeRequest));
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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        ChallengeCategory notExistCategory = ChallengeCategory.builder().id(2L)
            .category("형제와 경쟁 하기").build();

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(notExistCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().user(son).family(newFamily).build();

        FamilyUser newFamilyParent = FamilyUser.builder().user(mom).family(newFamily).build();

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyParent);

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);

        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            newChallenge.getChallengeCategory().getCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(newChallenge.getTargetItem().getName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () ->
            challengeController.postChallenge(son, challengeRequest));

    }

    @Test
    @DisplayName("챌린지 생성 시, 가족 없음 403에러 테스트")
    public void testIfMakeChallengeNotExistFamilyForbiddenErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            newChallenge.getChallengeCategory().getCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(newChallenge.getTargetItem().getName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(ForbiddenException.class, () ->
            challengeController.postChallenge(son, challengeRequest));

    }

    @Test
    @DisplayName("챌린지 생성 시, 부모 없음 400에러 테스트")
    public void testIfMakeChallengeNotExistParentBadRequestErr() {

        //given
        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);

        Challenge newChallenge = Challenge.builder().title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().user(son).family(newFamily).build();

        List<FamilyUser> familyUserList = new ArrayList<>();

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);

        Mockito.when(mockChallengeCategoryRepository.findByCategory(
            newChallenge.getChallengeCategory().getCategory())).thenReturn(newChallengeCategory);
        Mockito.when(mockTargetItemRepository.findByName(newChallenge.getTargetItem().getName()))
            .thenReturn(newTargetItem);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () ->
            challengeController.postChallenge(son, challengeRequest));

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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);
        sonKid.setDeleteChallenge(null);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Progress newProgress1 = Progress.builder().id(2L).weeks(2L).isAchieved(true)
            .challenge(newChallenge).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));

        List<Progress> progressList = List.of(newProgress, newProgress1);
        newChallenge.setProgressList(progressList);

        ChallengeDTO newDeleteChallengeDTO = new ChallengeDTO(newChallenge, null, null);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse<ChallengeDTO> result = challengeController.deleteChallenge(son, challengeId);

        //then
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).delete(cuCaptor.capture());
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertNotEquals(sonKid.getDeleteChallenge(), null);

        Assertions.assertEquals(CommonResponse.onSuccess(newDeleteChallengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 삭제 시, 삭제한지 2주가 경과된 유저가 시도 했을 때 정상적으로 없어지는지 테스트")
    public void testIfDeleteTwoWeeksUserChallengeIsNull() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        LocalDateTime now = LocalDateTime.of(2022, 6, 10, 3, 3);
        Timestamp timestamp = Timestamp.valueOf(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.add(Calendar.DATE, -15);

        ReflectionTestUtils.setField(
            sonKid,
            Kid.class,
            "deleteChallenge",
            Timestamp.valueOf(now),
            Timestamp.class
        );

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));

        List<Progress> progressList = List.of(newProgress);
        newChallenge.setProgressList(progressList);

        ChallengeDTO newDeleteChallengeDTO = new ChallengeDTO(newChallenge, null, null);
        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse<ChallengeDTO> result = challengeController.deleteChallenge(son, challengeId);

        //then
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).delete(cuCaptor.capture());
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(newDeleteChallengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 삭제 시, 삭제한지 2주가 경과된 유저가 시도 했을 때 (해가 넘어갈 시) 정상적으로 없어지는지 테스트")
    public void testIfDeleteTwoWeeksDiffYearUserChallengeIsNull() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        LocalDateTime now = LocalDateTime.of(2022, 6, 10, 3, 3);
        Timestamp timestamp = Timestamp.valueOf(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.add(Calendar.DATE, -15);

        ReflectionTestUtils.setField(
            sonKid,
            Kid.class,
            "deleteChallenge",
            Timestamp.valueOf(now.minusYears(1)),
            Timestamp.class
        );

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));

        List<Progress> progressList = List.of(newProgress);
        newChallenge.setProgressList(progressList);

        ChallengeDTO newDeleteChallengeDTO = new ChallengeDTO(newChallenge, null, null);
        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse<ChallengeDTO> result = challengeController.deleteChallenge(son, challengeId);

        //then
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).delete(cuCaptor.capture());
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(newDeleteChallengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 삭제 시, 삭제한지 2주가 경과되지 않은 유저가 거절당한 돈길 삭제를 시도 했을 때 정상적으로 없어지는지 테스트")
    public void testIfDeleteRejectChallengeIsNull() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.add(Calendar.DATE, -15);

        ReflectionTestUtils.setField(
            sonKid,
            Kid.class,
            "deleteChallenge",
            Timestamp.valueOf(now),
            Timestamp.class
        );

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(rejected)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Comment newComment = Comment.builder().id(1L).challenge(newChallenge).user(mom)
            .content("아쉽구나").build();

        newChallenge.setComment(newComment);

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));

        List<Progress> progressList = List.of(newProgress);
        newChallenge.setProgressList(progressList);

        ChallengeDTO newDeleteChallengeDTO = new ChallengeDTO(newChallenge, null, null);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse<ChallengeDTO> result = challengeController.deleteChallenge(son, challengeId);

        //then
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).delete(cuCaptor.capture());
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(newDeleteChallengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 삭제 시, 삭제한지 2주가 경과되지 않은 유저가 제안중인 돈길 삭제를 시도 했을 때 정상적으로 없어지는지 테스트")
    public void testIfDeletePendingChallengeIsNull() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.add(Calendar.DATE, -15);

        ReflectionTestUtils.setField(
            sonKid,
            Kid.class,
            "deleteChallenge",
            Timestamp.valueOf(now),
            Timestamp.class
        );

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Comment newComment = Comment.builder().id(1L).challenge(newChallenge).user(mom)
            .content("아쉽구나").build();

        newChallenge.setComment(newComment);

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));

        List<Progress> progressList = List.of(newProgress);
        newChallenge.setProgressList(progressList);

        ChallengeDTO newDeleteChallengeDTO = new ChallengeDTO(newChallenge, null, null);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse<ChallengeDTO> result = challengeController.deleteChallenge(son, challengeId);

        //then
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).delete(cuCaptor.capture());
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(newDeleteChallengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 삭제 시, 삭제한지 2주가 경과되지 않은 유저가 실패한 돈길 삭제를 시도 했을 때 정상적으로 없어지는지 테스트")
    public void testIfDeleteFailureChallengeIsNull() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.add(Calendar.DATE, -15);

        ReflectionTestUtils.setField(
            sonKid,
            Kid.class,
            "deleteChallenge",
            Timestamp.valueOf(now),
            Timestamp.class
        );

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(failed)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));

        List<Progress> progressList = List.of(newProgress);
        newChallenge.setProgressList(progressList);

        ChallengeDTO newDeleteChallengeDTO = new ChallengeDTO(newChallenge, null, null);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();
        CommonResponse<ChallengeDTO> result = challengeController.deleteChallenge(son, challengeId);

        //then
        ArgumentCaptor<ChallengeUser> cuCaptor = ArgumentCaptor.forClass(ChallengeUser.class);
        ArgumentCaptor<Challenge> cCaptor = ArgumentCaptor.forClass(Challenge.class);

        Mockito.verify(mockChallengeUserRepository, Mockito.times(1)).delete(cuCaptor.capture());
        Mockito.verify(mockChallengeRepository, Mockito.times(1)).delete(cCaptor.capture());

        Assertions.assertEquals(newChallenge, cCaptor.getValue());

        Assertions.assertEquals(newChallengeUser, cuCaptor.getValue());

        Assertions.assertEquals(CommonResponse.onSuccess(newDeleteChallengeDTO), result);
    }

    @Test
    @DisplayName("챌린지 삭제 시, 챌린지를 생성한 유저가 아닌 경우 403 에러 테스트")
    public void testIfNotAuthUserDeleteChallengeForbidden() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L).user(daughter).family(newFamily)
            .build();

        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(daughter.getId()))
            .thenReturn(Optional.of(newFamilyUser1));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();

        //then
        Assertions.assertThrows(ForbiddenException.class, () ->
            challengeController.deleteChallenge(daughter, challengeId));
    }

    @Test
    @DisplayName("돈길 삭제한지 2주 안된 유저가 접근 시, 403 에러 테스트")
    public void testIfOverTwoWeekChallengeTryDeleteForbidden() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        ReflectionTestUtils.setField(
            sonKid,
            Kid.class,
            "deleteChallenge",
            Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
            Timestamp.class
        );

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge).build();

        Progress newProgress2 = Progress.builder().id(2L).weeks(2L).isAchieved(true)
            .challenge(newChallenge).build();

        Family newFamily = Family.builder().id(1L).code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L).user(son).family(newFamily).build();

        List<Progress> progressList = Arrays.asList(newProgress, newProgress2);
        newChallenge.setProgressList(progressList);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.of(newFamilyUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        Long challengeId = newChallenge.getId();

        //then
        Assertions.assertThrows(ForbiddenException.class, () ->
            challengeController.deleteChallenge(son, challengeId));
    }

    @Test
    @DisplayName("챌린지 삭제 시, 챌린지 아이디로 챌린지를 못찾으면 400 에러 테스트")
    public void testIfDeleteChallengeIsNullBadRequestErr() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () ->
            challengeController.deleteChallenge(son, 2L));
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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().challenge(newChallenge1)
            .member("parent").user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge1).build();

        ReflectionTestUtils.setField(
            newProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
            Timestamp.class
        );

        List<Progress> progressList = new ArrayList<>();
        progressList.add(newProgress);

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        progressDTOList.add(new ProgressDTO(newProgress));

        newChallenge1.setProgressList(progressList);

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.save(newChallenge1)).thenReturn(newChallenge1);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser1))
            .thenReturn(newChallengeUser1);
        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<List<ChallengeDTO>> result = challengeController.getListChallenge(son,
            "pending");
        CommonResponse<List<ChallengeDTO>> result1 = challengeController.getListChallenge(son,
            "accept");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<ChallengeDTO> challengeDTOList1 = new ArrayList<>();
        for (ChallengeUser r : challengeUserList) {
            if (r.getChallenge().getChallengeStatus() != walking) {
                challengeDTOList.add(new ChallengeDTO(r.getChallenge(), null, null));

            } else if (r.getChallenge().getChallengeStatus() == walking) {
                challengeDTOList1.add(new ChallengeDTO(r.getChallenge(), progressDTOList, null));
            }
        }

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList).getData(),
            result.getData());
        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList1).getData(),
            result1.getData());

    }

    @Test
    @DisplayName("챌린지 리스트 가져오기 테스트 시 작년에 생성한 progress 정보 가져오기")
    public void testIfLastYearGetListChallengeTest() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().challenge(newChallenge1)
            .member("parent").user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(true)
            .challenge(newChallenge1).build();

        Progress newProgress1 = Progress.builder().id(2L).weeks(2L).isAchieved(true)
            .challenge(newChallenge1).build();

        ReflectionTestUtils.setField(
            newProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusYears(1)),
            Timestamp.class
        );

        List<Progress> progressList = new ArrayList<>();
        progressList.add(newProgress);
        progressList.add(newProgress1);

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        progressDTOList.add(new ProgressDTO(newProgress));
        progressDTOList.add(new ProgressDTO(newProgress1));

        newChallenge1.setProgressList(progressList);

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.save(newChallenge1)).thenReturn(newChallenge1);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser1))
            .thenReturn(newChallengeUser1);
        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<List<ChallengeDTO>> result = challengeController.getListChallenge(son,
            "pending");
        CommonResponse<List<ChallengeDTO>> result1 = challengeController.getListChallenge(son,
            "accept");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<ChallengeDTO> challengeDTOList1 = new ArrayList<>();
        for (ChallengeUser r : challengeUserList) {
            if (r.getChallenge().getChallengeStatus() != walking) {
                challengeDTOList.add(new ChallengeDTO(r.getChallenge(), null, null));

            } else {
                challengeDTOList1.add(new ChallengeDTO(r.getChallenge(), progressDTOList, null));
            }
        }

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList).getData(),
            result.getData());
        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList1).getData(),
            result1.getData());

    }

    @Test
    @DisplayName("챌린지 리스트 가져오기 시, 이자율에 따른 실패 테스트")
    public void testIfGetListChallengeChallengeIsFailureTest() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(30L)
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge2 = Challenge.builder().id(3L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(10L)
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge3 = Challenge.builder().id(4L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(20L)
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().challenge(newChallenge1)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser2 = ChallengeUser.builder().challenge(newChallenge2)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser3 = ChallengeUser.builder().challenge(newChallenge3)
            .member("parent").user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);
        challengeUserList.add(newChallengeUser2);
        challengeUserList.add(newChallengeUser3);

        Progress progress = Progress.builder().id(3L).isAchieved(false).weeks(1L)
            .challenge(newChallenge).build();

        Progress progress1 = Progress.builder().id(4L).isAchieved(false).weeks(2L)
            .challenge(newChallenge).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(newChallenge1).build();

        Progress lowProgress = Progress.builder().id(2L).weeks(1L).isAchieved(false)
            .challenge(newChallenge2).build();

        Progress middleProgress = Progress.builder().id(5L).weeks(1L).isAchieved(false)
            .challenge(newChallenge3).build();

        Progress middleProgress1 = Progress.builder().id(6L).weeks(2L).isAchieved(false)
            .challenge(newChallenge3).build();

        Progress middleProgress2 = Progress.builder().id(7L).weeks(3L).isAchieved(false)
            .challenge(newChallenge3).build();

        Progress middleProgress3 = Progress.builder().id(8L).weeks(4L).isAchieved(false)
            .challenge(newChallenge3).build();

        ReflectionTestUtils.setField(
            lowProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            newProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            progress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            middleProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusDays(24)),
            Timestamp.class
        );

        List<Progress> progressList = new ArrayList<>();
        progressList.add(newProgress);

        List<Progress> progressList1 = new ArrayList<>();
        progressList1.add(progress);
        progressList1.add(progress1);

        List<Progress> progressList2 = new ArrayList<>();
        progressList2.add(lowProgress);

        List<Progress> progressList3 = new ArrayList<>();
        progressList3.add(middleProgress);
        progressList3.add(middleProgress1);
        progressList3.add(middleProgress2);
        progressList3.add(middleProgress3);

        newChallenge.setProgressList(progressList1);

        newChallenge1.setProgressList(progressList);

        newChallenge2.setProgressList(progressList2);

        newChallenge3.setProgressList(progressList3);

        ProgressDTO progressDTO = new ProgressDTO(newProgress);
        ProgressDTO progressDTO1 = new ProgressDTO(progress);
        ProgressDTO progressDTO2 = new ProgressDTO(progress1);
        ProgressDTO lowProgressDTO = new ProgressDTO(lowProgress);

        List<ProgressDTO> lowProgressDTOList = List.of(lowProgressDTO);

        List<ProgressDTO> middleProgressDTOList = progressList3.stream().map(ProgressDTO::new)
            .collect(Collectors.toList());

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        progressDTOList.add(progressDTO);

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.save(newChallenge1)).thenReturn(newChallenge1);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser1))
            .thenReturn(newChallengeUser1);
        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<List<ChallengeDTO>> result1 = challengeController.getListChallenge(son,
            "accept");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<ProgressDTO> resultProgressDTOList = new ArrayList<>();
        resultProgressDTOList.add(progressDTO1);
        resultProgressDTOList.add(progressDTO2);

        challengeDTOList.add(
            new ChallengeDTO(newChallengeUser.getChallenge(), resultProgressDTOList, null));
        challengeDTOList.add(
            new ChallengeDTO(newChallengeUser1.getChallenge(), progressDTOList, null));
        challengeDTOList.add(new ChallengeDTO(newChallenge2, lowProgressDTOList, null));
        challengeDTOList.add(new ChallengeDTO(newChallenge3, middleProgressDTOList, null));

        Assertions.assertEquals(failed, newChallenge3.getChallengeStatus());

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList).getData(),
            result1.getData());

    }

    @Test
    @DisplayName("챌린지 리스트 가져오기 시, 이자율에 따른 실패한 돈길이 이미 있을 때 테스트")
    public void testIfGetListChallengeChallengeIsAlreadyFailureTest() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(failed)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().challenge(newChallenge1)
            .member("parent").user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);

        Progress progress = Progress.builder().id(3L).isAchieved(false).weeks(1L)
            .challenge(newChallenge).build();

        Progress progress1 = Progress.builder().id(4L).isAchieved(false).weeks(2L)
            .challenge(newChallenge).build();

        Progress newProgress = Progress.builder().id(1L).weeks(1L).isAchieved(false)
            .challenge(newChallenge1).build();

        Progress newProgress1 = Progress.builder().id(2L).weeks(2L).isAchieved(false)
            .challenge(newChallenge1).build();

        ReflectionTestUtils.setField(
            newProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            progress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusWeeks(2)),
            Timestamp.class
        );

        List<Progress> progressList = new ArrayList<>();
        progressList.add(newProgress);
        progressList.add(newProgress1);

        List<Progress> progressList1 = new ArrayList<>();
        progressList1.add(progress);
        progressList1.add(progress1);

        newChallenge.setProgressList(progressList1);

        newChallenge1.setProgressList(progressList);

        ProgressDTO progressDTO = new ProgressDTO(newProgress);
        ProgressDTO progressDTO1 = new ProgressDTO(progress);
        ProgressDTO progressDTO2 = new ProgressDTO(progress1);

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        progressDTOList.add(progressDTO);

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.save(newChallenge1)).thenReturn(newChallenge1);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser))
            .thenReturn(newChallengeUser);
        Mockito.when(mockChallengeUserRepository.save(newChallengeUser1))
            .thenReturn(newChallengeUser1);
        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<List<ChallengeDTO>> result1 = challengeController.getListChallenge(son,
            "accept");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<ProgressDTO> resultProgressDTOList = new ArrayList<>();
        resultProgressDTOList.add(progressDTO1);
        resultProgressDTOList.add(progressDTO2);

        challengeDTOList.add(
            new ChallengeDTO(newChallengeUser.getChallenge(), resultProgressDTOList, null));
        challengeDTOList.add(
            new ChallengeDTO(newChallengeUser1.getChallenge(), progressDTOList, null));

        Assertions.assertEquals(CommonResponse.onSuccess(challengeDTOList).getData(),
            result1.getData());
        System.out.println("result1 = " + result1.getData());

    }

    @Test
    @DisplayName("챌린지 리스트 조회 시, 완주한 챌린지 정상 response 테스트")
    public void testIfAchievedChallengeReturn() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(3000L)
            .weekPrice(1000L).weeks(3L)
            .challengeStatus(achieved)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).user(son)
            .challenge(newChallenge)
            .member("parent").build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        Progress progress = Progress.builder().id(1L).isAchieved(true).challenge(newChallenge)
            .weeks(1L).build();

        Progress progress1 = Progress.builder().id(2L).isAchieved(true).challenge(newChallenge)
            .weeks(2L).build();

        Progress progress2 = Progress.builder().id(3L).isAchieved(true).challenge(newChallenge)
            .weeks(3L).build();

        ReflectionTestUtils.setField(
            progress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusDays(25)),
            Timestamp.class
        );

        List<Progress> progressList = List.of(progress, progress1, progress2);

        newChallenge.setProgressList(progressList);

        List<ProgressDTO> progressDTOList = progressList.stream().map(ProgressDTO::new)
            .collect(Collectors.toList());

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<List<ChallengeDTO>> result = challengeController.getListChallenge(son,
            "accept");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        challengeDTOList.add(new ChallengeDTO(newChallenge, progressDTOList, null));

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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        List<ChallengeUser> challengeUserList = new ArrayList<>();

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<List<ChallengeDTO>> result = challengeController.getListChallenge(son,
            "pending");

        //then
        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        for (ChallengeUser r : challengeUserList) {
            challengeDTOList.add(new ChallengeDTO(r.getChallenge(), null, null));
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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge2 = Challenge.builder().id(3L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(rejected)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge3 = Challenge.builder().id(4L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser2 = ChallengeUser.builder().id(3L).challenge(newChallenge2)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser3 = ChallengeUser.builder().id(4L).challenge(newChallenge3)
            .member("parent").user(son).build();

        Family newFamily = Family.builder().id(1L)
            .code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(mom).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(son).build();

        Progress newProgress = Progress.builder().id(1L).challenge(newChallenge1).isAchieved(false)
            .weeks(1L).build();

        Progress successProgress = Progress.builder().id(5L).challenge(newChallenge3)
            .isAchieved(true).weeks(1L).build();

        Progress successProgress1 = Progress.builder().id(6L).challenge(newChallenge3)
            .isAchieved(true).weeks(2L).build();

        Progress successProgress2 = Progress.builder().id(7L).challenge(newChallenge3)
            .isAchieved(true).weeks(3L).build();

        List<Progress> successProgressList = List.of(successProgress, successProgress1,
            successProgress2);

        newChallenge3.setProgressList(successProgressList);

        ReflectionTestUtils.setField(
            successProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now().minusDays(22L)),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            successProgress1,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            successProgress2,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            newProgress,
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        ProgressDTO progressDTO = new ProgressDTO(successProgress);
        ProgressDTO progressDTO1 = new ProgressDTO(successProgress1);
        ProgressDTO progressDTO2 = new ProgressDTO(successProgress2);

        List<ProgressDTO> successProgressDTOList = List.of(progressDTO, progressDTO1, progressDTO2);

        Progress newProgress1 = Progress.builder().id(2L).challenge(newChallenge1).isAchieved(false)
            .weeks(2L).build();

        Comment newComment = Comment.builder().id(1L).challenge(newChallenge2).user(mom)
            .content("아쉽다").build();

        newChallenge2.setComment(newComment);

        List<Progress> progressList = new ArrayList<>();
        progressList.add(newProgress);
        progressList.add(newProgress1);

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        progressDTOList.add(new ProgressDTO(newProgress));

        newChallenge1.setProgressList(progressList);

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);
        challengeUserList.add(newChallengeUser2);
        challengeUserList.add(newChallengeUser3);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        List<ChallengeDTO> challengeDTOList = new ArrayList<>();
        List<ChallengeDTO> challengeDTOList1 = new ArrayList<>();

        challengeDTOList.add(new ChallengeDTO(newChallenge, null, null));
        challengeDTOList.add(new ChallengeDTO(newChallenge2, null, newComment));

        challengeDTOList1.add(new ChallengeDTO(newChallenge1, progressDTOList, null));
        challengeDTOList1.add(new ChallengeDTO(newChallenge3, successProgressDTOList, null));

        Mockito.when(mockFamilyUserRepository.findByUserId(mom.getId()))
            .thenReturn(Optional.of(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<KidChallengeListDTO> result = challengeController.getListKidChallenge(mom,
            son.getKid()
                .getId(), "accept");
        CommonResponse<KidChallengeListDTO> result1 = challengeController.getListKidChallenge(mom,
            son.getKid().getId(), "pending");

        //then
        KidChallengeListDTO kidChallengeListDTOResult = new KidChallengeListDTO(son,
            challengeDTOList);
        KidChallengeListDTO kidChallengeListDTO = new KidChallengeListDTO(son,
            challengeDTOList1);

        Assertions.assertEquals(CommonResponse.onSuccess(kidChallengeListDTO).getData(),
            result.getData());
        Assertions.assertEquals(CommonResponse.onSuccess(kidChallengeListDTOResult).getData(),
            result1.getData());
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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);
        mom.setParent(momParent);

        KidChallengeRequest successKidChallengeRequest = new KidChallengeRequest(true, null);
        KidChallengeRequest falseKidChallengeRequest = new KidChallengeRequest(false, "아쉽구나");

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(son).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(mom).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(son).build();

        List<ProgressDTO> progressDTOList = new ArrayList<>();
        for (int i = 1; i <= newChallenge.getWeeks(); i++) {
            Progress newProgress = Progress.builder().weeks((long) i)
                .challenge(newChallenge)
                .isAchieved(false).build();
            progressDTOList.add(new ProgressDTO(newProgress));
        }

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        Comment newComment = Comment.builder().content(falseKidChallengeRequest.getComment())
            .challenge(newChallenge1).user(mom).build();

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.of(newChallenge1));
        Mockito.when(mockFamilyUserRepository.findByUserId(mom.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<ChallengeDTO> successResult = challengeController.patchChallengeStatus(mom,
            newChallenge.getId(), successKidChallengeRequest);
        CommonResponse<ChallengeDTO> falseResult = challengeController.patchChallengeStatus(mom,
            newChallenge1.getId(), falseKidChallengeRequest);

        //then

        newChallenge.setStatus(2L);
        newChallenge1.setStatus(0L);
        newChallenge1.setComment(newComment);
        ChallengeDTO successChallengeDTO = new ChallengeDTO(newChallenge, progressDTOList, null);
        ChallengeDTO falseChallengeDTO = new ChallengeDTO(newChallenge1, null, newComment);
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
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);
        mom.setParent(momParent);

        KidChallengeRequest kidChallengeRequest = new KidChallengeRequest(true, null);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(son).build();

        Family newFamily = Family.builder().id(1L)
            .code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(mom).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.of(newChallenge1));
        Mockito.when(mockFamilyUserRepository.findByUserId(mom.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        //then

        Assertions.assertThrows(BadRequestException.class, () ->
            challengeController.patchChallengeStatus(mom, 150L,
                kidChallengeRequest));
    }

    //Todo: 이거부터 해야함
    @Test
    @DisplayName("자녀 돈길 요청 수락 / 거절 시, 자녀가 챌린지 생성 개수 제한 도달 시, 403 에러 테스트")
    public void testIfUpdateChallengeStatusMaxCountForbiddenErr() {

        ChallengeCategoryRepository mockChallengeCategoryRepository = Mockito.mock(
            ChallengeCategoryRepository.class);
        TargetItemRepository mockTargetItemRepository = Mockito.mock(TargetItemRepository.class);
        ChallengeRepository mockChallengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeUserRepository mockChallengeUserRepository = Mockito.mock(
            ChallengeUserRepository.class);
        ProgressRepository mockProgressRepository = Mockito.mock(ProgressRepository.class);
        FamilyUserRepository mockFamilyUserRepository = Mockito.mock(FamilyUserRepository.class);
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);
        KidRepository mockKidRepository = Mockito.mock(KidRepository.class);
        ParentRepository mockParentRepository = Mockito.mock(ParentRepository.class);
        //given

        KidChallengeRequest kidChallengeRequest = new KidChallengeRequest(true, null);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge2 = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge3 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge4 = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge5 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Family newFamily = Family.builder().code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().user(son).family(newFamily).build();

        FamilyUser newFamilyFather = FamilyUser.builder().user(father).family(newFamily).build();

        FamilyUser newFamilyParent = FamilyUser.builder().user(mom).family(newFamily).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().user(son)
            .challenge(newChallenge1).member("parent").build();

        ChallengeUser newChallengeUser2 = ChallengeUser.builder().user(son)
            .challenge(newChallenge2).member("parent").build();

        ChallengeUser newChallengeUser3 = ChallengeUser.builder().user(son)
            .challenge(newChallenge3).member("parent").build();

        ChallengeUser newChallengeUser4 = ChallengeUser.builder().user(son)
            .challenge(newChallenge4).member("parent").build();

        ChallengeUser newChallengeUser5 = ChallengeUser.builder().user(son)
            .challenge(newChallenge5).member("parent").build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().user(son)
            .challenge(newChallenge).member("parent").build();

        List<ChallengeUser> challengeUserList = List.of(newChallengeUser1, newChallengeUser2,
            newChallengeUser3, newChallengeUser4, newChallengeUser5, newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyFather);
        familyUserList.add(newFamilyParent);

        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);
        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.of(newChallengeUser));

        Mockito.when(mockChallengeRepository.save(newChallenge)).thenReturn(newChallenge);
        Mockito.when(mockChallengeRepository.findById(1L))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockTargetItemRepository.findByName(newTargetItem.getName()))
            .thenReturn(newTargetItem);
        Mockito.when(
                mockChallengeCategoryRepository.findByCategory(newChallengeCategory.getCategory()))
            .thenReturn(newChallengeCategory);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(ForbiddenException.class,
            () -> challengeController.patchChallengeStatus(mom, newChallenge.getId(),
                kidChallengeRequest));
    }

    @Test
    @DisplayName("자녀 돈길 요청 수락 / 거절 시 , 권한이 없을 때 403에러 테스트")
    public void testIfUpdateChallengeStatusNotAuthUserForbiddenErr() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        KidChallengeRequest kidChallengeRequest = new KidChallengeRequest(true, null);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(pending)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(son).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(mom).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(son).build();

        FamilyUser newFamilyUser2 = FamilyUser.builder().id(3L)
            .family(newFamily).user(father).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);
        familyUserList.add(newFamilyUser2);

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.of(newChallenge1));
        Mockito.when(mockFamilyUserRepository.findByUserId(father.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser2));
        Mockito.when(mockFamilyUserRepository.findByUserId(mom.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        //then

        Assertions.assertThrows(ForbiddenException.class, () ->
            challengeController.patchChallengeStatus(father, newChallenge.getId(),
                kidChallengeRequest));
    }

    @Test
    @DisplayName("자녀 돈길 요청 수락 / 거절 시 , 이미 처리된 돈길일 때 400에러 테스트")
    public void testIfUpdateChallengeStatusAlreadyChallengeBadRequestErr() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        KidChallengeRequest kidChallengeRequest = new KidChallengeRequest(true, null);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent")
            .user(son).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(mom).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockChallengeRepository.findById(newChallenge.getId()))
            .thenReturn(Optional.of(newChallenge));
        Mockito.when(mockChallengeRepository.findById(newChallenge1.getId()))
            .thenReturn(Optional.of(newChallenge1));
        Mockito.when(mockFamilyUserRepository.findByUserId(mom.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByUserId(son.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser1));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser));
        Mockito.when(mockChallengeUserRepository.findByChallengeId(newChallenge1.getId()))
            .thenReturn(Optional.ofNullable(newChallengeUser1));
        assert newFamilyUser != null;
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamilyUser.getFamily()))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class, () ->
            challengeController.patchChallengeStatus(mom, newChallenge.getId(),
                kidChallengeRequest));
    }

    @Test
    @DisplayName("주차 정보 가져오기 API 실행 시, 정상 Response 테스트")
    public void testIfReadWeekInfo() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        Challenge newChallenge1 = Challenge.builder().id(2L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        ChallengeUser newChallengeUser1 = ChallengeUser.builder().id(2L).challenge(newChallenge1)
            .member("parent").user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);
        challengeUserList.add(newChallengeUser1);

        List<Progress> progressList = new ArrayList<>();
        List<Progress> progressList1 = new ArrayList<>();

        for (long i = 1L; i <= challengeRequest.getWeeks(); i++) {
            Progress newProgress = Progress.builder().weeks(i).challenge(newChallenge)
                .isAchieved(false).build();
            Progress newProgress1 = Progress.builder().weeks(i).challenge(newChallenge1)
                .isAchieved(false).build();
            if (i == 1L || i == 2L) {
                newProgress.setIsAchieved(true);
                newProgress1.setIsAchieved(true);
            }
            progressList.add(newProgress);
            progressList1.add(newProgress1);
        }

        ReflectionTestUtils.setField(
            progressList.get(0),
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        ReflectionTestUtils.setField(
            progressList1.get(0),
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        newChallenge.setProgressList(progressList);
        newChallenge1.setProgressList(progressList1);

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<WeekDTO> result = challengeController.getWeekInfo(son);

        //then
        WeekDTO weekDTO1 = new WeekDTO(newChallenge.getWeekPrice() + newChallenge1.getWeekPrice(),
            newChallenge.getWeekPrice() + newChallenge1.getWeekPrice());

        Assertions.assertEquals(weekDTO1, result.getData());
    }

    @Test
    @DisplayName("자녀의 주차 정보 가져오기 API 실행 시, 정상 Response 테스트")
    public void testIfReadKidWeekInfo() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        Family newFamily = Family.builder().id(1L)
            .code("adfadfaf").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(mom).build();

        FamilyUser newFamilyUser1 = FamilyUser.builder().id(2L)
            .family(newFamily).user(son).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);
        familyUserList.add(newFamilyUser1);

        List<Progress> progressList = new ArrayList<>();

        for (long i = 1L; i <= newChallenge.getWeeks(); i++) {
            Progress newProgress = Progress.builder().weeks(i).challenge(newChallenge)
                .isAchieved(false).build();
            if (i == 1L) {
                newProgress.setIsAchieved(true);
            }
            progressList.add(newProgress);
        }

        ReflectionTestUtils.setField(
            progressList.get(0),
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        newChallenge.setProgressList(progressList);

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);

        Mockito.when(mockFamilyUserRepository.findByUserId(mom.getId()))
            .thenReturn(Optional.ofNullable(newFamilyUser));

        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);

        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);
        CommonResponse<WeekDTO> result = challengeController.getKidWeekInfo(mom,
            son.getKid().getId());

        //then
        WeekDTO weekDTO1 = new WeekDTO(newChallenge.getWeekPrice(), newChallenge.getWeekPrice());

        Assertions.assertEquals(weekDTO1, result.getData());
    }

    @Test
    @DisplayName("자녀의 주차 정보 가져오기 API 실행 시, 가족이 없을 때, 400 에러")
    public void testIfReadKidWeekInfoNotExistFamilyBadRequestErr() {

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
        CommentRepository mockCommentRepository = Mockito.mock(CommentRepository.class);

        son.setKid(sonKid);

        Challenge newChallenge = Challenge.builder().id(1L).title(challengeRequest.getTitle())
            .contractUser(challengeRequest.getIsMom() ? mom : father)
            .totalPrice(challengeRequest.getTotalPrice())
            .weekPrice(challengeRequest.getWeekPrice()).weeks(challengeRequest.getWeeks())
            .challengeStatus(walking)
            .interestRate(challengeRequest.getInterestRate())
            .challengeCategory(newChallengeCategory).targetItem(newTargetItem)
            .filename(challengeRequest.getFileName()).build();

        ChallengeUser newChallengeUser = ChallengeUser.builder().id(1L).challenge(newChallenge)
            .member("parent").user(son).build();

        Family newFamily = Family.builder().id(1L)
            .code("family").build();

        FamilyUser newFamilyUser = FamilyUser.builder().id(1L)
            .family(newFamily).user(mom).build();

        List<ChallengeUser> challengeUserList = new ArrayList<>();
        challengeUserList.add(newChallengeUser);

        List<FamilyUser> familyUserList = new ArrayList<>();
        familyUserList.add(newFamilyUser);

        List<Progress> progressList = new ArrayList<>();

        for (long i = 1L; i <= newChallenge.getWeeks(); i++) {
            Progress newProgress = Progress.builder().weeks(i).challenge(newChallenge)
                .isAchieved(false).build();
            if (i == 1L) {
                newProgress.setIsAchieved(true);
            }
            progressList.add(newProgress);
        }

        ReflectionTestUtils.setField(
            progressList.get(0),
            AbstractTimestamp.class,
            "createdAt",
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.class
        );

        newChallenge.setProgressList(progressList);

        Mockito.when(mockChallengeUserRepository.findByUserId(son.getId()))
            .thenReturn(challengeUserList);
        Mockito.when(mockFamilyUserRepository.findByUserId(mom.getId()))
            .thenReturn(Optional.of(newFamilyUser));
        Mockito.when(mockFamilyUserRepository.findByFamily(newFamily))
            .thenReturn(familyUserList);
        //when
        ChallengeServiceImpl challengeService = new ChallengeServiceImpl(mockChallengeRepository,
            mockChallengeCategoryRepository, mockTargetItemRepository, mockChallengeUserRepository,
            mockProgressRepository, mockFamilyUserRepository, mockCommentRepository,
            mockKidRepository, mockParentRepository);
        ChallengeController challengeController = new ChallengeController(challengeService);

        //then
        Assertions.assertThrows(BadRequestException.class,
            () -> challengeController.getKidWeekInfo(mom, son.getKid().getId()));
    }
}
