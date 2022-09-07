package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.NotificationDTO;
import com.ceos.bankids.dto.NotificationListDTO;
import org.springframework.stereotype.Service;

@Service
public interface ExpoNotificationService {

    public NotificationListDTO readNotificationList(User user, Long lastId);

    public NotificationDTO updateNotification(User user, Long notificationId);
}
