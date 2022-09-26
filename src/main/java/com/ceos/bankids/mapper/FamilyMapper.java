package com.ceos.bankids.mapper;

import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeCompleteDeleteByKidMapperDTO;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.dto.KidListDTO;
import com.ceos.bankids.service.ChallengeServiceImpl;
import com.ceos.bankids.service.ChallengeUserServiceImpl;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import com.ceos.bankids.service.KidServiceImpl;
import com.ceos.bankids.service.ParentServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import kotlin.jvm.internal.SerializedIr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SerializedIr
@RequiredArgsConstructor
public class FamilyMapper {

    private final FamilyServiceImpl familyService;
    private final FamilyUserServiceImpl familyUserService;
    private final ChallengeServiceImpl challengeService;
    private final ChallengeUserServiceImpl challengeUserService;
    private final KidServiceImpl kidService;
    private final ParentServiceImpl parentService;
    private final ExpoNotificationServiceImpl notificationService;

    @Transactional
    public FamilyDTO createFamily(User user) {
        familyUserService.checkIfFamilyExists(user);

        Family family = familyService.postNewFamily(user);
        familyUserService.postNewFamilyUser(family, user);

        return new FamilyDTO(family, List.of());
    }

    @Transactional(readOnly = true)
    public FamilyDTO readFamily(User user) {
        FamilyUser familyUser = familyUserService.findByUser(user);
        Family family = familyUser.getFamily();
        List<FamilyUser> familyUserList = familyUserService.getFamilyUserListExclude(family,
            user);

        return new FamilyDTO(family, familyUserList.stream()
            .map(FamilyUserDTO::new)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public List<KidListDTO> readFamilyKidList(User user) {
        FamilyUser familyUser = familyUserService.findByUser(user);
        List<KidListDTO> kidListDTOList = familyUserService.getKidListFromFamily(familyUser);

        return kidListDTOList;
    }

    @Transactional
    public FamilyDTO createFamilyUser(User user, FamilyRequest familyRequest) {

        Family family = familyService.getFamilyByCode(familyRequest.getCode());
        List<FamilyUser> familyUserList = familyUserService.checkFamilyUserList(family, user);

        familyUserService.leavePreviousFamily(user);
        familyUserService.postNewFamilyUser(family, user);

        notificationService.newFamilyUserNotification(user, familyUserList);

        return new FamilyDTO(family, familyUserList.stream()
            .map(FamilyUserDTO::new)
            .collect(Collectors.toList()));
    }

    @Transactional
    public FamilyDTO deleteFamilyUser(User user, FamilyRequest familyRequest) {
        FamilyUser familyUser = familyUserService.findByUserAndCheckCode(user,
            familyRequest.getCode());
        Family family = familyUser.getFamily();
        List<FamilyUser> familyUserList = familyUserService.getFamilyUserListExclude(family,
            user);

        if (user.getIsKid()) {
            List<Challenge> challengeList = challengeUserService.readAllChallengeUserListToChallengeList(
                user);
            challengeUserService.deleteAllChallengeUserOfUser(user);
            ChallengeCompleteDeleteByKidMapperDTO challengeCompleteDeleteByKidMapperDTO = challengeService.challengeCompleteDeleteByKid(
                challengeList);
            kidService.updateInitKid(user);
            parentService.updateParentForDeleteFamilyUserByKid(familyUserList,
                challengeCompleteDeleteByKidMapperDTO);
        } else {
            List<ChallengeUser> challengeUserList = challengeUserService.getChallengeUserListByContractUser(
                user);
            kidService.updateKidForDeleteFamilyUserByParent(challengeUserList);
            parentService.updateInitParent(user);
            challengeService.challengeCompleteDeleteByParent(challengeUserList);
        }

        familyUserService.deleteFamilyUser(familyUser);
        if (familyUserList.size() == 0) {
            familyService.deleteFamily(family);
        }

        return new FamilyDTO(family, familyUserList.stream()
            .map(FamilyUserDTO::new).collect(Collectors.toList()));
    }
}
