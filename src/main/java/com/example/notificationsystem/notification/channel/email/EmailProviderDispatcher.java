package com.example.notificationsystem.notification.channel.email;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EmailProviderDispatcher {

    private final Map<EmailProvider, EmailProviderSender> emailRegistry;

    public EmailProviderDispatcher(List<EmailProviderSender> providerSenders) {
        this.emailRegistry = providerSenders.stream()
                .collect(Collectors.toMap(EmailProviderSender::getProvider, sender -> sender));
    }

    public void sendNotification(EmailProvider provider ,CreateNotificationRequest notificationRequest) {
        emailRegistry.get(provider).sendNotification(notificationRequest);
    }
}
