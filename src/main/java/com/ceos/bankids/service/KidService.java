package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface KidService {

    public void createNewKid(User user);

    public void deleteKid(User user);

    public void checkKidDeleteChallenge(User user);

}
