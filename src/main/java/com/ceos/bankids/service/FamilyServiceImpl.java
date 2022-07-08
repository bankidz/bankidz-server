package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FamilyDTO;
import com.ceos.bankids.dto.FamilyUserDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.FamilyRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
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
    @Transactional
    public List<FamilyUserDTO> getFamilyUserList(Family family) {
        List<FamilyUser> familyUser = fuRepo.findByFamily(family);

        List<FamilyUserDTO> userDTOList = familyUser.stream().map(FamilyUser::getUser)
            .map(FamilyUserDTO::new)
            .collect(Collectors.toList());

        return userDTOList;
    }
}
