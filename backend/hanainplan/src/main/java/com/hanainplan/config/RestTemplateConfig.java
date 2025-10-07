package com.hanainplan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RestTemplate 설정
 * - 외부 API 호출용 RestTemplate Bean
 */
@Configuration
public class RestTemplateConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // JSON 메시지 컨버터 설정 (JacksonConfig의 objectMapper 사용)
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        restTemplate.getMessageConverters().add(0, converter);
        
        return restTemplate;
    }
}