package com.ceos.bankids.repository;

import com.ceos.bankids.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findById(Long id);

    public Optional<User> findByAuthenticationCode(String authenticationCode);
}
