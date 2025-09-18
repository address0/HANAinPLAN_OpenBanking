package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.PhoneVerificationResponseDto;
import com.hanainplan.domain.user.dto.VerifyCodeResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 인증번호 서비스 (User 서버 호출)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserServerClient userServerClient;

    /**
     * 인증번호 생성 및 전송
     */
    public String sendVerificationCode(String phoneNumber) {
        try {
            PhoneVerificationResponseDto response = userServerClient.sendVerificationCode(phoneNumber);
            
            if (response.isSuccess()) {
                log.info("인증번호 발송 성공: phoneNumber={}", phoneNumber);
                return response.getVerificationCode(); // 개발용으로 인증번호 반환
            } else {
                log.error("인증번호 발송 실패: phoneNumber={}, message={}", phoneNumber, response.getMessage());
                throw new RuntimeException(response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("인증번호 전송 실패: phoneNumber={}", phoneNumber, e);
            throw new RuntimeException("인증번호 전송에 실패했습니다.");
        }
    }

    /**
     * 인증번호 확인
     */
    public boolean verifyCode(String phoneNumber, String inputCode) {
        try {
            VerifyCodeResponseDto response = userServerClient.verifyCode(phoneNumber, inputCode);
            
            if (response.isSuccess()) {
                log.info("인증번호 확인 성공: phoneNumber={}, verified={}", phoneNumber, response.isVerified());
                return response.isVerified();
            } else {
                log.error("인증번호 확인 실패: phoneNumber={}, message={}", phoneNumber, response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("인증번호 확인 실패: phoneNumber={}, inputCode={}", phoneNumber, inputCode, e);
            return false;
        }
    }

    /**
     * 전화번호 인증 완료 여부 확인
     */
    public boolean isPhoneNumberVerified(String phoneNumber) {
        try {
            VerifyCodeResponseDto response = userServerClient.checkVerificationStatus(phoneNumber);
            
            if (response.isSuccess()) {
                log.info("인증 상태 확인 성공: phoneNumber={}, verified={}", phoneNumber, response.isVerified());
                return response.isVerified();
            } else {
                log.error("인증 상태 확인 실패: phoneNumber={}, message={}", phoneNumber, response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("인증 상태 확인 실패: phoneNumber={}", phoneNumber, e);
            return false;
        }
    }
}
