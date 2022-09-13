package com.ceos.bankids.service;

import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeUserService {

    public ChallengeUser postChallengeUser(User authUser, Challenge challenge);

    public ChallengeUser getChallengeUser(User authUser, Long challengeId);

    public void deleteChallengeUser(User authUser, Long challengeId);
}
