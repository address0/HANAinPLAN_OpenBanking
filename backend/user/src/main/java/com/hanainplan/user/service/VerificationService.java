package com.hanainplan.user.service;

import com.hanainplan.user.entity.VerificationCode;
import com.hanainplan.user.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String sendVerificationCode(String phoneNumber) {
        try {
            verificationCodeRepository.expireExistingCodes(phoneNumber, LocalDateTime.now());

            String code = generateRandomCode();

            VerificationCode verificationCode = VerificationCode.builder()
                    .phoneNumber(phoneNumber)
                    .code(code)
                    .expiresAt(VerificationCode.calculateExpiryTime())
                    .build();

            verificationCodeRepository.save(verificationCode);

            log.info("인증번호 생성 완료: phoneNumber={}, code={}, expiresAt={}", 
                    phoneNumber, code, verificationCode.getExpiresAt());

            return code;

        } catch (Exception e) {
            log.error("인증번호 전송 실패: phoneNumber={}", phoneNumber, e);
            throw new RuntimeException("인증번호 전송에 실패했습니다.");
        }
    }

    @Transactional
    public boolean verifyCode(String phoneNumber, String inputCode) {
        try {
            Optional<VerificationCode> optionalVerificationCode = 
                    verificationCodeRepository.findLatestByPhoneNumber(phoneNumber, LocalDateTime.now());

            if (optionalVerificationCode.isEmpty()) {
                log.warn("인증번호를 찾을 수 없음: phoneNumber={}", phoneNumber);
                return false;
            }

            VerificationCode verificationCode = optionalVerificationCode.get();

            if (verificationCode.isExpired()) {
                log.warn("인증번호 만료: phoneNumber={}, expiresAt={}", phoneNumber, verificationCode.getExpiresAt());
                return false;
            }

            if (Boolean.TRUE.equals(verificationCode.getIsVerified())) {
                log.warn("이미 인증된 번호: phoneNumber={}", phoneNumber);
                return false;
            }

            if (verificationCode.isMaxAttemptsExceeded()) {
                log.warn("최대 시도 횟수 초과: phoneNumber={}, attemptCount={}", phoneNumber, verificationCode.getAttemptCount());
                return false;
            }

            verificationCode.incrementAttemptCount();

            if (verificationCode.getCode().equals(inputCode)) {
                verificationCode.markAsVerified();
                verificationCodeRepository.save(verificationCode);
                log.info("인증번호 확인 성공: phoneNumber={}", phoneNumber);
                return true;
            } else {
                verificationCodeRepository.save(verificationCode);
                log.warn("인증번호 불일치: phoneNumber={}, inputCode={}, attemptCount={}", 
                        phoneNumber, inputCode, verificationCode.getAttemptCount());
                return false;
            }

        } catch (Exception e) {
            log.error("인증번호 확인 실패: phoneNumber={}, inputCode={}", phoneNumber, inputCode, e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean isPhoneNumberVerified(String phoneNumber) {
        Optional<VerificationCode> optionalVerificationCode = 
                verificationCodeRepository.findLatestByPhoneNumber(phoneNumber, LocalDateTime.now());

        return optionalVerificationCode
                .map(code -> Boolean.TRUE.equals(code.getIsVerified()) && !code.isExpired())
                .orElse(false);
    }

    private String generateRandomCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredCodes() {
        try {
            verificationCodeRepository.deleteExpiredCodes(LocalDateTime.now());
            log.debug("만료된 인증번호 정리 완료");
        } catch (Exception e) {
            log.error("만료된 인증번호 정리 실패", e);
        }
    }
}