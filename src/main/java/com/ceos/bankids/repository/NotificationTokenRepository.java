package com.ceos.bankids.repository;

import com.ceos.bankids.domain.NotificationToken;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {

    public List<NotificationToken> findByUserId(Long userId);
}
