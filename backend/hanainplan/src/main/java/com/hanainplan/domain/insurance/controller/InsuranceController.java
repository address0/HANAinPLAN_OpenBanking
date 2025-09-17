package com.hanainplan.domain.insurance.controller;

import com.hanainplan.domain.insurance.dto.*;
import com.hanainplan.domain.insurance.service.InsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insurance")
@RequiredArgsConstructor
@Slf4j
public class InsuranceController {

    private final InsuranceService insuranceService;

    /**
     * 보험 상품 목록 조회
     */
    @GetMapping("/products")
    public ResponseEntity<List<InsuranceProductDto>> getInsuranceProducts(
            @RequestParam(required = false) String category) {
        try {
            List<InsuranceProductDto> products = insuranceService.getInsuranceProducts(category);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting insurance products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 보험 상품 상세 조회
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<InsuranceProductDto> getInsuranceProduct(@PathVariable String productId) {
        try {
            InsuranceProductDto product = insuranceService.getInsuranceProduct(productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error getting insurance product: {}", productId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 보험료 계산
     */
    @PostMapping("/premium/calculate")
    public ResponseEntity<PremiumCalculationResponseDto> calculatePremium(
            @RequestBody PremiumCalculationRequestDto request) {
        try {
            PremiumCalculationResponseDto response = insuranceService.calculatePremium(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating premium", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 보험 가입 신청
     */
    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> submitInsuranceApplication(
            @RequestBody InsuranceApplicationDto application) {
        try {
            String applicationId = insuranceService.submitInsuranceApplication(application);
            String policyNumber = insuranceService.generatePolicyNumber(applicationId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "applicationId", applicationId,
                "policyNumber", policyNumber,
                "message", "보험 가입 신청이 완료되었습니다."
            ));
        } catch (Exception e) {
            log.error("Error submitting insurance application", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "보험 가입 신청에 실패했습니다."
            ));
        }
    }

    /**
     * 보험 가입 신청 조회
     */
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<InsuranceApplicationDto> getInsuranceApplication(
            @PathVariable String applicationId) {
        try {
            InsuranceApplicationDto application = insuranceService.getInsuranceApplication(applicationId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            log.error("Error getting insurance application: {}", applicationId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 개인정보 검증
     */
    @PostMapping("/validate/personal-info")
    public ResponseEntity<Map<String, Object>> validatePersonalInfo(
            @RequestBody PersonalInfoDto personalInfo) {
        try {
            List<String> errors = insuranceService.validatePersonalInfo(personalInfo);
            return ResponseEntity.ok(Map.of(
                "valid", errors.isEmpty(),
                "errors", errors
            ));
        } catch (Exception e) {
            log.error("Error validating personal info", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 주민번호 중복 확인
     */
    @PostMapping("/validate/resident-number")
    public ResponseEntity<Map<String, Object>> checkResidentNumberDuplicate(
            @RequestBody Map<String, String> request) {
        try {
            String residentNumber = request.get("residentNumber");
            boolean duplicate = insuranceService.checkResidentNumberDuplicate(residentNumber);
            return ResponseEntity.ok(Map.of("duplicate", duplicate));
        } catch (Exception e) {
            log.error("Error checking resident number duplicate", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 은행 계좌 검증
     */
    @PostMapping("/validate/bank-account")
    public ResponseEntity<Map<String, Object>> validateBankAccount(
            @RequestBody BankAccountDto bankAccount) {
        try {
            boolean valid = insuranceService.validateBankAccount(bankAccount);
            return ResponseEntity.ok(Map.of(
                "valid", valid,
                "message", valid ? "유효한 계좌입니다." : "유효하지 않은 계좌입니다."
            ));
        } catch (Exception e) {
            log.error("Error validating bank account", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 보험 가입 신청 상태 업데이트
     */
    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable String applicationId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            boolean success = insuranceService.updateApplicationStatus(applicationId, status);
            return ResponseEntity.ok(Map.of("success", success));
        } catch (Exception e) {
            log.error("Error updating application status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 보험 가입 히스토리 조회
     */
    @GetMapping("/applications/user/{userId}")
    public ResponseEntity<List<InsuranceApplicationDto>> getInsuranceHistory(
            @PathVariable String userId) {
        try {
            List<InsuranceApplicationDto> applications = insuranceService.getInsuranceHistory(userId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error getting insurance history for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

