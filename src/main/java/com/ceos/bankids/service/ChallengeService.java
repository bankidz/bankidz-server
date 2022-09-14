package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AchievedChallengeDTO;
import com.ceos.bankids.dto.AchievedChallengeListDTO;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.ChallengeListMapperDTO;
import com.ceos.bankids.dto.ChallengePostDTO;
import com.ceos.bankids.dto.KidAchievedChallengeListDTO;
import com.ceos.bankids.dto.KidWeekDTO;
import com.ceos.bankids.dto.WeekDTO;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeService {

    public ChallengeDTO createChallenge(User user, ChallengePostDTO challengePostDTO);

    public ChallengeDTO deleteWalkingChallenge(User user, ChallengeUser challengeUser);

    public ChallengeDTO deleteRejectedChallenge(User user, ChallengeUser challengeUser);

    public ChallengeDTO deletePendingChallenge(User user, ChallengeUser challengeUser);

    public List<ChallengeDTO> readChallengeList(User user, List<Challenge> challengeList,
        String status);

//    public KidChallengeListDTO readKidChallenge(User user, Long kidId, String status);

    public ChallengeDTO updateChallengeStatus(User user, Long challengeId,
        KidChallengeRequest kidChallengeRequest) throws IOException;

    public WeekDTO readWeekInfo(User user);

    public KidWeekDTO readKidWeekInfo(User user, Long kidId);

    public AchievedChallengeListDTO readAchievedChallenge(User user, String interestPayment);

    public AchievedChallengeDTO updateChallengeInterestPayment(User user, Long challengeId);

    public KidAchievedChallengeListDTO readKidAchievedChallenge(User user, Long kidId,
        String interestPayment);

    public Challenge readChallenge(Long challengeId);

    public ChallengeListMapperDTO test(Challenge challenge);

}
