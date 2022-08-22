package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.dto.KidListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.FamilyRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository fRepo;
    private final FamilyUserRepository fuRepo;

    @Override
    @Transactional
    public FamilyDTO postNewFamily(User user) {
        Optional<FamilyUser> familyUser = fuRepo.findByUserId(user.getId());

        if (familyUser.isPresent()) {
            Optional<Family> family = fRepo.findById(familyUser.get().getFamily().getId());
            if (family.isEmpty()) {
                throw new BadRequestException(ErrorCode.FAMILY_NOT_EXISTS.getErrorCode());
            }
            List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(
                familyUser.get().getFamily(), user);
            FamilyDTO familyDTO = new FamilyDTO(familyUser.get().getFamily(), familyUserDTOList);

            return familyDTO;
        } else {
            String newFamilyCode = UUID.randomUUID().toString();
            Family newFamily = Family.builder()
                .code(newFamilyCode)
                .build();
            fRepo.save(newFamily);

            FamilyUser newFamilyUser = FamilyUser.builder()
                .user(user)
                .family(newFamily)
                .build();
            fuRepo.save(newFamilyUser);

            List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(newFamily, user);
            FamilyDTO familyDTO = new FamilyDTO(newFamily, familyUserDTOList);

            return familyDTO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyUserDTO> getFamilyUserList(Family family, User user) {
        List<FamilyUser> familyUser = fuRepo.findByFamily(family);

        List<FamilyUserDTO> userDTOList = familyUser.stream().map(FamilyUser::getUser)
            .filter(u -> !u.getId().equals(user.getId()))
            .map(FamilyUserDTO::new)
            .collect(Collectors.toList());

        return userDTOList;
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyDTO getFamily(User user) {
        Optional<FamilyUser> familyUser = fuRepo.findByUserId(user.getId());
        if (familyUser.isPresent()) {
            Optional<Family> family = fRepo.findById(familyUser.get().getFamily().getId());
            if (family.isEmpty()) {
                throw new BadRequestException(ErrorCode.FAMILY_NOT_EXISTS.getErrorCode());
            }
            List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(family.get(), user);
            FamilyDTO familyDTO = new FamilyDTO(family.get(), familyUserDTOList);
            return familyDTO;
        } else {
            FamilyDTO familyDTO = new FamilyDTO(new Family(), new ArrayList<>());
            return familyDTO;
        }
    }

    @Override
    @Transactional
    public List<KidListDTO> getKidListFromFamily(User user) {
        if (user.getIsKid()) {
            throw new ForbiddenException(ErrorCode.KID_FORBIDDEN.getErrorCode());
        }
        Optional<FamilyUser> familyUser = fuRepo.findByUserId(user.getId());
        if (familyUser.isPresent()) {
            Optional<Family> family = fRepo.findById(familyUser.get().getFamily().getId());
            if (family.isEmpty()) {
                throw new BadRequestException(ErrorCode.FAMILY_NOT_EXISTS.getErrorCode());
            }
            List<FamilyUser> familyUserList = fuRepo.findByFamily(family.get());
            List<KidListDTO> kidListDTOList = familyUserList.stream().map(FamilyUser::getUser)
                .filter(User::getIsKid).map(KidListDTO::new).collect(
                    Collectors.toList());
            Collections.sort(kidListDTOList, new KidListDTOComparator());
            return kidListDTOList;
        } else {
            List<KidListDTO> kidListDTOList = new ArrayList<>();
            return kidListDTOList;
        }
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

        List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(newFamily.get(), user);
        FamilyDTO familyDTO = new FamilyDTO(newFamily.get(), familyUserDTOList);

        return familyDTO;
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

        List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(family, user);
        FamilyDTO familyDTO = new FamilyDTO(family, familyUserDTOList);

        if (familyUserDTOList.size() == 0) {
            fRepo.delete(family);
        }

        return familyDTO;
    }


    class KidListDTOComparator implements Comparator<KidListDTO> {

        @Override
        public int compare(KidListDTO k1, KidListDTO k2) {
            return k1.getUsername().compareTo(k2.getUsername());
        }
    }
}
