package com.ceos.bankids.repository;

import com.ceos.bankids.domain.LinkChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkChallengeRepository extends JpaRepository<LinkChallenge, Long> {
}
