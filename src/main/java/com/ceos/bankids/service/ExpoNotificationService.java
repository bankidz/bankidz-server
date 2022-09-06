package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.NotificationDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ExpoNotificationService {

    public List<NotificationDTO> readNotificationList(User user);

    public NotificationDTO updateNotification(User user, Long notificationId);
}
