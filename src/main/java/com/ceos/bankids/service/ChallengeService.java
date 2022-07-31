package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.KidChallengeListDTO;
import com.ceos.bankids.dto.WeekDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeService {

    public ChallengeDTO createChallenge(User user, ChallengeRequest challengeRequest);

    public ChallengeDTO detailChallenge(User user, Long challengeId);

    public ChallengeDTO deleteChallenge(User user, Long challengeId);

    public List<ChallengeDTO> readChallenge(User user, String status);

    public List<KidChallengeListDTO> readKidChallenge(User user);

    public ChallengeDTO updateChallengeStatus(User user, Long challengeId,
        KidChallengeRequest kidChallengeRequest);

    public WeekDTO readWeekInfo(User user);

    public WeekDTO readKidWeekInfo(User user, String kidName);

}
