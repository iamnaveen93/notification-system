package com.example.notificationsystem.notification.channel.push;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PushProviderDispatcher {

    private final Map<PushNotificationProvider, PushProviderSender> providerSenders;

    public PushProviderDispatcher(List<PushProviderSender> providerSenders) {
        this.providerSenders = providerSenders.stream()
                .collect(Collectors.toMap(PushProviderSender::getProvider, sender -> sender));
    }

    public void sendNotification(PushNotificationProvider pushNotificationProvider,CreateNotificationRequest createNotificationRequest) {
        providerSenders.get(pushNotificationProvider).sendNotification(createNotificationRequest);
    }
}
