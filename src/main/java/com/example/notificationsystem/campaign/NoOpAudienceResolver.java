package com.example.notificationsystem.campaign;

import com.example.notificationsystem.dto.Recipient;
import com.example.notificationsystem.persistence.entities.CampaignEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Placeholder until a segment / audience-file service exists. Replace this bean with a real
 * {@link AudienceResolver} implementation.
 */
@Component
@Slf4j
public class NoOpAudienceResolver implements AudienceResolver {

    @Override
    public List<Recipient> resolve(CampaignEntity campaign) {
        log.warn("No audience source configured, resolving empty audience [campaignId={}, segmentId={}, audienceFileRef={}]",
                campaign.getCampaignId(), campaign.getAudienceSegmentId(), campaign.getAudienceFileRef());
        return List.of();
    }
}
