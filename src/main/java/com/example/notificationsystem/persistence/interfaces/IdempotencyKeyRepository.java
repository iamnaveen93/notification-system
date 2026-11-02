package com.example.notificationsystem.persistence.interfaces;

import com.example.notificationsystem.persistence.entities.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
}
