package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Challenge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    public Optional<Challenge> findById(Long id);

    public List<Challenge> findByContractUserId(Long contractUserId);

    public List<Challenge> findAllByDeleted_atIsNotNullOrderByIdDesc();

    public List<Challenge> findByChallengeStatusAndDeleted_atIsNotNullOrderByIdDesc(
        String challengeStatus);
}
