package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyUserRepository extends JpaRepository<FamilyUser, Long> {

    public Optional<FamilyUser> findByUserId(Long id);

    public List<FamilyUser> findByFamily(Family family);

}
