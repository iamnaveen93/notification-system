package com.example.notificationsystem.controller;

import com.example.notificationsystem.constants.ResourcesConstants;
import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.dto.NotificationAccepted;
import com.example.notificationsystem.dto.NotificationStatus;
import com.example.notificationsystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping(path = ResourcesConstants.NOTIFICATION_URL,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationAccepted> createNotification(
            @RequestHeader("x-header-idempotency-key") String idempotencyKey,
            @RequestBody CreateNotificationRequest notificationRequest) {
        NotificationService.CreateResult result = notificationService.create(idempotencyKey, notificationRequest);
        ResponseEntity.BodyBuilder response = ResponseEntity.accepted();
        if (result.replayed()) {
            response.header("x-header-idempotent-replayed", "true");
        }
        return response.body(result.response());
    }

    @GetMapping(path = ResourcesConstants.NOTIFICATION_DETAILS_URL,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationStatus> getNotification(
            @PathVariable("notificationId") String notificationId) {
        return ResponseEntity.ok(notificationService.get(notificationId));
    }
}