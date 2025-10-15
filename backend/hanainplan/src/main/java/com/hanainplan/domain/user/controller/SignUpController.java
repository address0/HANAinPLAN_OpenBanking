package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.SignUpRequestDto;
import com.hanainplan.domain.user.dto.SignUpResponseDto;
import com.hanainplan.domain.user.service.SignUpService;
import com.hanainplan.domain.user.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "회원가입", description = "사용자 회원가입 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class SignUpController {

    private final SignUpService signUpService;
    private final VerificationService verificationService;

    @Operation(summary = "회원가입", description = "일반고객 또는 상담원 회원가입을 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request) {
        log.info("회원가입 요청: userType={}, name={}, phoneNumber={}", 
                request.getUserType(), request.getName(), request.getPhoneNumber());

        SignUpResponseDto response = signUpService.signUp(request);

        if (response.getUserId() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "주민번호 중복 확인", description = "주민번호가 이미 가입되어 있는지 확인합니다.")
    @GetMapping("/check-social-number")
    public ResponseEntity<Map<String, Object>> checkSocialNumber(
            @Parameter(description = "주민번호 (000000-0 형식)", example = "900101-1")
            @RequestParam String socialNumber) {
        log.info("주민번호 중복 확인 요청: socialNumber={}", socialNumber);

        boolean isDuplicate = signUpService.isDuplicateSocialNumber(socialNumber);

        return ResponseEntity.ok(Map.of(
                "isDuplicate", isDuplicate,
                "message", isDuplicate ? "이미 가입된 주민번호입니다." : "사용 가능한 주민번호입니다."
        ));
    }

    @GetMapping("/check-phone-number")
    public ResponseEntity<Map<String, Object>> checkPhoneNumber(@RequestParam String phoneNumber) {
        log.info("전화번호 중복 확인 요청: phoneNumber={}", phoneNumber);

        boolean isDuplicate = signUpService.isDuplicatePhoneNumber(phoneNumber);

        return ResponseEntity.ok(Map.of(
                "isDuplicate", isDuplicate,
                "message", isDuplicate ? "이미 가입된 전화번호입니다." : "사용 가능한 전화번호입니다."
        ));
    }

    @GetMapping("/check-kakao-id")
    public ResponseEntity<Map<String, Object>> checkKakaoId(@RequestParam String kakaoId) {
        log.info("카카오 ID 중복 확인 요청: kakaoId={}", kakaoId);

        boolean isDuplicate = signUpService.isDuplicateKakaoId(kakaoId);

        return ResponseEntity.ok(Map.of(
                "isDuplicate", isDuplicate,
                "message", isDuplicate ? "이미 가입된 카카오 계정입니다." : "사용 가능한 카카오 계정입니다."
        ));
    }

    @Operation(summary = "인증번호 전송", description = "전화번호로 6자리 인증번호를 전송합니다. (3분간 유효)")
    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        log.info("인증번호 전송 요청: phoneNumber={}", phoneNumber);

        try {
            String verificationCode = verificationService.sendVerificationCode(phoneNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증번호가 전송되었습니다. (3분간 유효)",
                    "verificationCode", verificationCode,
                    "expiresInMinutes", 3
            ));
        } catch (Exception e) {
            log.error("인증번호 전송 실패: phoneNumber={}", phoneNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "인증번호 전송에 실패했습니다. 다시 시도해주세요."
            ));
        }
    }

    @Operation(summary = "인증번호 확인", description = "전송된 인증번호를 확인합니다.")
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String verificationCode = request.get("verificationCode");
        log.info("인증번호 확인 요청: phoneNumber={}, code={}", phoneNumber, verificationCode);

        try {
            boolean isValid = verificationService.verifyCode(phoneNumber, verificationCode);

            if (isValid) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "인증이 완료되었습니다."
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "인증번호가 올바르지 않거나 만료되었습니다."
                ));
            }
        } catch (Exception e) {
            log.error("인증번호 확인 실패: phoneNumber={}, code={}", phoneNumber, verificationCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "인증번호 확인 중 오류가 발생했습니다."
            ));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("SignUpController 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 오류가 발생했습니다.", "message", e.getMessage()));
    }
}