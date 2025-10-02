package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.IrpAccountDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenRequestDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenResponseDto;
import com.hanainplan.domain.banking.dto.IrpAccountStatusResponseDto;
import com.hanainplan.domain.banking.service.IrpIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IRP 통합 관리 컨트롤러
 * - HANAinPLAN에서 은행별 IRP 데이터를 통합 관리하는 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/irp-integration")
@RequiredArgsConstructor
@Tag(name = "IRP 통합 관리", description = "은행별 IRP 데이터를 HANAinPLAN에서 통합 관리하는 API")
public class IrpIntegrationController {

    private final IrpIntegrationService irpIntegrationService;

    // ===== 계좌 관리 API =====

    @GetMapping("/accounts/customer/{customerId}")
    @Operation(summary = "고객 IRP 계좌 조회 (고객 ID)", description = "특정 고객의 IRP 계좌 정보를 고객 ID로 조회합니다")
    public ResponseEntity<?> getCustomerIrpAccountById(
            @Parameter(description = "고객 ID") @PathVariable Long customerId) {
        try {
            log.info("IRP 계좌 조회 API 호출 - 고객 ID: {}", customerId);
            
            // 고객 ID로 IRP 계좌 조회
            IrpAccountDto irpAccount = irpIntegrationService.getCustomerIrpAccountByCustomerId(customerId);
            
            if (irpAccount != null) {
                log.info("IRP 계좌 조회 성공 - 고객 ID: {}, 계좌번호: {}", customerId, irpAccount.getAccountNumber());
                return ResponseEntity.ok(irpAccount);
            } else {
                log.info("IRP 계좌 없음 - 고객 ID: {}", customerId);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "IRP 계좌가 없습니다");
                response.put("customerId", customerId);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("고객 IRP 계좌 조회 실패 - 고객 ID: {}", customerId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "고객 IRP 계좌 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/accounts/ci/{customerCi}")
    @Operation(summary = "고객 IRP 계좌 조회 (고객 CI)", description = "특정 고객의 모든 은행 IRP 계좌 정보를 CI로 조회합니다")
    public ResponseEntity<?> getCustomerIrpAccountsByCi(
            @Parameter(description = "고객 CI") @PathVariable String customerCi) {
        try {
            List<IrpAccountDto> accounts = irpIntegrationService.getCustomerIrpAccounts(customerCi);

            Map<String, Object> response = new HashMap<>();
            response.put("customerCi", customerCi);
            response.put("accountCount", accounts.size());
            response.put("accounts", accounts);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("고객 IRP 계좌 조회 실패 - 고객 CI: {}", customerCi, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "고객 IRP 계좌 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/accounts/bank/{bankCode}")
    @Operation(summary = "은행 IRP 계좌 조회", description = "특정 은행의 모든 IRP 계좌 정보를 조회합니다")
    public ResponseEntity<?> getBankIrpAccounts(
            @Parameter(description = "은행 코드 (HANA, KOOKMIN, SHINHAN)") @PathVariable String bankCode) {
        try {
            List<IrpAccountDto> accounts = irpIntegrationService.getBankIrpAccounts(bankCode);

            Map<String, Object> response = new HashMap<>();
            response.put("bankCode", bankCode);
            response.put("bankName", getBankName(bankCode));
            response.put("accountCount", accounts.size());
            response.put("accounts", accounts);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("은행 IRP 계좌 조회 실패 - 은행 코드: {}", bankCode, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "은행 IRP 계좌 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/accounts/all")
    @Operation(summary = "전체 IRP 계좌 조회", description = "모든 은행의 IRP 계좌 정보를 조회합니다 (관리자용)")
    public ResponseEntity<?> getAllIrpAccounts() {
        try {
            List<IrpAccountDto> accounts = irpIntegrationService.getAllIrpAccounts();

            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", accounts.size());
            response.put("accounts", accounts);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("전체 IRP 계좌 조회 실패", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "전체 IRP 계좌 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "IRP 계좌 상세 조회", description = "특정 IRP 계좌의 상세 정보를 조회합니다")
    public ResponseEntity<?> getIrpAccountDetail(
            @Parameter(description = "IRP 계좌 ID") @PathVariable Long accountId) {
        try {
            IrpAccountDto account = irpIntegrationService.getIrpAccountDetail(accountId);

            if (account != null) {
                return ResponseEntity.ok(account);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "IRP 계좌를 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("IRP 계좌 상세 조회 실패 - 계좌 ID: {}", accountId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 상세 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/accounts/check/{customerId}")
    @Operation(summary = "IRP 계좌 보유 여부 확인", description = "특정 고객의 IRP 계좌 보유 여부를 확인합니다")
    public ResponseEntity<?> checkIrpAccountStatus(
            @Parameter(description = "고객 ID") @PathVariable Long customerId) {
        try {
            IrpAccountStatusResponseDto response = irpIntegrationService.checkIrpAccountStatusByCustomerId(customerId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("IRP 계좌 보유 여부 확인 실패 - 고객 ID: {}", customerId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 보유 여부 확인에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ===== 계좌 개설 API =====

    @PostMapping("/accounts/open")
    @Operation(summary = "IRP 계좌 개설", description = "새로운 IRP 계좌를 개설합니다")
    public ResponseEntity<?> openIrpAccount(
            @Valid @RequestBody IrpAccountOpenRequestDto request) {
        try {
            log.info("IRP 계좌 개설 요청 - 고객 CI: {}", request.getCustomerCi());

            IrpAccountOpenResponseDto response = irpIntegrationService.openIrpAccount(request);

            if (response.isSuccess()) {
                log.info("IRP 계좌 개설 성공 - 계좌번호: {}", response.getAccountNumber());
                return ResponseEntity.ok(response);
            } else {
                log.warn("IRP 계좌 개설 실패 - 사유: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("IRP 계좌 개설 중 오류 발생", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 개설에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ===== 통계 및 분석 API =====

    @GetMapping("/statistics/accounts")
    @Operation(summary = "IRP 계좌 통계", description = "은행별 IRP 계좌 통계를 조회합니다")
    public ResponseEntity<?> getIrpAccountStatistics() {
        try {
            Map<String, Object> statistics = irpIntegrationService.getIrpAccountStatistics();

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("IRP 계좌 통계 조회 실패", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 통계 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/portfolio/{customerCi}")
    @Operation(summary = "고객 IRP 포트폴리오", description = "특정 고객의 IRP 포트폴리오 요약을 조회합니다")
    public ResponseEntity<?> getCustomerIrpPortfolio(
            @Parameter(description = "고객 CI") @PathVariable String customerCi) {
        try {
            Map<String, Object> portfolio = irpIntegrationService.getCustomerIrpPortfolio(customerCi);

            return ResponseEntity.ok(portfolio);

        } catch (Exception e) {
            log.error("고객 IRP 포트폴리오 조회 실패 - 고객 CI: {}", customerCi, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "고객 IRP 포트폴리오 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/statistics/balance")
    @Operation(summary = "IRP 잔고 통계", description = "은행별 IRP 잔고 합계를 조회합니다")
    public ResponseEntity<?> getTotalIrpBalanceByBank() {
        try {
            Map<String, Object> balanceStats = irpIntegrationService.getTotalIrpBalanceByBank();

            return ResponseEntity.ok(balanceStats);

        } catch (Exception e) {
            log.error("IRP 잔고 통계 조회 실패", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 잔고 통계 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ===== 헬퍼 메소드 =====

    private String getBankName(String bankCode) {
        switch (bankCode.toUpperCase()) {
            case "HANA": return "하나은행";
            case "KOOKMIN": return "국민은행";
            case "SHINHAN": return "신한은행";
            default: return bankCode;
        }
    }
}
