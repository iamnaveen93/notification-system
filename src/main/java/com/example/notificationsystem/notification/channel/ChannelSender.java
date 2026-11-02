package com.example.notificationsystem.notification.channel;

import com.example.notificationsystem.dto.CreateNotificationRequest;

public interface ChannelSender {

    NotificationChannel getChannel();

    void sendNotification(CreateNotificationRequest notificationRequest);
}
