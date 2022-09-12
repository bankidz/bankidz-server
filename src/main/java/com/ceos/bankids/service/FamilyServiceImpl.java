package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.FamilyRepository;
import com.ceos.bankids.repository.FamilyUserRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyUserRepository familyUserRepository;

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
    @Transactional
    public List<KidListDTO> getKidListFromFamily(FamilyUser familyUser) {
        User user = familyUser.getUser();
        Family family = familyUser.getFamily();
        if (user.getIsKid()) {
            throw new ForbiddenException(ErrorCode.KID_FORBIDDEN.getErrorCode());
        }

        List<FamilyUser> familyUserList = familyUserRepository.findByFamily(family);
        List<KidListDTO> kidListDTOList = familyUserList.stream().map(FamilyUser::getUser)
            .filter(User::getIsKid).map(KidListDTO::new).collect(
                Collectors.toList());
        Collections.sort(kidListDTOList, new KidListDTOComparator());
        return kidListDTOList;
    }

    @Override
    @Transactional
    public Family getFamilyByCode(String code) {
        return familyRepository.findByCode(code).orElseThrow(
            () -> new BadRequestException(ErrorCode.FAMILY_TO_JOIN_NOT_EXISTS.getErrorCode()));
    }

    @Override
    @Transactional
    public void deleteFamily(Family family) {
        familyRepository.delete(family);
    }


    class KidListDTOComparator implements Comparator<KidListDTO> {

        @Override
        public int compare(KidListDTO k1, KidListDTO k2) {
            return k1.getUsername().compareTo(k2.getUsername());
        }
    }
}
