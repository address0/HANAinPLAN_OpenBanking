package com.hanainplan.user.controller;

import com.hanainplan.user.dto.*;
import com.hanainplan.user.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/phone")
@Tag(name = "전화번호 인증", description = "전화번호 인증번호 발송 및 검증 API")
public class PhoneVerificationController {

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/send")
    @Operation(
        summary = "인증번호 발송", 
        description = "전화번호로 인증번호를 발송합니다.",
        operationId = "sendVerificationCode"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "인증번호 발송 성공",
            content = @Content(
                schema = @Schema(implementation = PhoneVerificationResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    value = "{\"success\": true, \"message\": \"인증번호가 발송되었습니다.\", \"verificationCode\": \"123456\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "입력 데이터 오류",
            content = @Content(
                schema = @Schema(implementation = PhoneVerificationResponseDto.class),
                examples = @ExampleObject(
                    name = "오류 예시",
                    value = "{\"success\": false, \"message\": \"전화번호 형식이 올바르지 않습니다.\", \"verificationCode\": null, \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<PhoneVerificationResponseDto> sendVerificationCode(
        @Valid 
        @RequestBody
        @io.swagger.v3.oas.annotations.media.Schema(implementation = PhoneVerificationRequestDto.class)
        PhoneVerificationRequestDto request) {
        try {
            String verificationCode = verificationService.sendVerificationCode(request.getPhoneNumber());
            return ResponseEntity.ok(PhoneVerificationResponseDto.success("인증번호가 발송되었습니다.", verificationCode));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(PhoneVerificationResponseDto.error("입력 데이터 오류: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(PhoneVerificationResponseDto.error("서버 오류: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    @Operation(
        summary = "인증번호 검증", 
        description = "전화번호와 인증번호를 검증합니다.",
        operationId = "verifyCode"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "인증번호 검증 성공",
            content = @Content(
                schema = @Schema(implementation = VerifyCodeResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    value = "{\"success\": true, \"message\": \"인증번호가 확인되었습니다.\", \"verified\": true, \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "입력 데이터 오류 또는 인증 실패",
            content = @Content(
                schema = @Schema(implementation = VerifyCodeResponseDto.class),
                examples = @ExampleObject(
                    name = "오류 예시",
                    value = "{\"success\": false, \"message\": \"인증번호가 일치하지 않습니다.\", \"verified\": false, \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<VerifyCodeResponseDto> verifyCode(
        @Valid 
        @RequestBody
        @io.swagger.v3.oas.annotations.media.Schema(implementation = VerifyCodeRequestDto.class)
        VerifyCodeRequestDto request) {
        try {
            boolean verified = verificationService.verifyCode(request.getPhoneNumber(), request.getVerificationCode());

            String message = verified ? "인증번호가 확인되었습니다." : "인증번호가 일치하지 않습니다.";
            return ResponseEntity.ok(VerifyCodeResponseDto.success(verified, message));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(VerifyCodeResponseDto.error("입력 데이터 오류: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(VerifyCodeResponseDto.error("서버 오류: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{phoneNumber}")
    @Operation(
        summary = "전화번호 인증 상태 확인", 
        description = "전화번호의 인증 완료 여부를 확인합니다.",
        operationId = "checkVerificationStatus"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "인증 상태 확인 성공",
            content = @Content(
                schema = @Schema(implementation = VerifyCodeResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    value = "{\"success\": true, \"message\": \"인증 상태를 확인했습니다.\", \"verified\": true, \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<VerifyCodeResponseDto> checkVerificationStatus(@PathVariable String phoneNumber) {
        try {
            boolean verified = verificationService.isPhoneNumberVerified(phoneNumber);

            String message = verified ? "전화번호가 인증되었습니다." : "전화번호가 인증되지 않았습니다.";
            return ResponseEntity.ok(VerifyCodeResponseDto.success(verified, message));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(VerifyCodeResponseDto.error("서버 오류: " + e.getMessage()));
        }
    }
}