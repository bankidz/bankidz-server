package com.ceos.bankids.repository;

import com.ceos.bankids.domain.ChallengeUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeUserRepository extends JpaRepository<ChallengeUser, Long> {

    public ChallengeUser findByChallengeId(Long challengeId);
}
