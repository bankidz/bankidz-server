package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Family;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyUserRepository extends JpaRepository<FamilyUser, Long> {

    public Optional<FamilyUser> findByUserId(Long id);

    public Optional<FamilyUser> findByUser(User user);
    
    public List<FamilyUser> findByFamily(Family family);

    public List<FamilyUser> findByFamilyAndUserNot(Family family, User user);
}
