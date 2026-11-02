package com.example.notificationsystem.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "notifications")
public class NotificationEntity extends BaseEntity {

    @Id
    @Column(name = "notification_id", length = 64, nullable = false, updatable = false)
    private String notificationId;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationStatus status;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    protected NotificationEntity() {
    }

    @Builder
    public NotificationEntity(String notificationId, String templateId, Instant scheduledAt,
                              String correlationId) {
        this.notificationId = notificationId;
        this.templateId = templateId;
        this.status = NotificationStatus.ACCEPTED;
        this.scheduledAt = scheduledAt;
        this.correlationId = correlationId;
    }

}
