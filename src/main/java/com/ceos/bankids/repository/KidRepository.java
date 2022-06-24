package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Kid;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KidRepository extends JpaRepository<Kid, Long> {

    public Optional<Kid> findById(Long id);

    public Optional<Kid> findByAuthenticationCode(String authenticationCode);

}
