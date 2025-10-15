package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UserServerClient {

    private final RestTemplate restTemplate;
    private final String userServerUrl;

    public UserServerClient(RestTemplate restTemplate, @Value("${user.server.url:http://localhost:8084}") String userServerUrl) {
        this.restTemplate = restTemplate;
        this.userServerUrl = userServerUrl;
    }

    public PhoneVerificationResponseDto sendVerificationCode(String phoneNumber) {
        try {
            String url = userServerUrl + "/api/user/phone/send";

            PhoneVerificationRequestDto request = new PhoneVerificationRequestDto(phoneNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PhoneVerificationRequestDto> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PhoneVerificationResponseDto> response = restTemplate.postForEntity(
                url, entity, PhoneVerificationResponseDto.class);

            log.info("인증번호 발송 요청 성공: phoneNumber={}, response={}", phoneNumber, response.getBody());
            return response.getBody();

        } catch (Exception e) {
            log.error("인증번호 발송 요청 실패: phoneNumber={}", phoneNumber, e);
            return new PhoneVerificationResponseDto(false, "인증번호 발송에 실패했습니다.", null);
        }
    }

    public VerifyCodeResponseDto verifyCode(String phoneNumber, String verificationCode) {
        try {
            String url = userServerUrl + "/api/user/phone/verify";

            VerifyCodeRequestDto request = new VerifyCodeRequestDto(phoneNumber, verificationCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<VerifyCodeRequestDto> entity = new HttpEntity<>(request, headers);

            ResponseEntity<VerifyCodeResponseDto> response = restTemplate.postForEntity(
                url, entity, VerifyCodeResponseDto.class);

            log.info("인증번호 검증 요청 성공: phoneNumber={}, verified={}", phoneNumber, response.getBody().isVerified());
            return response.getBody();

        } catch (Exception e) {
            log.error("인증번호 검증 요청 실패: phoneNumber={}, verificationCode={}", phoneNumber, verificationCode, e);
            return new VerifyCodeResponseDto(false, "인증번호 검증에 실패했습니다.", false);
        }
    }

    public VerifyCodeResponseDto checkVerificationStatus(String phoneNumber) {
        try {
            String url = userServerUrl + "/api/user/phone/status/" + phoneNumber;

            ResponseEntity<VerifyCodeResponseDto> response = restTemplate.getForEntity(
                url, VerifyCodeResponseDto.class);

            log.info("인증 상태 확인 요청 성공: phoneNumber={}, verified={}", phoneNumber, response.getBody().isVerified());
            return response.getBody();

        } catch (Exception e) {
            log.error("인증 상태 확인 요청 실패: phoneNumber={}", phoneNumber, e);
            return new VerifyCodeResponseDto(false, "인증 상태 확인에 실패했습니다.", false);
        }
    }
}