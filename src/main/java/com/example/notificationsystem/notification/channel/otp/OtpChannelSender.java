package com.example.notificationsystem.notification.channel.otp;

import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.notification.channel.ChannelSender;
import com.example.notificationsystem.notification.channel.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OtpChannelSender implements ChannelSender {

    private final OtpProviderDispatcher providerDispatcher;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.OTP;
    }

    @Override
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        providerDispatcher.sendNotification(OtpProvider.WHATSAPP, notificationRequest);
        providerDispatcher.sendNotification(OtpProvider.TWILIO, notificationRequest);
        providerDispatcher.sendNotification(OtpProvider.AWS_SNS, notificationRequest);
    }
}
