package com.fm.foodmanagementsystem.core.configs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalDateTime trong entity/DTO được hiểu là “giờ địa bàn” {@link #zoneId}; JSON ra mobile dạng ISO-8601 có offset
 * (vd. {@code 2026-05-03T14:30:00+07:00}) để Flutter parse ổn định, tránh chuỗi không zone.
 */
@Configuration
public class JacksonApiDatetimeConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonApiLocalDateTimeCustomizer(
            @Value("${app.api.datetime-zone:Asia/Ho_Chi_Minh}") String zoneConfig) {
        ZoneId zoneId = ZoneId.of(zoneConfig);
        return builder -> builder.modules(new SimpleModule("ApiLocalDateTime")
                .addSerializer(LocalDateTime.class, new JsonSerializer<>() {
                    @Override
                    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                            throws IOException {
                        if (value == null) {
                            gen.writeNull();
                            return;
                        }
                        String out = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value.atZone(zoneId).toOffsetDateTime());
                        gen.writeString(out);
                    }
                })
                .addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
                    @Override
                    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        String s = p.getValueAsString();
                        if (s == null || s.isBlank()) {
                            return null;
                        }
                        try {
                            return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                    .atZoneSameInstant(zoneId)
                                    .toLocalDateTime();
                        } catch (DateTimeParseException ignored) {
                            return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                    }
                }));
    }
}
