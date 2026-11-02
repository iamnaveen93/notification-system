package com.example.notificationsystem.controller;

import com.example.notificationsystem.constants.ResourcesConstants;
import com.example.notificationsystem.dto.CampaignAccepted;
import com.example.notificationsystem.dto.CampaignState;
import com.example.notificationsystem.dto.CampaignStatus;
import com.example.notificationsystem.dto.CreateCampaignRequest;
import com.example.notificationsystem.dto.ListCampaigns200Response;
import com.example.notificationsystem.service.CampaignService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping(path = ResourcesConstants.CAMPAIGN_URL,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CampaignAccepted> createCampaign(
            @RequestHeader("x-header-idempotency-key") String idempotencyKey,
            @Valid @RequestBody CreateCampaignRequest campaignRequest) {
        CampaignService.CreateResult result = campaignService.create(idempotencyKey, campaignRequest);
        ResponseEntity.BodyBuilder response = ResponseEntity.accepted();
        if (result.replayed()) {
            response.header("x-header-idempotent-replayed", "true");
        }
        return response.body(result.response());
    }

    @GetMapping
    public ResponseEntity<ListCampaigns200Response> listCampaigns(
            @RequestParam(name = "state", required = false) CampaignState state,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "cursor", required = false) String cursor) {
        return ResponseEntity.ok(campaignService.list(state, limit, cursor));
    }

    @GetMapping(ResourcesConstants.CAMPAIGN_ID_URL)
    public ResponseEntity<CampaignStatus> getCampaign(@PathVariable("campaignId") String campaignId) {
        return ResponseEntity.ok(campaignService.get(campaignId));
    }

    @PostMapping("/{campaignId}/pause")
    public ResponseEntity<CampaignStatus> pauseCampaign(@PathVariable("campaignId") String campaignId) {
        return ResponseEntity.ok(campaignService.pause(campaignId));
    }

    @PostMapping("/{campaignId}/resume")
    public ResponseEntity<CampaignStatus> resumeCampaign(@PathVariable("campaignId") String campaignId) {
        return ResponseEntity.ok(campaignService.resume(campaignId));
    }

    @PostMapping("/{campaignId}/cancel")
    public ResponseEntity<CampaignStatus> cancelCampaign(@PathVariable("campaignId") String campaignId) {
        return ResponseEntity.ok(campaignService.cancel(campaignId));
    }
}
