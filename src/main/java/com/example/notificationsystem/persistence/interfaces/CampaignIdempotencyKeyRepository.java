package com.example.notificationsystem.persistence.interfaces;

import com.example.notificationsystem.persistence.entities.CampaignIdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignIdempotencyKeyRepository extends JpaRepository<CampaignIdempotencyKeyEntity, String> {
}
