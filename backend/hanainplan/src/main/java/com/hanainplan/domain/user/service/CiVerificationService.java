package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.CiVerificationRequestDto;
import com.hanainplan.domain.user.dto.CiVerificationResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CiVerificationService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String CI_SERVER_URL = "http://localhost:8084/api/user/ci/verify";

    public CiVerificationResponseDto verifyCi(CiVerificationRequestDto request) {
        try {
            CiVerificationResponseDto response = restTemplate.postForObject(
                CI_SERVER_URL, 
                request, 
                CiVerificationResponseDto.class
            );

            if (response != null && response.isSuccess()) {
                return CiVerificationResponseDto.success(response.getCi());
            } else {
                return CiVerificationResponseDto.error("실명인증에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return CiVerificationResponseDto.error("실명인증 서버 연결 실패: " + e.getMessage());
        }
    }
}