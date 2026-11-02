package com.example.notificationsystem.notification.channel.push;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.notification.channel.ChannelSender;
import com.example.notificationsystem.notification.channel.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushChannelSender implements ChannelSender {

    private final PushProviderDispatcher providerDispatcher;
    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH_NOTIFICATION;
    }

    @Override
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        providerDispatcher.sendNotification(PushNotificationProvider.FIREBASE, notificationRequest);
        providerDispatcher.sendNotification(PushNotificationProvider.ONESIGNAL, notificationRequest);
    }
}
