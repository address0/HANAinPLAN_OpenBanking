package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.CiVerificationRequestDto;
import com.hanainplan.domain.user.dto.CiVerificationResponseDto;
import com.hanainplan.domain.user.service.CiVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/ci")
@Tag(name = "CI 관리", description = "실명인증 CI 검증 API")
@CrossOrigin(origins = "*")
public class CiController {

    @Autowired
    private CiVerificationService ciVerificationService;

    @PostMapping("/verify")
    @Operation(summary = "CI 검증", description = "입력된 개인정보를 실명인증 서버에서 CI로 변환하여 검증합니다.")
    public ResponseEntity<CiVerificationResponseDto> verifyCi(@Valid @RequestBody CiVerificationRequestDto request) {
        try {
            CiVerificationResponseDto response = ciVerificationService.verifyCi(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}