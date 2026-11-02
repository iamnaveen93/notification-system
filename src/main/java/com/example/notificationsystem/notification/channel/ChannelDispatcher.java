package com.example.notificationsystem.notification.channel;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChannelDispatcher {

    private final Map<NotificationChannel, ChannelSender> channelRegistry;

    public ChannelDispatcher(List<ChannelSender> channelSenders) {
        this.channelRegistry = channelSenders.stream()
                .collect(Collectors.toMap(ChannelSender::getChannel, sender -> sender));
    }

    public void sendNotification(NotificationChannel notificationChannel, CreateNotificationRequest notificationRequest) {
        ChannelSender channelSender = channelRegistry.get(notificationChannel);
        if (channelSender != null) {
            channelSender.sendNotification(notificationRequest);
        }
        log.info("Notification dispatch completed [channel={}]", notificationChannel);
    }
}
