package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Family;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    public Optional<Family> findById(Long id);

    public Optional<Family> findByCode(String code);
}
