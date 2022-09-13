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

}
