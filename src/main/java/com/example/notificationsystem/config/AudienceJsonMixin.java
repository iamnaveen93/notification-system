package com.example.notificationsystem.config;

import com.example.notificationsystem.dto.Audience;
import com.example.notificationsystem.dto.AudienceOneOf;
import com.example.notificationsystem.dto.AudienceOneOf1;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.boot.jackson.JacksonMixin;

/**
 * The generator emits {@code Audience} (a oneOf) as a bare interface, which Jackson cannot deserialize.
 * Deduce the concrete type from which property is present: segmentId or audienceFileRef.
 */
@JacksonMixin(Audience.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(AudienceOneOf.class),
        @JsonSubTypes.Type(AudienceOneOf1.class)
})
public interface AudienceJsonMixin {
}
