package com.hanainplan.hana.irp.controller;

import com.hanainplan.hana.user.entity.IrpAccount;
import com.hanainplan.hana.user.repository.IrpAccountRepository;
import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hana/irp")
@RequiredArgsConstructor
@Slf4j
public class IrpDepositController {

    private final IrpAccountRepository irpAccountRepository;
    private final TransactionRepository transactionRepository;

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> depositToIrp(@RequestBody IrpDepositRequest request) {
        log.info("하나은행 IRP 계좌 입금 요청 - 계좌번호: {}, 금액: {}원", request.getAccountNumber(), request.getAmount());

        Map<String, Object> response = new HashMap<>();

        try {
            IrpAccount irpAccount = irpAccountRepository.findByAccountNumber(request.getAccountNumber())
                    .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + request.getAccountNumber()));

            BigDecimal ANNUAL_LIMIT = new BigDecimal("18000000");
            BigDecimal currentYearDeposit = irpAccount.getCurrentYearDeposit() != null 
                    ? irpAccount.getCurrentYearDeposit() 
                    : BigDecimal.ZERO;
            BigDecimal newYearDeposit = currentYearDeposit.add(request.getAmount());

            if (newYearDeposit.compareTo(ANNUAL_LIMIT) > 0) {
                log.warn("IRP 연간 한도 초과 - 계좌번호: {}, 현재 납입: {}원, 요청 금액: {}원, 한도: {}원",
                        request.getAccountNumber(), currentYearDeposit, request.getAmount(), ANNUAL_LIMIT);

                response.put("success", false);
                response.put("message", "IRP 연간 납입 한도(1,800만원)를 초과합니다");
                response.put("currentYearDeposit", currentYearDeposit);
                response.put("requestedAmount", request.getAmount());
                response.put("annualLimit", ANNUAL_LIMIT);
                response.put("remainingLimit", ANNUAL_LIMIT.subtract(currentYearDeposit));

                return ResponseEntity.badRequest().body(response);
            }

            BigDecimal newBalance = (irpAccount.getCurrentBalance() != null ? irpAccount.getCurrentBalance() : BigDecimal.ZERO)
                    .add(request.getAmount());
            irpAccount.setCurrentBalance(newBalance);
            irpAccount.setCurrentYearDeposit(newYearDeposit);
            irpAccount.setUpdatedAt(LocalDateTime.now());
            irpAccountRepository.save(irpAccount);

            String transactionId = saveIrpDepositTransaction(irpAccount, request.getAmount(), 
                    newBalance, request.getDescription());

            response.put("success", true);
            response.put("message", "IRP 계좌 입금이 완료되었습니다");
            response.put("transactionId", transactionId);
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());
            response.put("newBalance", newBalance);
            response.put("currentYearDeposit", newYearDeposit);
            response.put("remainingLimit", ANNUAL_LIMIT.subtract(newYearDeposit));

            log.info("하나은행 IRP 계좌 입금 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원, 새 잔액: {}원", 
                    transactionId, request.getAccountNumber(), request.getAmount(), newBalance);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("하나은행 IRP 계좌 입금 실패 - 계좌번호: {}, 오류: {}", request.getAccountNumber(), e.getMessage());

            response.put("success", false);
            response.put("message", "IRP 입금 처리 중 오류가 발생했습니다: " + e.getMessage());
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("하나은행 IRP 계좌 입금 중 예외 발생", e);

            response.put("success", false);
            response.put("message", "IRP 입금 처리 중 시스템 오류가 발생했습니다");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String saveIrpDepositTransaction(IrpAccount irpAccount, BigDecimal amount, 
                                             BigDecimal balanceAfter, String description) {
        try {
            String transactionId = "HANA-IRP-DP-" + System.currentTimeMillis() + "-" + 
                    String.format("%04d", (int)(Math.random() * 10000));

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .accountNumber(irpAccount.getAccountNumber())
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("IRP 입금")
                    .transactionCategory("투자")
                    .transactionStatus("COMPLETED")
                    .transactionDirection("CREDIT")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .branchName("하나은행 본점")
                    .description(description != null ? description : "IRP 계좌 입금")
                    .referenceNumber(transactionId)
                    .build();

            transactionRepository.save(transaction);

            log.info("하나은행 IRP 입금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, irpAccount.getAccountNumber(), amount);

            return transactionId;

        } catch (Exception e) {
            log.error("하나은행 IRP 입금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", 
                    irpAccount.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }

    public static class IrpDepositRequest {
        private String accountNumber;
        private BigDecimal amount;
        private String description;

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}