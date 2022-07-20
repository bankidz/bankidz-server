package com.ceos.bankids.service;

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
                throw new BadRequestException("삭제된 가족입니다.");
            }
            List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(
                familyUser.get().getFamily());
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

            List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(newFamily);
            FamilyDTO familyDTO = new FamilyDTO(newFamily, familyUserDTOList);

            return familyDTO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyUserDTO> getFamilyUserList(Family family) {
        List<FamilyUser> familyUser = fuRepo.findByFamily(family);

        List<FamilyUserDTO> userDTOList = familyUser.stream().map(FamilyUser::getUser)
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
                throw new BadRequestException("삭제된 가족입니다.");
            }
            List<FamilyUserDTO> familyUserDTOList = getFamilyUserList(family.get());
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
            throw new ForbiddenException("부모만 자녀 정보를 조회할 수 있습니다.");
        }
        Optional<FamilyUser> familyUser = fuRepo.findByUserId(user.getId());
        if (familyUser.isPresent()) {
            Optional<Family> family = fRepo.findById(familyUser.get().getFamily().getId());
            if (family.isEmpty()) {
                throw new BadRequestException("삭제된 가족입니다.");
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

    class KidListDTOComparator implements Comparator<KidListDTO> {

        @Override
        public int compare(KidListDTO k1, KidListDTO k2) {
            return k1.getUsername().compareTo(k2.getUsername());
        }
    }
}
