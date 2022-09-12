package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface FamilyUserService {

    public void checkIfFamilyExists(User user);

    public void postNewFamilyUser(Family family, User user);
}
