package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.FamilyUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyUserServiceImpl implements FamilyUserService {

    private final FamilyUserRepository familyUserRepository;

    @Override
    @Transactional
    public void checkIfFamilyExists(User user) {
        if (familyUserRepository.findByUser(user).isPresent()) {
            throw new BadRequestException(ErrorCode.FAMILY_ALREADY_EXISTS.getErrorCode());
        }
    }

    @Override
    @Transactional
    public void postNewFamilyUser(Family family, User user) {
        FamilyUser familyUser = FamilyUser.builder()
            .user(user)
            .family(family)
            .build();
        familyUserRepository.save(familyUser);
    }

    @Override
    @Transactional
    public void leavePreviousFamily(User user) {
        Optional<FamilyUser> familyUser = familyUserRepository.findByUser(user);
        if (familyUser.isPresent()) {
            familyUserRepository.delete(familyUser.get());
        }
    }

    @Override
    @Transactional
    public Optional<FamilyUser> findByUser(User user) {
        return familyUserRepository.findByUser(user);
    }

    @Override
    @Transactional
    public FamilyUser findByUserAndCheckCode(User user, String code) {
        FamilyUser familyUser = familyUserRepository.findByUser(user).orElseThrow(
            () -> new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode()));

        if (!familyUser.getFamily().getCode().equals(code)) {
            throw new BadRequestException(ErrorCode.USER_NOT_IN_THIS_FAMILY.getErrorCode());
        }
        return familyUser;
    }

    @Override
    @Transactional
    public List<FamilyUser> checkFamilyUserList(Family family, User user) {
        List<FamilyUser> familyUserList = familyUserRepository.findByFamily(family);

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
        return familyUserList;
    }

    @Override
    @Transactional
    public void deleteFamilyUser(FamilyUser familyUser) {
        familyUserRepository.delete(familyUser);
    }

    @Override
    @Transactional
    public List<FamilyUser> getFamilyUserListExclude(Family family, User user) {
        return familyUserRepository.findByFamilyAndUserNot(family, user);
    }
}