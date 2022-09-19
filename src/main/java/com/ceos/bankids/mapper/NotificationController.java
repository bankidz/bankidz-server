package com.ceos.bankids.mapper;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.constant.NotificationCategory;
import com.ceos.bankids.domain.FamilyUser;
import com.ceos.bankids.domain.Notification;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AllSendNotificationDTO;
import com.ceos.bankids.dto.NotificationDTO;
import com.ceos.bankids.dto.NotificationIsReadDTO;
import com.ceos.bankids.dto.NotificationListDTO;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.repository.NotificationRepository;
import com.ceos.bankids.repository.UserRepository;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final ExpoNotificationServiceImpl expoNotificationService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @ApiOperation(value = "모든 유저에게 알림")
    @PostMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<String> allSendNotification(
        @RequestBody AllSendNotificationDTO allSendNotificationRequest,
        @AuthenticationPrincipal User authUser) {

        if (authUser.getId() != 1L) {
            throw new ForbiddenException(ErrorCode.NOTICE_AUTH_ERROR.getErrorCode());
        }

        String title = allSendNotificationRequest.getTitle();
        String message = allSendNotificationRequest.getMessage();
        NotificationCategory notificationCategory = NotificationCategory.NOTICE;
        userRepository.findAll().stream()
            .filter(user -> user.getExpoToken() != null)
            .forEach(user -> {
                if (user.getNoticeOptIn() && user.getExpoToken()
                    .startsWith("ExponentPushToken")) {
                    expoNotificationService.sendMessage(user, title, message,
                        allSendNotificationRequest.getNewMap(), notificationCategory,
                        "/manage/notices/" + allSendNotificationRequest.getNewMap()
                            .get("noticeId"));
                } else {
                    Notification notification = Notification.builder().user(user).title(title)
                        .message(message).notificationCategory(notificationCategory)
                        .linkUrl("/manage/notices/" + allSendNotificationRequest.getNewMap()
                            .get("noticeId"))
                        .build();
                    notificationRepository.save(notification);
                }
            });
        return CommonResponse.onSuccess("NOTIFICATION SUCCESS");
    }

    @ApiOperation(value = "유저 알림 리스트 가져오기")
    @GetMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<NotificationListDTO> getNotificationList(
        @AuthenticationPrincipal User authUser, @RequestParam(required = false) Long lastId) {

        log.info("api = 유저 알림 리스트 가져오기 user = {}", authUser.getUsername());
        NotificationListDTO notificationListDTOS = expoNotificationService.readNotificationList(
            authUser, lastId);
        return CommonResponse.onSuccess(notificationListDTOS);
    }

    @ApiOperation(value = "유저 안읽은 알림 있는지 확인")
    @GetMapping(value = "/isRead", produces = "application/json; charset=utf-8")
    public CommonResponse<NotificationIsReadDTO> getNotificationIsAllRead(
        @AuthenticationPrincipal User authUser) {

        log.info("api = 안읽은 알림 있는지 확인 user = {}", authUser.getId());
        NotificationIsReadDTO notificationIsReadDTO = expoNotificationService.readNotificationIsAllRead(
            authUser);
        return CommonResponse.onSuccess(notificationIsReadDTO);
    }


    @ApiOperation(value = "유저 알림 읽음 확인")
    @PatchMapping(value = "/{notificationId}", produces = "application/json; charset=utf-8")
    public CommonResponse<NotificationDTO> patchNotification(@AuthenticationPrincipal User authUser,
        @PathVariable Long notificationId) {

        log.info("api = 유저 알림 읽음 처리 user = {} notification = {}", authUser.getUsername(),
            notificationId);
        NotificationDTO notificationDTO = expoNotificationService.updateNotification(authUser,
            notificationId);
        return CommonResponse.onSuccess(notificationDTO);
    }

    @Async
    public void newFamilyUserNotification(User newFamilyUser, List<FamilyUser> familyUserList) {

        String title = "가족그룹\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66에 새로 참여했어요";
        String notificationBody = "누가 가족그룹에 참여했는지 확인해요!\uD83D\uDCAB";
        HashMap<String, Object> newMap = new HashMap<>();
        NotificationCategory notificationCategory = NotificationCategory.FAMILY;
//        newMap.put("user", newFamilyUser.getId());
        familyUserList.forEach(familyUser -> {
            User user = familyUser.getUser();
            Boolean checkServiceOptIn = checkServiceOptIn(user, title, notificationBody,
                notificationCategory, "");
            if (checkServiceOptIn) {
                expoNotificationService.sendMessage(user, title, notificationBody, newMap,
                    notificationCategory, "");
            }
            log.info("기존 가족 구성원 id = {}에게 유저 id = {}의 가족 참여 알림 전송", familyUser.getUser().getId(),
                newFamilyUser.getId());
        });
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