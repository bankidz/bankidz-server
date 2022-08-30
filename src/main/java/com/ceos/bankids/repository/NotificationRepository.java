package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends
    JpaRepository<Notification, Long> {

}
