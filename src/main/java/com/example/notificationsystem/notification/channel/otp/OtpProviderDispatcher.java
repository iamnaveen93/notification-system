package com.example.notificationsystem.notification.channel.otp;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OtpProviderDispatcher {

    private final Map<OtpProvider, OtpProviderSender> providerSenders;

    public OtpProviderDispatcher(List<OtpProviderSender> providerSenders) {
        this.providerSenders = providerSenders.stream()
                .collect(Collectors.toMap(OtpProviderSender::getProvider, sender -> sender));
    }

    public void sendNotification(OtpProvider otpProvider, CreateNotificationRequest notificationRequest) {
       providerSenders.get(otpProvider).sendNotification(notificationRequest);
    }
}
