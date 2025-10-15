package com.hanainplan.hana.user.controller;

import com.hanainplan.hana.user.dto.IrpAccountRequest;
import com.hanainplan.hana.user.dto.IrpAccountResponse;
import com.hanainplan.hana.user.dto.IrpDepositRequest;
import com.hanainplan.hana.user.dto.IrpDepositResponse;
import com.hanainplan.hana.user.entity.IrpAccount;
import com.hanainplan.hana.user.service.IrpAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/irp")
@RequiredArgsConstructor
@Tag(name = "IRP 계좌 관리", description = "하나은행 IRP 계좌 개설 및 관리 API")
public class IrpAccountController {

    private final IrpAccountService irpAccountService;

    @PostMapping("/open")
    @Operation(summary = "IRP 계좌 개설", description = "새로운 IRP 계좌를 개설합니다")
    public ResponseEntity<?> openIrpAccount(
            @Valid @RequestBody IrpAccountRequest request) {
        try {
            log.info("IRP 계좌 개설 요청 - 사용자 CI: {}", request.getCustomerCi());

            IrpAccountResponse response = irpAccountService.openIrpAccount(request);

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/account/{customerCi}")
    @Operation(summary = "IRP 계좌 조회", description = "사용자 CI로 IRP 계좌 정보를 조회합니다")
    public ResponseEntity<?> getIrpAccount(
            @Parameter(description = "사용자 CI") @PathVariable String customerCi) {
        try {
            Optional<IrpAccount> accountOpt = irpAccountService.getIrpAccountByCustomerCi(customerCi);

            if (accountOpt.isPresent()) {
                return ResponseEntity.ok(accountOpt.get());
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "IRP 계좌를 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("IRP 계좌 조회 중 오류 발생 - 사용자 CI: {}", customerCi, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/check/{customerCi}")
    @Operation(summary = "IRP 계좌 보유 여부 확인", description = "사용자가 IRP 계좌를 보유하고 있는지 확인합니다")
    public ResponseEntity<?> checkIrpAccountStatus(
            @Parameter(description = "사용자 CI") @PathVariable String customerCi) {
        try {
            boolean hasAccount = irpAccountService.hasExistingIrpAccount(customerCi);

            Map<String, Object> response = new HashMap<>();
            response.put("customerCi", customerCi);
            response.put("hasIrpAccount", hasAccount);
            response.put("message", hasAccount ? "IRP 계좌를 보유하고 있습니다" : "IRP 계좌를 보유하고 있지 않습니다");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("IRP 계좌 보유 여부 확인 중 오류 발생 - 사용자 CI: {}", customerCi, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 보유 여부 확인에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/sync/{customerCi}")
    @Operation(summary = "HANAinPLAN 동기화", description = "HANAinPLAN과 IRP 계좌 정보를 동기화합니다")
    public ResponseEntity<?> syncWithHanaInPlan(
            @Parameter(description = "사용자 CI") @PathVariable String customerCi) {
        try {
            boolean syncResult = irpAccountService.syncWithHanaInPlan(customerCi);

            Map<String, Object> response = new HashMap<>();
            response.put("customerCi", customerCi);
            response.put("syncSuccess", syncResult);
            response.put("message", syncResult ? "동기화가 완료되었습니다" : "동기화에 실패했습니다");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("HANAinPLAN 동기화 중 오류 발생 - 사용자 CI: {}", customerCi, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "동기화에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/accounts/changed")
    @Operation(summary = "변경된 IRP 계좌 조회", description = "특정 시간 이후 변경된 IRP 계좌 목록을 조회합니다")
    public ResponseEntity<List<IrpAccount>> getChangedIrpAccounts(
            @Parameter(description = "기준 시간 (ISO 형식)") @RequestParam(required = false) String since) {
        try {
            List<IrpAccount> changedAccounts;

            if (since != null) {
                LocalDateTime sinceDateTime = LocalDateTime.parse(since);
                changedAccounts = irpAccountService.getChangedIrpAccounts(sinceDateTime);
            } else {
                changedAccounts = irpAccountService.getAllIrpAccounts();
            }

            return ResponseEntity.ok(changedAccounts);

        } catch (Exception e) {
            log.error("변경된 IRP 계좌 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/close/{accountNumber}")
    @Operation(summary = "IRP 계좌 해지", description = "IRP 계좌를 해지합니다")
    public ResponseEntity<?> closeIrpAccount(
            @Parameter(description = "계좌번호") @PathVariable String accountNumber) {
        try {
            log.info("IRP 계좌 해지 요청 - 계좌번호: {}", accountNumber);

            boolean closeResult = irpAccountService.closeIrpAccount(accountNumber);

            if (closeResult) {
                Map<String, String> response = new HashMap<>();
                response.put("accountNumber", accountNumber);
                response.put("message", "IRP 계좌가 성공적으로 해지되었습니다");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("accountNumber", accountNumber);
                response.put("message", "IRP 계좌 해지에 실패했습니다");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("IRP 계좌 해지 중 오류 발생 - 계좌번호: {}", accountNumber, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 해지에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/account/number/{accountNumber}")
    @Operation(summary = "IRP 계좌 정보 조회 (계좌번호)", description = "계좌번호로 IRP 계좌 정보를 조회합니다")
    public ResponseEntity<?> getIrpAccountByNumber(
            @Parameter(description = "계좌번호") @PathVariable String accountNumber) {
        try {
            log.info("IRP 계좌 정보 조회 요청 - 계좌번호: {}", accountNumber);

            Optional<IrpAccount> accountOpt = irpAccountService.getIrpAccountByAccountNumber(accountNumber);

            if (accountOpt.isPresent()) {
                IrpAccount account = accountOpt.get();

                Map<String, Object> response = new HashMap<>();
                response.put("accountNumber", account.getAccountNumber());
                response.put("accountStatus", account.getAccountStatus());
                response.put("currentBalance", account.getCurrentBalance());
                response.put("totalContribution", account.getTotalContribution());
                response.put("totalReturn", account.getTotalReturn());
                response.put("returnRate", account.getReturnRate());
                response.put("monthlyDeposit", account.getMonthlyDeposit());
                response.put("investmentStyle", account.getInvestmentStyle());
                response.put("openDate", account.getOpenDate());
                response.put("maturityDate", account.getMaturityDate());
                response.put("depositDay", account.getDepositDay());
                response.put("isAutoDeposit", account.getIsAutoDeposit());
                response.put("linkedMainAccount", account.getLinkedMainAccount());
                response.put("lastContributionDate", account.getLastContributionDate());
                response.put("createdAt", account.getCreatedAt());
                response.put("updatedAt", account.getUpdatedAt());

                log.info("IRP 계좌 정보 조회 성공 - 계좌번호: {}, 잔액: {}", 
                        accountNumber, account.getCurrentBalance());

                return ResponseEntity.ok(response);
            } else {
                log.warn("IRP 계좌를 찾을 수 없음 - 계좌번호: {}", accountNumber);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "IRP 계좌를 찾을 수 없습니다");
                errorResponse.put("accountNumber", accountNumber);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("IRP 계좌 정보 조회 실패 - 계좌번호: {}", accountNumber, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 정보 조회에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/deposit")
    @Operation(summary = "IRP 계좌 입금", description = "IRP 계좌에 입금을 처리합니다")
    public ResponseEntity<?> depositToIrpAccount(
            @Valid @RequestBody IrpDepositRequest request) {
        try {
            log.info("IRP 계좌 입금 요청 - 계좌번호: {}, 금액: {}원", request.getAccountNumber(), request.getAmount());

            IrpDepositResponse response = irpAccountService.processIrpDeposit(
                    request.getAccountNumber(),
                    request.getAmount(),
                    request.getDescription()
            );

            if (response.isSuccess()) {
                log.info("IRP 계좌 입금 성공 - 계좌번호: {}, 거래ID: {}", 
                        request.getAccountNumber(), response.getTransactionId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("IRP 계좌 입금 실패 - 계좌번호: {}, 사유: {}", 
                        request.getAccountNumber(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("IRP 계좌 입금 중 오류 발생 - 계좌번호: {}", request.getAccountNumber(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "IRP 계좌 입금에 실패했습니다");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}