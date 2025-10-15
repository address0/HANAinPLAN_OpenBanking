package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.PhoneVerificationResponseDto;
import com.hanainplan.domain.user.dto.VerifyCodeResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserServerClient userServerClient;

    public String sendVerificationCode(String phoneNumber) {
        try {
            PhoneVerificationResponseDto response = userServerClient.sendVerificationCode(phoneNumber);

            if (response.isSuccess()) {
                log.info("인증번호 발송 성공: phoneNumber={}", phoneNumber);
                return response.getVerificationCode();
            } else {
                log.error("인증번호 발송 실패: phoneNumber={}, message={}", phoneNumber, response.getMessage());
                throw new RuntimeException(response.getMessage());
            }

        } catch (Exception e) {
            log.error("인증번호 전송 실패: phoneNumber={}", phoneNumber, e);
            throw new RuntimeException("인증번호 전송에 실패했습니다.");
        }
    }

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