package com.example.notificationsystem.campaign;

import com.example.notificationsystem.dto.Channel;
import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.dto.Delivery;
import com.example.notificationsystem.dto.Recipient;
import com.example.notificationsystem.notification.channel.ChannelDispatcher;
import com.example.notificationsystem.notification.channel.NotificationChannel;
import com.example.notificationsystem.persistence.entities.CampaignEntity;
import com.example.notificationsystem.persistence.entities.CampaignState;
import com.example.notificationsystem.persistence.interfaces.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drains due campaigns by fanning each audience member out as a single notification through the
 * existing {@link ChannelDispatcher} / {@code ChannelSender} flow (SMS, EMAIL, PUSH).
 * Pause and cancel are honoured between batches; a batch is at most {@code maxPerSecond} recipients.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignRunner {

    private static final int DEFAULT_BATCH_SIZE = 100;

    private final CampaignRepository campaignRepository;
    private final AudienceResolver audienceResolver;
    private final ChannelDispatcher channelDispatcher;
    private final JsonMapper jsonMapper;

    @Scheduled(fixedDelayString = "${campaign.runner.poll-interval-ms:5000}")
    public void run() {
        Instant now = Instant.now();
        campaignRepository.findByStateAndStartAtLessThanEqual(CampaignState.SCHEDULED, now).forEach(this::start);
        campaignRepository.findByState(CampaignState.RUNNING).forEach(this::drain);
    }

    private void start(CampaignEntity campaign) {
        long audienceSize = audienceResolver.resolve(campaign).size();
        if (campaignRepository.markStarted(campaign.getCampaignId(), Instant.now(), audienceSize) == 1) {
            log.info("Campaign started [campaignId={}, audienceSize={}]", campaign.getCampaignId(), audienceSize);
        }
    }

    private void drain(CampaignEntity campaign) {
        String campaignId = campaign.getCampaignId();
        List<Recipient> audience = audienceResolver.resolve(campaign);
        Set<Channel> channels = parseChannels(campaign.getChannels());
        Map<String, String> variables = readVariables(campaign.getVariables());
        int batchSize = campaign.getMaxPerSecond() == null ? DEFAULT_BATCH_SIZE : campaign.getMaxPerSecond();

        int offset = (int) Math.min(campaign.getProcessedOffset(), audience.size());
        while (offset < audience.size()) {
            if (campaignRepository.findStateById(campaignId) != CampaignState.RUNNING) {
                log.info("Campaign drain stopped, no longer running [campaignId={}]", campaignId);
                return;
            }
            if (campaign.getEndBy() != null && Instant.now().isAfter(campaign.getEndBy())) {
                // Contract: messages not sent by endBy are dropped, not sent late.
                long remaining = audience.size() - offset;
                campaignRepository.addProgress(campaignId, remaining, 0, 0, 0, remaining);
                log.info("Campaign passed endBy, expiring remaining recipients [campaignId={}, expired={}]", campaignId, remaining);
                break;
            }

            long batchStart = System.currentTimeMillis();
            int end = Math.min(offset + batchSize, audience.size());
            long sent = 0;
            long failed = 0;
            long suppressed = 0;
            for (Recipient recipient : audience.subList(offset, end)) {
                Set<Channel> reachable = reachableChannels(recipient, channels);
                if (reachable.isEmpty()) {
                    suppressed++;
                    continue;
                }
                CreateNotificationRequest request = new CreateNotificationRequest()
                        .templateId(campaign.getTemplateId())
                        .recipient(recipient)
                        .delivery(new Delivery().channels(reachable))
                        .variables(variables)
                        .locale(campaign.getLocale())
                        .correlationId(campaignId);
                try {
                    reachable.forEach(channel ->
                            channelDispatcher.sendNotification(NotificationChannel.valueOf(channel), request));
                    sent++;
                } catch (RuntimeException exception) {
                    log.warn("Campaign send failed [campaignId={}, customerId={}]", campaignId, recipient.getCustomerId(), exception);
                    failed++;
                }
            }
            campaignRepository.addProgress(campaignId, end - offset, sent, failed, suppressed, 0);
            offset = end;

            if (campaign.getMaxPerSecond() != null) {
                throttle(batchStart);
            }
        }

        if (campaignRepository.compareAndSetState(campaignId, CampaignState.RUNNING, CampaignState.COMPLETED) == 1) {
            log.info("Campaign completed [campaignId={}]", campaignId);
        }
    }

    private Set<Channel> reachableChannels(Recipient recipient, Set<Channel> channels) {
        Set<Channel> reachable = new LinkedHashSet<>();
        for (Channel channel : channels) {
            String contact = switch (channel) {
                case SMS -> recipient.getPhoneNumber();
                case EMAIL -> recipient.getEmailAddress();
                case PUSH -> recipient.getPushToken();
            };
            if (contact != null && !contact.isBlank()) {
                reachable.add(channel);
            }
        }
        return reachable;
    }

    private void throttle(long batchStart) {
        long elapsed = System.currentTimeMillis() - batchStart;
        if (elapsed < 1000) {
            try {
                Thread.sleep(1000 - elapsed);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Set<Channel> parseChannels(String channels) {
        return Arrays.stream(channels.split(","))
                .map(Channel::fromValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, String> readVariables(String variables) {
        if (variables == null) {
            return new HashMap<>();
        }
        return jsonMapper.readValue(variables, new TypeReference<>() {
        });
    }
}
