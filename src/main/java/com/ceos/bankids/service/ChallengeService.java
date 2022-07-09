package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ChallengeDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeService {

    public ChallengeDTO createChallenge(User user, ChallengeRequest challengeRequest);

    public ChallengeDTO detailChallenge(User user, Long challengeId);

    public ChallengeDTO deleteChallenge(User user, Long challengeId);

    public List<ChallengeDTO> readChallenge(User user, String status);

}
