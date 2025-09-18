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

/**
 * 인증번호 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 인증번호 생성 및 전송
     */
    @Transactional
    public String sendVerificationCode(String phoneNumber) {
        try {
            // 기존 인증번호 만료 처리
            verificationCodeRepository.expireExistingCodes(phoneNumber, LocalDateTime.now());

            // 6자리 랜덤 인증번호 생성
            String code = generateRandomCode();

            // 인증번호 엔터티 생성
            VerificationCode verificationCode = VerificationCode.builder()
                    .phoneNumber(phoneNumber)
                    .code(code)
                    .expiresAt(VerificationCode.calculateExpiryTime()) // 3분 후 만료
                    .build();

            verificationCodeRepository.save(verificationCode);

            log.info("인증번호 생성 완료: phoneNumber={}, code={}, expiresAt={}", 
                    phoneNumber, code, verificationCode.getExpiresAt());

            // TODO: 실제 SMS 발송 로직 추가
            // smsService.sendSMS(phoneNumber, "하나인플랜 인증번호: " + code);

            return code; // 개발용으로 인증번호 반환 (실제 운영시에는 제거)

        } catch (Exception e) {
            log.error("인증번호 전송 실패: phoneNumber={}", phoneNumber, e);
            throw new RuntimeException("인증번호 전송에 실패했습니다.");
        }
    }

    /**
     * 인증번호 확인
     */
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

            // 만료 확인
            if (verificationCode.isExpired()) {
                log.warn("인증번호 만료: phoneNumber={}, expiresAt={}", phoneNumber, verificationCode.getExpiresAt());
                return false;
            }

            // 이미 인증된 경우
            if (Boolean.TRUE.equals(verificationCode.getIsVerified())) {
                log.warn("이미 인증된 번호: phoneNumber={}", phoneNumber);
                return false;
            }

            // 최대 시도 횟수 확인
            if (verificationCode.isMaxAttemptsExceeded()) {
                log.warn("최대 시도 횟수 초과: phoneNumber={}, attemptCount={}", phoneNumber, verificationCode.getAttemptCount());
                return false;
            }

            // 시도 횟수 증가
            verificationCode.incrementAttemptCount();

            // 인증번호 확인
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

    /**
     * 전화번호 인증 완료 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isPhoneNumberVerified(String phoneNumber) {
        Optional<VerificationCode> optionalVerificationCode = 
                verificationCodeRepository.findLatestByPhoneNumber(phoneNumber, LocalDateTime.now());

        return optionalVerificationCode
                .map(code -> Boolean.TRUE.equals(code.getIsVerified()) && !code.isExpired())
                .orElse(false);
    }

    /**
     * 6자리 랜덤 인증번호 생성
     */
    private String generateRandomCode() {
        int code = 100000 + secureRandom.nextInt(900000); // 100000~999999
        return String.valueOf(code);
    }

    /**
     * 만료된 인증번호 정리 (매 시간마다 실행)
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
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
