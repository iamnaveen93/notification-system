package com.example.notificationsystem.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "campaigns")
public class CampaignEntity extends BaseEntity {

    @Id
    @Column(name = "campaign_id", length = 64, nullable = false, updatable = false)
    private String campaignId;

    @Column(nullable = false)
    private String name;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CampaignState state;

    @Column(name = "audience_segment_id")
    private String audienceSegmentId;

    @Column(name = "audience_file_ref", length = 1024)
    private String audienceFileRef;

    /** Comma-separated contract channels (SMS, EMAIL, PUSH), in request order. */
    @Column(nullable = false, length = 64)
    private String channels;

    /** Campaign-wide variables serialized as JSON. */
    @Column(columnDefinition = "TEXT")
    private String variables;

    @Column(length = 32)
    private String locale;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_by")
    private Instant endBy;

    @Column(name = "max_per_second")
    private Integer maxPerSecond;

    @Column(name = "started_at")
    private Instant startedAt;

    /** Number of audience members already processed, so a resumed campaign continues where it stopped. */
    @Column(name = "processed_offset", nullable = false)
    private long processedOffset;

    @Column(name = "audience_size", nullable = false)
    private long audienceSize;

    @Column(nullable = false)
    private long queued;

    @Column(nullable = false)
    private long sent;

    @Column(nullable = false)
    private long delivered;

    @Column(nullable = false)
    private long failed;

    @Column(nullable = false)
    private long suppressed;

    @Column(nullable = false)
    private long expired;

    protected CampaignEntity() {
    }

    @Builder
    public CampaignEntity(String campaignId, String name, String templateId, String audienceSegmentId,
                          String audienceFileRef, String channels, String variables, String locale,
                          Instant startAt, Instant endBy, Integer maxPerSecond) {
        this.campaignId = campaignId;
        this.name = name;
        this.templateId = templateId;
        this.state = CampaignState.SCHEDULED;
        this.audienceSegmentId = audienceSegmentId;
        this.audienceFileRef = audienceFileRef;
        this.channels = channels;
        this.variables = variables;
        this.locale = locale;
        this.startAt = startAt;
        this.endBy = endBy;
        this.maxPerSecond = maxPerSecond;
    }
}
