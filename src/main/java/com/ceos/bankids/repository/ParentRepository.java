package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Parent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    public Optional<Parent> findByUserId(Long userId);
}