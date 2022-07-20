package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Progress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    public List<Progress> findByChallengeId(Long challengeId);

    public Optional<Progress> findByChallengeIdAndWeeks(Long challengeId, Long weeks);

}
