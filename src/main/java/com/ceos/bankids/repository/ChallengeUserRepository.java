package com.ceos.bankids.repository;

import com.ceos.bankids.domain.ChallengeUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeUserRepository extends JpaRepository<ChallengeUser, Long> {

    public Optional<ChallengeUser> findByChallengeId(Long challengeId);
}
