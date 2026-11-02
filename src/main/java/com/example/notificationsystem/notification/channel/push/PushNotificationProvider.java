package com.example.notificationsystem.notification.channel.push;

public enum PushNotificationProvider {

    FIREBASE,
    ONESIGNAL;

    /**
     * Converts a provider enum from an external boundary into the internal provider type.
     * The external enum must use the same constant names as this enum.
     */
    public static PushNotificationProvider valueOf(Enum<?> provider) {
        return valueOf(provider.name());
    }
}
