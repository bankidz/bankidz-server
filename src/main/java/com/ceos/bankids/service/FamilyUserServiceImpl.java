package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.FamilyUserRepository;
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
}
