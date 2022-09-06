package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends
    JpaRepository<Notification, Long> {

    public List<Notification> findAllByUserId(Long userId);
}
