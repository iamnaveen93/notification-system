package com.example.notificationsystem.service;

import com.example.notificationsystem.notification.channel.ChannelDispatcher;
import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.dto.NotificationAccepted;
import com.example.notificationsystem.dto.NotificationState;
import com.example.notificationsystem.dto.NotificationStatus;
import com.example.notificationsystem.notification.channel.NotificationChannel;
import com.example.notificationsystem.persistence.entities.IdempotencyKeyEntity;
import com.example.notificationsystem.persistence.entities.NotificationEntity;
import com.example.notificationsystem.persistence.interfaces.IdempotencyKeyRepository;
import com.example.notificationsystem.persistence.interfaces.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.example.notificationsystem.constants.ResourcesConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String STATUS_URL_PREFIX = ResourcesConstants.NOTIFICATION_URL + "/";

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    private final NotificationRepository notificationRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;
    private final ChannelDispatcher channelDispatcher;

    @Transactional
    public CreateResult create(String idempotencyKey, CreateNotificationRequest request) {

        String requestHash = requestHash(request);
        IdempotencyKeyEntity existing = idempotencyKeyRepository.findById(idempotencyKey).orElse(null);

        if (existing != null && existing.getExpiresAt().isAfter(Instant.now())) {
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency key was already used with a different request");
            }
            return new CreateResult(toAccepted(existing.getNotification()), true);
        }

        if (existing != null) {
            idempotencyKeyRepository.delete(existing);
            idempotencyKeyRepository.flush();
        }
        log.info("Dispatching notification [channels={}]", request.getDelivery().getChannels());
        request.getDelivery().getChannels().forEach(
                channel -> channelDispatcher.sendNotification(NotificationChannel.valueOf(channel), request));

        NotificationEntity notification = new NotificationEntity(
                "ntf_" + UUID.randomUUID(),
                request.getTemplateId(),
                request.getScheduledAt() == null ? null : request.getScheduledAt().toInstant(),
                request.getCorrelationId());
        notificationRepository.save(notification);

        idempotencyKeyRepository.save(new IdempotencyKeyEntity(
                idempotencyKey, requestHash, notification, Instant.now().plusSeconds(24 * 60 * 60)));

        return new CreateResult(toAccepted(notification), false);
    }

    @Transactional(readOnly = true)
    public NotificationStatus get(String notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        return new NotificationStatus()
                .notificationId(notification.getNotificationId())
                .templateId(notification.getTemplateId())
                .correlationId(notification.getCorrelationId())
                .status(NotificationState.valueOf(notification.getStatus().name()))
                .acceptedAt(notification.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(notification.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }

    private NotificationAccepted toAccepted(NotificationEntity notification) {
        return new NotificationAccepted()
                .notificationId(notification.getNotificationId())
                .status(NotificationAccepted.StatusEnum.ACCEPTED)
                .statusUrl(contextPath + STATUS_URL_PREFIX + notification.getNotificationId());
    }

    private String requestHash(CreateNotificationRequest request) {
        try {
            ObjectMapper canonicalMapper = objectMapper.copy()
                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            JsonNode payload = canonicalMapper.valueToTree(request);
            ((com.fasterxml.jackson.databind.node.ObjectNode) payload).remove("correlationId");
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalMapper.writeValueAsBytes(payload));
            return HexFormat.of().formatHex(digest);
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not create request hash", exception);
        }
    }

    public record CreateResult(NotificationAccepted response, boolean replayed) {
    }
}
