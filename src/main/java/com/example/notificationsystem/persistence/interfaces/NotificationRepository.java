package com.example.notificationsystem.persistence.interfaces;

import com.example.notificationsystem.persistence.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
}
