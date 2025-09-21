package com.hanainplan.samsung.subscription.controller;

import com.hanainplan.samsung.subscription.dto.InsuranceSubscriptionRequestDto;
import com.hanainplan.samsung.subscription.dto.InsuranceSubscriptionResponseDto;
import com.hanainplan.samsung.subscription.service.InsuranceSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/samsung/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Samsung 보험 가입 관리", description = "Samsung 보험사의 보험 가입 CRUD API")
public class InsuranceSubscriptionController {

    private final InsuranceSubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "보험 가입 생성", description = "새로운 보험 가입을 생성합니다.")
    public ResponseEntity<InsuranceSubscriptionResponseDto> createSubscription(@Valid @RequestBody InsuranceSubscriptionRequestDto requestDto) {
        try {
            InsuranceSubscriptionResponseDto response = subscriptionService.createSubscription(requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/policy/{policyNumber}")
    @Operation(summary = "증권번호로 보험 가입 조회", description = "증권번호로 보험 가입을 조회합니다.")
    public ResponseEntity<InsuranceSubscriptionResponseDto> getSubscriptionByPolicyNumber(@PathVariable String policyNumber) {
        try {
            InsuranceSubscriptionResponseDto response = subscriptionService.getSubscriptionByPolicyNumber(policyNumber);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "가입 ID로 보험 가입 조회", description = "가입 ID로 보험 가입을 조회합니다.")
    public ResponseEntity<InsuranceSubscriptionResponseDto> getSubscriptionById(@PathVariable Long subscriptionId) {
        try {
            InsuranceSubscriptionResponseDto response = subscriptionService.getSubscriptionById(subscriptionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "전체 보험 가입 목록 조회", description = "모든 보험 가입 목록을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getAllSubscriptions() {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/customer/{customerCi}")
    @Operation(summary = "고객별 보험 가입 조회", description = "특정 고객의 모든 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getSubscriptionsByCustomer(@PathVariable String customerCi) {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getSubscriptionsByCustomer(customerCi);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/customer/{customerCi}/active")
    @Operation(summary = "고객별 활성 보험 가입 조회", description = "특정 고객의 활성 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getActiveSubscriptionsByCustomer(@PathVariable String customerCi) {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getActiveSubscriptionsByCustomer(customerCi);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/product/{productCode}")
    @Operation(summary = "상품별 보험 가입 조회", description = "특정 상품의 모든 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getSubscriptionsByProduct(@PathVariable String productCode) {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getSubscriptionsByProduct(productCode);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "상태별 보험 가입 조회", description = "특정 상태의 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getSubscriptionsByStatus(@PathVariable String status) {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getSubscriptionsByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/agent/{agentCode}")
    @Operation(summary = "설계사별 보험 가입 조회", description = "특정 설계사의 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getSubscriptionsByAgent(@PathVariable String agentCode) {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getSubscriptionsByAgent(agentCode);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/branch/{branchCode}")
    @Operation(summary = "지점별 보험 가입 조회", description = "특정 지점의 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getSubscriptionsByBranch(@PathVariable String branchCode) {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getSubscriptionsByBranch(branchCode);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    @Operation(summary = "기간별 보험 가입 조회", description = "특정 기간의 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getSubscriptionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getSubscriptionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/expired")
    @Operation(summary = "만료된 보험 가입 조회", description = "만료된 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getExpiredSubscriptions() {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getExpiredSubscriptions();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/pending-medical")
    @Operation(summary = "의료검진 대기 보험 가입 조회", description = "의료검진 대기 중인 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getPendingMedicalExams() {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getPendingMedicalExams();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/auto-renewal")
    @Operation(summary = "자동갱신 보험 가입 조회", description = "자동갱신 설정된 보험 가입을 조회합니다.")
    public ResponseEntity<List<InsuranceSubscriptionResponseDto>> getAutoRenewalSubscriptions() {
        List<InsuranceSubscriptionResponseDto> responses = subscriptionService.getAutoRenewalSubscriptions();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/customer/{customerCi}/stats")
    @Operation(summary = "고객별 보험 가입 통계", description = "고객의 보험 가입 통계를 조회합니다.")
    public ResponseEntity<CustomerSubscriptionStats> getCustomerSubscriptionStats(@PathVariable String customerCi) {
        Long activeCount = subscriptionService.getActiveSubscriptionCountByCustomer(customerCi);
        BigDecimal totalPremium = subscriptionService.getTotalPremiumByCustomer(customerCi);
        
        CustomerSubscriptionStats stats = new CustomerSubscriptionStats();
        stats.setActiveSubscriptionCount(activeCount);
        stats.setTotalPremium(totalPremium);
        
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/policy/{policyNumber}")
    @Operation(summary = "보험 가입 정보 수정", description = "기존 보험 가입 정보를 수정합니다.")
    public ResponseEntity<InsuranceSubscriptionResponseDto> updateSubscription(
            @PathVariable String policyNumber,
            @Valid @RequestBody InsuranceSubscriptionRequestDto requestDto) {
        try {
            InsuranceSubscriptionResponseDto response = subscriptionService.updateSubscription(policyNumber, requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/policy/{policyNumber}/status")
    @Operation(summary = "보험 가입 상태 변경", description = "보험 가입 상태를 변경합니다.")
    public ResponseEntity<InsuranceSubscriptionResponseDto> updateSubscriptionStatus(
            @PathVariable String policyNumber,
            @RequestParam String status) {
        try {
            InsuranceSubscriptionResponseDto response = subscriptionService.updateSubscriptionStatus(policyNumber, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/policy/{policyNumber}/medical-exam")
    @Operation(summary = "의료검진 결과 업데이트", description = "의료검진 결과를 업데이트합니다.")
    public ResponseEntity<InsuranceSubscriptionResponseDto> updateMedicalExamResult(
            @PathVariable String policyNumber,
            @RequestParam String result,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate) {
        try {
            InsuranceSubscriptionResponseDto response = subscriptionService.updateMedicalExamResult(policyNumber, result, examDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/policy/{policyNumber}")
    @Operation(summary = "보험 가입 삭제", description = "보험 가입을 삭제합니다.")
    public ResponseEntity<Void> deleteSubscription(@PathVariable String policyNumber) {
        try {
            subscriptionService.deleteSubscription(policyNumber);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 통계 응답 DTO
    public static class CustomerSubscriptionStats {
        private Long activeSubscriptionCount;
        private BigDecimal totalPremium;

        public Long getActiveSubscriptionCount() {
            return activeSubscriptionCount;
        }

        public void setActiveSubscriptionCount(Long activeSubscriptionCount) {
            this.activeSubscriptionCount = activeSubscriptionCount;
        }

        public BigDecimal getTotalPremium() {
            return totalPremium;
        }

        public void setTotalPremium(BigDecimal totalPremium) {
            this.totalPremium = totalPremium;
        }
    }
}
