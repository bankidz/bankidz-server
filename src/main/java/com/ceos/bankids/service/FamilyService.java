package com.ceos.bankids.service;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface FamilyService {

    public Family createFamily(User user);

    public Family findByCode(String code);

    public void deleteFamily(Family family);
}
