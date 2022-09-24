package com.ceos.bankids.service;

import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeUserService {

    public ChallengeUser createdChallengeUser(User authUser, Challenge challenge);

    public ChallengeUser readChallengeUser(Long challengeId);

    public List<Challenge> readChallengeUserList(User authUser, String status);

    public List<Challenge> readAchievedChallengeUserList(User authUser);

    public List<Challenge> readAllChallengeUserListToChallengeList(User authUser);

    public void deleteAllChallengeUserOfUser(User authUser);
}
