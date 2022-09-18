package com.ceos.bankids.service;

import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeUserService {

    public ChallengeUser postChallengeUser(User authUser, Challenge challenge);

    public ChallengeUser getChallengeUser(Long challengeId);

    public void deleteChallengeUser(User authUser, Long challengeId);

    public List<Challenge> getChallengeUserList(User authUser, String status);

    public List<Challenge> getAchievedChallengeUserList(User authUser);

    public List<Challenge> getAllChallengeUserList(User authUser);

    public void deleteAllChallengeUser(User authUser);
}
