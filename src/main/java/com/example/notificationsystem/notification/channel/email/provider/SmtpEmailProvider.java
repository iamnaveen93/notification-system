package com.example.notificationsystem.notification.channel.email.provider;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.notification.channel.email.EmailProvider;
import com.example.notificationsystem.notification.channel.email.EmailProviderSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmtpEmailProvider implements EmailProviderSender {
    @Override
    public EmailProvider getProvider() {
        return EmailProvider.SMTP;
    }

    @Override
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        log.info("Dispatching notification [channel=EMAIL, provider=SMTP]");
    }
}
