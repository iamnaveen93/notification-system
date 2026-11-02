package com.example.notificationsystem.service;

import com.example.notificationsystem.dto.AudienceOneOf;
import com.example.notificationsystem.dto.AudienceOneOf1;
import com.example.notificationsystem.dto.CampaignAccepted;
import com.example.notificationsystem.dto.CampaignCounters;
import com.example.notificationsystem.dto.CampaignStatus;
import com.example.notificationsystem.dto.Channel;
import com.example.notificationsystem.dto.CreateCampaignRequest;
import com.example.notificationsystem.dto.ListCampaigns200Response;
import com.example.notificationsystem.persistence.entities.CampaignEntity;
import com.example.notificationsystem.persistence.entities.CampaignIdempotencyKeyEntity;
import com.example.notificationsystem.persistence.entities.CampaignState;
import com.example.notificationsystem.persistence.interfaces.CampaignIdempotencyKeyRepository;
import com.example.notificationsystem.persistence.interfaces.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private static final String STATUS_URL_PREFIX = "/api/v1/campaigns/";

    private final CampaignRepository campaignRepository;
    private final CampaignIdempotencyKeyRepository idempotencyKeyRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public CreateResult create(String idempotencyKey, CreateCampaignRequest request) {
        validate(request);

        String requestHash = requestHash(request);
        CampaignIdempotencyKeyEntity existing = idempotencyKeyRepository.findById(idempotencyKey).orElse(null);

        if (existing != null && existing.getExpiresAt().isAfter(Instant.now())) {
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency key was already used with a different request");
            }
            return new CreateResult(toAccepted(existing.getCampaign()), true);
        }

        if (existing != null) {
            idempotencyKeyRepository.delete(existing);
            idempotencyKeyRepository.flush();
        }

        CampaignEntity campaign = CampaignEntity.builder()
                .campaignId("cmp_" + UUID.randomUUID())
                .name(request.getName())
                .templateId(request.getTemplateId())
                .audienceSegmentId(request.getAudience() instanceof AudienceOneOf segment ? segment.getSegmentId() : null)
                .audienceFileRef(request.getAudience() instanceof AudienceOneOf1 file ? file.getAudienceFileRef() : null)
                .channels(request.getDelivery().getChannels().stream().map(Channel::getValue).collect(Collectors.joining(",")))
                .variables(writeJson(request.getVariables()))
                .locale(request.getLocale())
                .startAt(request.getSchedule().getStartAt().toInstant())
                .endBy(request.getSchedule().getEndBy() == null ? null : request.getSchedule().getEndBy().toInstant())
                .maxPerSecond(request.getThrottle() == null ? null : request.getThrottle().getMaxPerSecond())
                .build();
        campaignRepository.save(campaign);
        log.info("Campaign scheduled [campaignId={}, templateId={}, startAt={}]",
                campaign.getCampaignId(), campaign.getTemplateId(), campaign.getStartAt());

        idempotencyKeyRepository.save(new CampaignIdempotencyKeyEntity(
                idempotencyKey, requestHash, campaign, Instant.now().plusSeconds(24 * 60 * 60)));

        return new CreateResult(toAccepted(campaign), false);
    }

    @Transactional(readOnly = true)
    public CampaignStatus get(String campaignId) {
        return toStatus(find(campaignId));
    }

    @Transactional(readOnly = true)
    public ListCampaigns200Response list(com.example.notificationsystem.dto.CampaignState state, int limit, String cursor) {
        if (limit < 1 || limit > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 200");
        }
        CampaignState filter = state == null ? null : CampaignState.valueOf(state.name());
        // Fetch one extra row to know whether another page exists.
        List<CampaignEntity> page = campaignRepository.findPage(filter, cursor, PageRequest.of(0, limit + 1));
        boolean hasMore = page.size() > limit;
        List<CampaignEntity> items = hasMore ? page.subList(0, limit) : page;

        return new ListCampaigns200Response()
                .items(items.stream().map(this::toStatus).toList())
                .nextCursor(hasMore ? items.getLast().getCampaignId() : null);
    }

    @Transactional
    public CampaignStatus pause(String campaignId) {
        return transition(campaignId, EnumSet.of(CampaignState.SCHEDULED, CampaignState.RUNNING), CampaignState.PAUSED);
    }

    @Transactional
    public CampaignStatus resume(String campaignId) {
        CampaignEntity campaign = find(campaignId);
        // A campaign that never started goes back to waiting for its start time.
        CampaignState target = campaign.getStartedAt() == null ? CampaignState.SCHEDULED : CampaignState.RUNNING;
        return transition(campaign, EnumSet.of(CampaignState.PAUSED), target);
    }

    @Transactional
    public CampaignStatus cancel(String campaignId) {
        return transition(campaignId,
                EnumSet.of(CampaignState.SCHEDULED, CampaignState.RUNNING, CampaignState.PAUSED), CampaignState.CANCELLED);
    }

    private CampaignStatus transition(String campaignId, Set<CampaignState> allowedFrom, CampaignState target) {
        return transition(find(campaignId), allowedFrom, target);
    }

    private CampaignStatus transition(CampaignEntity campaign, Set<CampaignState> allowedFrom, CampaignState target) {
        if (!allowedFrom.contains(campaign.getState())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Campaign in state " + campaign.getState() + " cannot move to " + target);
        }
        log.info("Campaign state change [campaignId={}, from={}, to={}]", campaign.getCampaignId(), campaign.getState(), target);
        campaign.setState(target);
        return toStatus(campaignRepository.saveAndFlush(campaign));
    }

    private CampaignEntity find(String campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    private void validate(CreateCampaignRequest request) {
        if (request.getAudience() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "audience must reference a segmentId or audienceFileRef");
        }
        if (request.getDelivery() == null || request.getDelivery().getChannels().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "delivery.channels must not be empty");
        }
        if (request.getSchedule() == null || request.getSchedule().getStartAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "schedule.startAt is required");
        }
        if (request.getSchedule().getEndBy() != null
                && !request.getSchedule().getEndBy().isAfter(request.getSchedule().getStartAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "schedule.endBy must be after schedule.startAt");
        }
    }

    private CampaignAccepted toAccepted(CampaignEntity campaign) {
        return new CampaignAccepted()
                .campaignId(campaign.getCampaignId())
                .state(CampaignAccepted.StateEnum.SCHEDULED)
                .statusUrl(STATUS_URL_PREFIX + campaign.getCampaignId());
    }

    private CampaignStatus toStatus(CampaignEntity campaign) {
        return new CampaignStatus()
                .campaignId(campaign.getCampaignId())
                .name(campaign.getName())
                .state(com.example.notificationsystem.dto.CampaignState.valueOf(campaign.getState().name()))
                .startedAt(campaign.getStartedAt() == null ? null : campaign.getStartedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(campaign.getUpdatedAt() == null ? null : campaign.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .counters(new CampaignCounters()
                        .audienceSize(campaign.getAudienceSize())
                        .queued(campaign.getQueued())
                        .sent(campaign.getSent())
                        .delivered(campaign.getDelivered())
                        .failed(campaign.getFailed())
                        .suppressed(campaign.getSuppressed())
                        .expired(campaign.getExpired()));
    }

    private String writeJson(Object value) {
        return jsonMapper.writeValueAsString(value);
    }

    private String requestHash(CreateCampaignRequest request) {
        try {
            JsonMapper canonicalMapper = jsonMapper.rebuild()
                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                    .build();
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalMapper.writeValueAsBytes(request));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not create request hash", exception);
        }
    }

    public record CreateResult(CampaignAccepted response, boolean replayed) {
    }
}
