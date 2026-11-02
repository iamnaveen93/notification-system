package com.example.notificationsystem.notification.channel.otp.provider;


import com.example.notificationsystem.dto.CreateNotificationRequest;
import com.example.notificationsystem.notification.channel.otp.OtpProvider;
import com.example.notificationsystem.notification.channel.otp.OtpProviderSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwilioOtpProvider implements OtpProviderSender {
    @Override
    public OtpProvider getProvider() {
        return OtpProvider.TWILIO;
    }

    @Override
    public void sendNotification(CreateNotificationRequest notificationRequest) {
        log.info("Dispatching notification [channel=OTP, provider=TWILIO]");
    }
}
