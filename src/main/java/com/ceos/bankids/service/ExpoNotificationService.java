package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AllSendNotificationDTO;
import com.ceos.bankids.dto.NotificationDTO;
import com.ceos.bankids.dto.NotificationIsReadDTO;
import com.ceos.bankids.dto.NotificationListDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface ExpoNotificationService {

    public NotificationListDTO readNotificationList(User user, Long lastId);

    public NotificationDTO updateNotification(User user, Long notificationId);

    public NotificationIsReadDTO readNotificationIsAllRead(User user);

    public void createAllNotification(List<User> userList, String title, String message,
        AllSendNotificationDTO allSendNotificationDTO);
}
