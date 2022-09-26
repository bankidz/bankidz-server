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
    @Transactional(readOnly = true)
    public void checkIfFamilyExists(User user) {
        if (familyUserRepository.findByUser(user).isPresent()) {
            throw new BadRequestException(ErrorCode.FAMILY_ALREADY_EXISTS.getErrorCode());
        }
    }

    @Override
    @Transactional
    public void createFamilyUser(Family family, User user) {
        FamilyUser familyUser = FamilyUser.builder()
            .user(user)
            .family(family)
            .build();
        familyUserRepository.save(familyUser);
    }

    @Override
    @Transactional
    public void leavePreviousFamilyIfExists(User user) {
        Optional<FamilyUser> familyUser = familyUserRepository.findByUser(user);
        if (familyUser.isPresent()) {
            familyUserRepository.delete(familyUser.get());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FamilyUser> findByUserNullable(User user) {
        return familyUserRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyUser findByUser(User user) {
        return familyUserRepository.findByUser(user)
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyUser findByUserAndCheckCode(User user, String code) {
        FamilyUser familyUser = familyUserRepository.findByUser(user).orElseThrow(
            () -> new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode()));

        if (!familyUser.getFamily().getCode().equals(code)) {
            throw new BadRequestException(ErrorCode.USER_NOT_IN_THIS_FAMILY.getErrorCode());
        }
        return familyUser;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyUser> getFamilyUserList(Family family, User user) {
        return familyUserRepository.findByFamily(family);
    }

    @Override
    @Transactional
    public void deleteFamilyUser(FamilyUser familyUser) {
        familyUserRepository.delete(familyUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyUser> getFamilyUserListExclude(Family family, User user) {
        return familyUserRepository.findByFamilyAndUserNot(family, user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getContractUser(User user, Boolean isMom) {
        FamilyUser familyUser = familyUserRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
//        Family family = fRepo.findByCode(familyUser.getFamily().getCode())
//            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
        return familyUserRepository.findByFamilyAndUserNot(familyUser.getFamily(), user).stream()
            .map(FamilyUser::getUser)
            .filter(user1 -> user1.getIsFemale() == isMom && !user1.getIsKid())
            .collect(Collectors.toList()).stream().findFirst().orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CONSTRUCT_USER.getErrorCode()));

    }

    @Override
    public void checkSameFamily(User firstUser, User secondUser) {
        FamilyUser firstFamilyUser = familyUserRepository.findByUserId(firstUser.getId())
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode()));
        FamilyUser secondFamilyUser = familyUserRepository.findByUserId(secondUser.getId())
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode()));
        if (firstFamilyUser.getFamily().getCode() != secondFamilyUser.getFamily().getCode()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_FAMILY.getErrorCode());
        }

    }


}