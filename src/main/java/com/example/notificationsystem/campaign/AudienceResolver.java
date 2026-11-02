package com.example.notificationsystem.campaign;

import com.example.notificationsystem.dto.Recipient;
import com.example.notificationsystem.persistence.entities.CampaignEntity;

import java.util.List;

/**
 * Resolves a campaign's audience reference (segmentId or audienceFileRef) into recipients.
 * The contract never inlines recipients, so the platform needs a segment / audience-file source behind this.
 */
public interface AudienceResolver {

    List<Recipient> resolve(CampaignEntity campaign);
}
