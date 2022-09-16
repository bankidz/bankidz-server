package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.FamilyRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;

    @Override
    @Transactional
    public Family postNewFamily(User user) {
        Family family = Family.builder()
            .code(UUID.randomUUID().toString())
            .build();
        familyRepository.save(family);
        return family;
    }

    @Override
    @Transactional(readOnly = true)
    public Family getFamilyByCode(String code) {
        return familyRepository.findByCode(code).orElseThrow(
            () -> new BadRequestException(ErrorCode.FAMILY_TO_JOIN_NOT_EXISTS.getErrorCode()));
    }

    @Override
    @Transactional
    public void deleteFamily(Family family) {
        familyRepository.delete(family);
    }

    @Override
    @Transactional
    public FamilyDTO postNewFamilyUser(User user, String code) {
        Optional<Family> newFamily = fRepo.findByCode(code);
        if (newFamily.isEmpty()) {
            throw new BadRequestException(ErrorCode.FAMILY_TO_JOIN_NOT_EXISTS.getErrorCode());
        }

        List<FamilyUser> familyUserList = fuRepo.findByFamily(newFamily.get());
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

        Optional<FamilyUser> familyUser = fuRepo.findByUserId(user.getId());
        if (familyUser.isPresent()) {
            Optional<Family> family = fRepo.findById(familyUser.get().getFamily().getId());
            if (family.get().getCode() == code) {
                throw new ForbiddenException(ErrorCode.USER_ALREADY_IN_THIS_FAMILY.getErrorCode());
            }
            fuRepo.delete(familyUser.get());
        }

        FamilyUser newFamilyUser = FamilyUser.builder()
            .user(user)
            .family(newFamily.get())
            .build();
        fuRepo.save(newFamilyUser);

        notificationController.newFamilyUserNotification(user, familyUserList);

        return FamilyDTO.builder()
            .family(newFamily.get())
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();
    }

    @Override
    @Transactional
    public FamilyDTO deleteFamilyUser(User user, String code) {
        Optional<FamilyUser> familyUser = fuRepo.findByUserId(user.getId());
        if (familyUser.isEmpty()) {
            throw new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode());
        }

        Family family = familyUser.get().getFamily();
        if (!code.equals(family.getCode())) {
            throw new BadRequestException(ErrorCode.USER_NOT_IN_THIS_FAMILY.getErrorCode());
        }
        fuRepo.delete(familyUser.get());

        List<FamilyUser> familyUserList = fuRepo.findByFamilyAndUserNot(family, user);
        if (familyUserList.size() == 0) {
            fRepo.delete(family);
        }

        return FamilyDTO.builder()
            .family(family)
            .familyUserList(
                familyUserList
                    .stream()
                    .map(FamilyUserDTO::new)
                    .collect(Collectors.toList())
            ).build();
    }

    @Override
    @Transactional(readOnly = true)
    public User getContractUser(User user, Boolean isMom) {
        FamilyUser familyUser = fuRepo.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
        Family family = fRepo.findByCode(familyUser.getFamily().getCode())
            .orElseThrow(() -> new BadRequestException(ErrorCode.NOT_EXIST_FAMILY.getErrorCode()));
        return fuRepo.findByFamilyAndUserNot(family, user).stream()
            .map(FamilyUser::getUser)
            .filter(user1 -> user1.getIsFemale() == isMom && !user1.getIsKid())
            .collect(Collectors.toList()).stream().findFirst().orElseThrow(
                () -> new BadRequestException(ErrorCode.NOT_EXIST_CONSTRUCT_USER.getErrorCode()));

    }

    @Override
    public void checkSameFamily(User firstUser, User secondUser) {
        FamilyUser firstFamilyUser = fuRepo.findByUserId(firstUser.getId())
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode()));
        FamilyUser secondFamilyUser = fuRepo.findByUserId(secondUser.getId())
            .orElseThrow(
                () -> new BadRequestException(ErrorCode.USER_NOT_IN_ANY_FAMILY.getErrorCode()));
        if (firstFamilyUser.getFamily().getCode() != secondFamilyUser.getFamily().getCode()) {
            throw new ForbiddenException(ErrorCode.NOT_MATCH_FAMILY.getErrorCode());
        }

    }


    class KidListDTOComparator implements Comparator<KidListDTO> {

        @Override
        public int compare(KidListDTO k1, KidListDTO k2) {
            return k1.getUsername().compareTo(k2.getUsername());
        }
    }
}
