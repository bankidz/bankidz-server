package com.ceos.bankids.service;

import com.ceos.bankids.controller.request.KidChallengeRequest;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AchievedChallengeDTO;
import com.ceos.bankids.dto.AchievedChallengeListDTO;
import com.ceos.bankids.dto.ChallengeCompleteDeleteByKidMapperDTO;
import com.ceos.bankids.dto.ChallengeDTO;
import com.ceos.bankids.dto.ChallengeListMapperDTO;
import com.ceos.bankids.dto.ChallengePostDTO;
import com.ceos.bankids.dto.KidAchievedChallengeListDTO;
import com.ceos.bankids.dto.ProgressDTO;
import com.ceos.bankids.dto.WeekDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ChallengeService {

    public Challenge createChallenge(User user, ChallengePostDTO challengePostDTO);

    public ChallengeDTO deleteWalkingChallenge(User user, ChallengeUser challengeUser);

    public ChallengeDTO deleteRejectedChallenge(User user, ChallengeUser challengeUser);

    public ChallengeDTO deletePendingChallenge(User user, ChallengeUser challengeUser);

    public ChallengeDTO updateChallengeStatusToWalking(Challenge challenge);

    public ChallengeDTO updateChallengeStatusToRejected(Challenge challenge,
        KidChallengeRequest kidChallengeRequest, User contractUser);

    public WeekDTO readWeekInfo(List<Challenge> challengeList);

    public AchievedChallengeListDTO readAchievedChallenge(List<Challenge> achievedChallengeList,
        String interestPayment);

    public AchievedChallengeDTO updateChallengeInterestPayment(User user, Long challengeId);

    public KidAchievedChallengeListDTO readKidAchievedChallenge(User user,
        List<Challenge> achievedChallengeList,
        String interestPayment, Long kidId);

    public Challenge readChallenge(Long challengeId);

    public ChallengeListMapperDTO readWalkingChallenge(Challenge challenge);

    public ChallengeListMapperDTO readPendingChallenge(Challenge challenge);

    public ProgressDTO updateProgress(Challenge challenge);

    public ChallengeCompleteDeleteByKidMapperDTO challengeCompleteDeleteByKid(
        List<Challenge> challengeList);

    public List<ChallengeDTO> readChallengeHistory(String status);

    public ChallengeDTO readChallengeDetail(Long challengeId);
}
