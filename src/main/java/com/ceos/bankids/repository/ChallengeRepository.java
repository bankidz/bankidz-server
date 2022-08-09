package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Challenge;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    public Optional<Challenge> findById(Long id);
}
