package com.example.notificationsystem.notification.channel.email;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.notification.channel.ChannelSender;
import com.example.notificationsystem.notification.channel.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailChannelSender implements ChannelSender {

    private final EmailProviderDispatcher providerDispatcher;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        providerDispatcher.sendNotification(EmailProvider.SMTP, notificationRequest);
        providerDispatcher.sendNotification(EmailProvider.GRID, notificationRequest);
    }
}
