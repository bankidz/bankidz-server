package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.constant.NotificationCategory;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.Notification;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.NotificationDTO;
import com.ceos.bankids.dto.NotificationIsReadDTO;
import com.ceos.bankids.dto.NotificationListDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.exception.InternalServerException;
import com.ceos.bankids.repository.NotificationRepository;
import io.github.jav.exposerversdk.ExpoPushMessage;
import io.github.jav.exposerversdk.ExpoPushMessageTicketPair;
import io.github.jav.exposerversdk.ExpoPushTicket;
import io.github.jav.exposerversdk.PushClient;
import io.github.jav.exposerversdk.PushClientException;
import io.github.jav.exposerversdk.PushNotificationException;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoNotificationServiceImpl implements ExpoNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    @Override
    public NotificationListDTO readNotificationList(User user, Long lastId) {
        PageRequest pageRequest = PageRequest.of(0, 11);
        if (lastId == null) {
            Page<Notification> byUserIdOrderByIdDesc = notificationRepository.findByUserIdOrderByIdDesc(
                user.getId(), pageRequest);
            List<NotificationDTO> notificationDTOS = byUserIdOrderByIdDesc.stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
            if (notificationDTOS.size() == 11L) {
                NotificationDTO lastNotification = notificationDTOS.get(
                    notificationDTOS.size() - 1);
                Long lastNotificationId = lastNotification.getId();
                notificationDTOS.remove(10);
                return new NotificationListDTO(lastNotificationId, false, notificationDTOS);
            } else if (notificationDTOS.size() < 11L) {
                return new NotificationListDTO(null, true, notificationDTOS);
            }
        }
        List<NotificationDTO> notificationDTOList = notificationRepository.findByIdLessThanEqualAndUserIdOrderByIdDesc(
                lastId, user.getId(), pageRequest).stream()
            .map(NotificationDTO::new).collect(Collectors.toList());
        if (notificationDTOList.size() == 11L) {
            NotificationDTO lastNotification = notificationDTOList.get(
                notificationDTOList.size() - 1);
            Long last = lastNotification.getId();
            notificationDTOList.remove(10);
            return new NotificationListDTO(last, false, notificationDTOList);
        } else {
            return new NotificationListDTO(null, true, notificationDTOList);
        }
    }

    @Transactional
    @Override
    public NotificationDTO updateNotification(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_NOTIFICATION_ERROR.getErrorCode()));
        if (notification.getIsRead()) {
            throw new BadRequestException(ErrorCode.ALREADY_READ_NOTIFICATION_ERROR.getErrorCode());
        }
        if (!Objects.equals(notification.getUser().getId(), user.getId())) {
            throw new ForbiddenException(
                ErrorCode.NOT_MATCH_NOTIFICATION_USER_ERROR.getErrorCode());
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return new NotificationDTO(notification);
    }

    @Transactional(readOnly = true)
    @Override
    public NotificationIsReadDTO readNotificationIsAllRead(User user) {

        List<Notification> notificationList = notificationRepository.findAllByUserId(user.getId())
            .stream()
            .filter(notification -> !notification.getIsRead()).collect(
                Collectors.toList());
        if (notificationList.size() == 0) {
            return new NotificationIsReadDTO(true);
        } else {
            return new NotificationIsReadDTO(false);
        }
    }

    @Transactional
    public void deleteAllNotification(User user) {
        notificationRepository.deleteAllByUserId(user.getId());
    }

    public void sendMessage(User user, String title, String body, Map<String, Object> data,
        NotificationCategory notificationCategory, String linkUrl) {

        String token = user.getExpoToken();
        if (token == null) {
            log.info("token = {}", user.getExpoToken());
            throw new BadRequestException(ErrorCode.NOTIFICATION_ACCESSCODE_ERROR.getErrorCode());
        }
        if (!PushClient.isExponentPushToken(token)) {
            log.info("token = {}", user.getExpoToken());
            throw new BadRequestException(ErrorCode.NOTIFICATION_ACCESSCODE_ERROR.getErrorCode());
        }
        ExpoPushMessage expoPushMessage = new ExpoPushMessage();
        expoPushMessage.getTo().add(token);
        expoPushMessage.setTitle(title);
        expoPushMessage.setBody(body);
        expoPushMessage.setData(data);

        List<ExpoPushMessage> expoPushMessages = new ArrayList<>();
        expoPushMessages.add(expoPushMessage);

        try {
            PushClient pushClient = new PushClient();
            List<List<ExpoPushMessage>> chunks = pushClient.chunkPushNotifications(
                expoPushMessages);

            List<CompletableFuture<List<ExpoPushTicket>>> messageRepliesFutures = new ArrayList<>();

            for (List<ExpoPushMessage> chunk : chunks) {
                messageRepliesFutures.add(pushClient.sendPushNotificationsAsync(chunk));
            }
            //Todo 메서드 인자가 user로 바뀌면 데이터 베이스에 꽂기
            Notification notification = Notification.builder().title(title).message(body).user(user)
                .notificationCategory(notificationCategory).linkUrl(linkUrl).build();
            notificationRepository.save(notification);
            List<ExpoPushTicket> allTickets = new ArrayList<>();
            for (CompletableFuture<List<ExpoPushTicket>> messageReplyFuture : messageRepliesFutures) {
                try {
                    allTickets.addAll(messageReplyFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("error message = {}", e.getMessage());
                    throw new InternalServerException(
                        ErrorCode.NOTIFICATION_SERVICE_ERROR.getErrorCode());
                }
            }

            List<ExpoPushMessageTicketPair<ExpoPushMessage>> zippedMessagesTickets = pushClient.zipMessagesTickets(
                expoPushMessages, allTickets);

            List<ExpoPushMessageTicketPair<ExpoPushMessage>> okTicketMessages = pushClient.filterAllSuccessfulMessages(
                zippedMessagesTickets);
            String okTicketMessagesString = okTicketMessages.stream()
                .map(p -> "Title: " + p.message.getTitle() + ", Id:" + p.ticket.getId()).collect(
                    Collectors.joining(","));
            log.info("Recieved OK ticket for " + okTicketMessages.size() + " messages: "
                + okTicketMessagesString);

            List<ExpoPushMessageTicketPair<ExpoPushMessage>> errorTicketMessages = pushClient.filterAllMessagesWithError(
                zippedMessagesTickets);
            String errorTicketMessagesString = errorTicketMessages.stream().map(
                p -> "id: " + user.getId() + ", " + "Title: " + p.message.getTitle() + ", Error: "
                    + p.ticket.getDetails()
                    .getError()).collect(Collectors.joining(","));
            log.info("Recieved ERROR ticket for " + errorTicketMessages.size() + " messages: "
                + errorTicketMessagesString);
        } catch (PushClientException | PushNotificationException | GenericJDBCException e) {
            log.info("error message = {}", e.getMessage());
            throw new InternalServerException(ErrorCode.NOTIFICATION_SERVICE_ERROR.getErrorCode());
        }
    }

    @Async
    @ApiOperation(value = "자녀가 돈길 제안했을 때 부모 알림")
    public void createPendingChallengeNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "\uD83D\uDD14 " + challengeUser.getUser().getUsername() + "님이 돈길을 제안했어요";
        String notificationBody =
            challengeUser.getUser().getUsername() + "님이 돈길을 제안했어요! 수락하러 가볼까요?";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        NotificationCategory notificationCategory = NotificationCategory.CHALLENGE;
        Boolean checkServiceOptIn = checkServiceOptIn(contractUser, title, notificationBody,
            notificationCategory, "");
        if (checkServiceOptIn) {
            this.sendMessage(contractUser, title, notificationBody, newMap,
                notificationCategory, "");
        }
        log.info("부모 유저 id = {}에게 유저 id = {} 돈길 id = {} 의 돈길 제안", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    @ApiOperation(value = "돈길을 완주했을 때 부모 알림")
    public void achieveChallengeNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "\uD83D\uDEA8자녀가 돈길을 완주했어요";
        String notificationBody =
            "실제로 다 모았는지 확인하시고\n그동안 고생한" + challengeUser.getUser().getUsername()
                + "님에게 이자\uD83D\uDCB0로 보상해주세요!";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        NotificationCategory notificationCategory = NotificationCategory.CHALLENGE;
        Boolean checkServiceOptIn = checkServiceOptIn(contractUser, title, notificationBody,
            notificationCategory, "/");
        if (checkServiceOptIn) {
            this.sendMessage(contractUser, title, notificationBody, newMap,
                notificationCategory, "/");
        }
        log.info("부모 유저 id = {}에게 유저 id = {}의 돈길 id = {} 돈길 완주 알림 전송", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    private Boolean checkServiceOptIn(User user, String title, String body,
        NotificationCategory notificationCategory, String linkUrl) {
        if (!user.getServiceOptIn() || !user.getExpoToken().startsWith("ExponentPushToken")) {
            Notification notification = Notification.builder().user(user).title(title).message(body)
                .notificationCategory(notificationCategory).linkUrl(linkUrl)
                .build();
            notificationRepository.save(notification);
            return false;
        }
        return true;
    }
}
