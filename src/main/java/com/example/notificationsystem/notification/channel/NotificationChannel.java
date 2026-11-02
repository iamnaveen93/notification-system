package com.example.notificationsystem.notification.channel;

import com.example.notificationsystem.dto.Channel;

public enum NotificationChannel {

    OTP,
    EMAIL,
    PUSH_NOTIFICATION;

    public static NotificationChannel valueOf(Channel channel) {
        return switch (channel) {
            case SMS -> OTP;
            case EMAIL -> EMAIL;
            case PUSH -> PUSH_NOTIFICATION;
        };
    }
}
