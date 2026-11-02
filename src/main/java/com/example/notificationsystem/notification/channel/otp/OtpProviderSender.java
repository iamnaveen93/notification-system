package com.example.notificationsystem.notification.channel.otp;

import com.example.notificationsystem.dto.CreateNotificationRequest;

public interface OtpProviderSender {

    OtpProvider getProvider();

    void sendNotification(CreateNotificationRequest notificationRequest);
}
