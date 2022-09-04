package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.dto.KidWeekDTO;
import com.ceos.bankids.dto.WeekDTO;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeService {

    public ChallengeDTO createChallenge(User user, ChallengeRequest challengeRequest);

    public ChallengeDTO deleteChallenge(User user, Long challengeId);

    public List<ChallengeDTO> readChallenge(User user, String status);

    public KidChallengeListDTO readKidChallenge(User user, Long kidId, String status);

    public ChallengeDTO updateChallengeStatus(User user, Long challengeId,
        KidChallengeRequest kidChallengeRequest) throws IOException;

    public WeekDTO readWeekInfo(User user);

    public KidWeekDTO readKidWeekInfo(User user, Long kidId);

    public List<ChallengeDTO> readAchievedChallenge(User user, String interestPayment);

    public ChallengeDTO updateChallengeInterestPayment(User user, Long challengeId);

}
