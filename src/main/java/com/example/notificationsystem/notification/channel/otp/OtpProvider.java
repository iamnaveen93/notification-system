package com.example.notificationsystem.notification.channel.otp;

public enum OtpProvider {

    WHATSAPP,
    TWILIO,
    AWS_SNS;

    /**
     * Converts a provider enum from an external boundary into the internal provider type.
     * The external enum must use the same constant names as this enum.
     */
    public static OtpProvider valueOf(Enum<?> provider) {
        return valueOf(provider.name());
    }
}
