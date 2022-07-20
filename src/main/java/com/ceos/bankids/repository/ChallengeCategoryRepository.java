package com.ceos.bankids.repository;

import com.ceos.bankids.domain.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeCategoryRepository extends JpaRepository<ChallengeCategory, Long> {

    public ChallengeCategory findByCategory(String category);
}
