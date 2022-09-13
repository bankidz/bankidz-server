package com.ceos.bankids.service;

import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeUserService {

    public ChallengeUser postChallengeUser(User authUser, Challenge challenge);
}
