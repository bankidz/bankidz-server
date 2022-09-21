package com.ceos.bankids.mapper;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.NotificationDTO;
import com.ceos.bankids.dto.NotificationIsReadDTO;
import com.ceos.bankids.dto.NotificationListDTO;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationMapper {

    private final ExpoNotificationServiceImpl notificationService;

    public NotificationListDTO readNotificationListMapper(User user, Long lastId) {
        return notificationService.readNotificationList(user, lastId);
    }

    public NotificationIsReadDTO readNotificationIsAllReadMapper(User user) {
        return notificationService.readNotificationIsAllRead(user);
    }

    public NotificationDTO updateNotificationMapper(User user, Long notificationId) {
        return notificationService.updateNotification(user, notificationId);
    }
}
