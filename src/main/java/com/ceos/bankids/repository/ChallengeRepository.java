package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    public Optional<Challenge> findById(Long id);

    public Optional<Challenge> findByStatus(Long status);
}
