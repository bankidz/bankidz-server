package com.ceos.bankids.repository;

import com.ceos.bankids.domain.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeCategoryRepository extends JpaRepository<ChallengeCategory, Long> {

    public ChallengeCategory findByCategory(String category);
}
