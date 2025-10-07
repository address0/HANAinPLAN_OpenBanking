package com.hanainplan.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Jackson 설정
 * - 날짜/시간 직렬화/역직렬화 설정
 * - 타임존 포함 ISO-8601 형식 지원
 */
@Configuration
public class JacksonConfig {

    /**
     * 타임존을 지원하는 LocalDateTime Deserializer
     */
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalStart()
            .appendOffset("+HH:MM", "Z")  // 타임존 정보가 있으면 파싱하고 무시
            .optionalEnd()
            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
            .toFormatter();

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .modules(javaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .build();
    }

    /**
     * Java Time Module 설정
     */
    private JavaTimeModule javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        
        // LocalDateTime Deserializer: 타임존 정보를 무시하고 로컬 시간만 사용
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(FLEXIBLE_FORMATTER));
        
        // LocalDateTime Serializer: 타임존 정보 없이 직렬화
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return module;
    }
}

