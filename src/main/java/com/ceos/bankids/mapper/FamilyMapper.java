package com.ceos.bankids.mapper;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.request.FamilyRequest;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.dto.KidListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.FamilyServiceImpl;
import com.ceos.bankids.service.FamilyUserServiceImpl;
import java.util.Collections;
import java.util.Comparator;
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
    private final ExpoNotificationServiceImpl notificationService;

    @Transactional
    public FamilyDTO createFamily(User user) {
        familyUserService.checkIfFamilyExists(user);

        Family family = familyService.createFamily(user);
        familyUserService.createFamilyUser(family, user);

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
        if (user.getIsKid()) {
            throw new ForbiddenException(ErrorCode.KID_FORBIDDEN.getErrorCode());
        }

        FamilyUser familyUser = familyUserService.findByUser(user);
        List<FamilyUser> familyUserList = familyUserService.getFamilyUserList(
            familyUser.getFamily(), user);

        List<KidListDTO> kidListDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .filter(User::getIsKid).map(KidListDTO::new).collect(
                Collectors.toList());
        Collections.sort(kidListDTOList, new KidListDTOComparator());

        return kidListDTOList;
    }

    @Transactional
    public FamilyDTO createFamilyUser(User user, FamilyRequest familyRequest) {

        Family family = familyService.findByCode(familyRequest.getCode());
        List<FamilyUser> familyUserList = familyUserService.getFamilyUserList(family, user);

        if (familyUserList.stream().filter(fu -> fu.getUser().equals(user))
            .collect(Collectors.toList()).size() > 0) {
            throw new ForbiddenException(ErrorCode.USER_ALREADY_IN_THIS_FAMILY.getErrorCode());
        }

        if (!user.getIsKid()) {
            if (user.getIsFemale() == null) {
                throw new BadRequestException(ErrorCode.INVALID_USER_TYPE.getErrorCode());
            } else if (user.getIsFemale()) {
                List<FamilyUser> checkMomList = familyUserList.stream()
                    .filter(fu -> !fu.getUser().getIsKid() && fu.getUser().getIsFemale())
                    .collect(Collectors.toList());
                if (!checkMomList.isEmpty()) {
                    throw new ForbiddenException(ErrorCode.MOM_ALREADY_EXISTS.getErrorCode());
                }
            } else {
                List<FamilyUser> checkDadList = familyUserList.stream()
                    .filter(fu -> !fu.getUser().getIsKid() && !fu.getUser().getIsFemale())
                    .collect(Collectors.toList());
                if (!checkDadList.isEmpty()) {
                    throw new ForbiddenException(ErrorCode.DAD_ALREADY_EXISTS.getErrorCode());
                }
            }
        }

        familyUserService.leavePreviousFamilyIfExists(user);
        familyUserService.createFamilyUser(family, user);

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

        familyUserService.deleteFamilyUser(familyUser);
        if (familyUserList.size() == 0) {
            familyService.deleteFamily(family);
        }

        return null;
    }

    class KidListDTOComparator implements Comparator<KidListDTO> {

        @Override
        public int compare(KidListDTO k1, KidListDTO k2) {
            return k1.getUsername().compareTo(k2.getUsername());
        }
    }
}
