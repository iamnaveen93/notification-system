package com.example.notificationsystem.notification.channel.email;

import com.example.notificationsystem.dto.CreateNotificationRequest;

public interface EmailProviderSender {

    EmailProvider getProvider();

    void sendNotification(CreateNotificationRequest notificationRequest);
}
