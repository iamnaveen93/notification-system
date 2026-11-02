package com.example.notificationsystem.notification.channel.push.provider;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.notification.channel.push.PushNotificationProvider;
import com.example.notificationsystem.notification.channel.push.PushProviderSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OneSignalPushProvider implements PushProviderSender {

    @Override
    public PushNotificationProvider getProvider() {
        return PushNotificationProvider.ONESIGNAL;
    }

    @Override
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        log.info("Dispatching notification [channel=PUSH_NOTIFICATION, provider=ONE_SIGNAL]");
    }
}
