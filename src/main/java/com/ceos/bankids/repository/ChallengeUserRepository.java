package com.ceos.bankids.repository;

import com.ceos.bankids.domain.ChallengeUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeUserRepository extends JpaRepository<ChallengeUser, Long> {

    public Optional<ChallengeUser> findByChallengeId(Long challengeId);

    public List<ChallengeUser> findByUserId(Long userId);

    public List<ChallengeUser> findByChallenge_ContractUserId(Long contractUserId);
}
