package com.example.notificationsystem.notification.channel.email;

public enum EmailProvider {
    SMTP,
    GRID;

    /**
     * Converts a provider enum from an external boundary into the internal provider type.
     * The external enum must use the same constant names as this enum.
     */
    public static EmailProvider valueOf(Enum<?> provider) {
        return valueOf(provider.name());
    }
}
