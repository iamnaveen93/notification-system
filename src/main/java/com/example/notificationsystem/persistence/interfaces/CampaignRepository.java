package com.example.notificationsystem.persistence.interfaces;

import com.example.notificationsystem.persistence.entities.CampaignEntity;
import com.example.notificationsystem.persistence.entities.CampaignState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface CampaignRepository extends JpaRepository<CampaignEntity, String> {

    /** Keyset page ordered by campaignId; null state/cursor mean "no filter". */
    @Query("""
            select c from CampaignEntity c
            where (:state is null or c.state = :state)
              and (:cursor is null or c.campaignId > :cursor)
            order by c.campaignId
            """)
    List<CampaignEntity> findPage(@Param("state") CampaignState state,
                                  @Param("cursor") String cursor,
                                  Pageable pageable);

    List<CampaignEntity> findByStateAndStartAtLessThanEqual(CampaignState state, Instant now);

    List<CampaignEntity> findByState(CampaignState state);

    @Query("select c.state from CampaignEntity c where c.campaignId = :campaignId")
    CampaignState findStateById(@Param("campaignId") String campaignId);

    /** Moves state only if the campaign is still in the expected state, so it never overwrites a concurrent pause/cancel. */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            update CampaignEntity c set c.state = :target, c.updatedAt = CURRENT_TIMESTAMP
            where c.campaignId = :campaignId and c.state = :expected
            """)
    int compareAndSetState(@Param("campaignId") String campaignId,
                           @Param("expected") CampaignState expected,
                           @Param("target") CampaignState target);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            update CampaignEntity c
            set c.state = com.example.notificationsystem.persistence.entities.CampaignState.RUNNING,
                c.startedAt = :startedAt, c.audienceSize = :audienceSize, c.updatedAt = CURRENT_TIMESTAMP
            where c.campaignId = :campaignId
              and c.state = com.example.notificationsystem.persistence.entities.CampaignState.SCHEDULED
            """)
    int markStarted(@Param("campaignId") String campaignId,
                    @Param("startedAt") Instant startedAt,
                    @Param("audienceSize") long audienceSize);

    /** Records drain progress without touching state. */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            update CampaignEntity c set c.processedOffset = c.processedOffset + :processed,
                   c.sent = c.sent + :sent, c.failed = c.failed + :failed,
                   c.suppressed = c.suppressed + :suppressed, c.expired = c.expired + :expired,
                   c.updatedAt = CURRENT_TIMESTAMP
            where c.campaignId = :campaignId
            """)
    int addProgress(@Param("campaignId") String campaignId,
                    @Param("processed") long processed,
                    @Param("sent") long sent,
                    @Param("failed") long failed,
                    @Param("suppressed") long suppressed,
                    @Param("expired") long expired);
}
