package com.example.notificationsystem.notification.channel.push;

import com.example.notificationsystem.dto.CreateNotificationRequest;

public interface PushProviderSender {

    PushNotificationProvider getProvider();

    void sendNotification(CreateNotificationRequest notificationRequest);
}
