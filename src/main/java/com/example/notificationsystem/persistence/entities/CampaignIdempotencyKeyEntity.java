package com.example.notificationsystem.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "campaign_idempotency_keys")
public class CampaignIdempotencyKeyEntity extends BaseEntity {

    @Id
    @Column(name = "idempotency_key", length = 128, nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", length = 64, nullable = false, updatable = false)
    private String requestHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false, updatable = false)
    private CampaignEntity campaign;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    protected CampaignIdempotencyKeyEntity() {
    }

    public CampaignIdempotencyKeyEntity(String idempotencyKey, String requestHash,
                                        CampaignEntity campaign, Instant expiresAt) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.campaign = campaign;
        this.expiresAt = expiresAt;
    }
}
