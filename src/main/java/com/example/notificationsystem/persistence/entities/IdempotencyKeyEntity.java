package com.example.notificationsystem.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity extends BaseEntity {

    @Id
    @Column(name = "idempotency_key", length = 128, nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", length = 64, nullable = false, updatable = false)
    private String requestHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false, updatable = false)
    private NotificationEntity notification;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    protected IdempotencyKeyEntity() {
    }

    public IdempotencyKeyEntity(String idempotencyKey, String requestHash,
                                NotificationEntity notification, Instant expiresAt) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.notification = notification;
        this.expiresAt = expiresAt;
    }

}
