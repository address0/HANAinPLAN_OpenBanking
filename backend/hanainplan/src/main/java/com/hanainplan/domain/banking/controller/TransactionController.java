package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.*;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.service.TransactionService;
import com.hanainplan.domain.banking.service.TransferIntegrationService;
import com.hanainplan.domain.banking.service.ExternalAccountVerificationService;
import com.hanainplan.domain.banking.service.IrpTransferService;
import com.hanainplan.domain.banking.service.ExternalTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/banking/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "거래 관리", description = "입출금, 이체, 거래내역 조회 API")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransferIntegrationService transferIntegrationService;
    private final ExternalAccountVerificationService verificationService;
    private final IrpTransferService irpTransferService;
    private final ExternalTransferService externalTransferService;

    @PostMapping("/deposit")
    @Operation(summary = "입금 처리", description = "계좌에 입금을 처리합니다")
    public ResponseEntity<TransactionResponseDto> deposit(
            @Parameter(description = "입금 요청 정보") @Valid @RequestBody DepositRequestDto request) {

        log.info("입금 처리 API 호출 - 계좌 ID: {}, 금액: {}", request.getAccountId(), request.getAmount());

        TransactionResponseDto response = transactionService.deposit(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdrawal")
    @Operation(summary = "출금 처리", description = "계좌에서 출금을 처리합니다")
    public ResponseEntity<TransactionResponseDto> withdrawal(
            @Parameter(description = "출금 요청 정보") @Valid @RequestBody WithdrawalRequestDto request) {

        log.info("출금 처리 API 호출 - 계좌 ID: {}, 금액: {}", request.getAccountId(), request.getAmount());

        TransactionResponseDto response = transactionService.withdrawal(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @Operation(summary = "이체 처리", description = "계좌 간 이체를 처리합니다")
    public ResponseEntity<TransactionResponseDto> transfer(
            @Parameter(description = "이체 요청 정보") @Valid @RequestBody TransferRequestDto request) {

        log.info("이체 처리 API 호출 - 출금 계좌 ID: {}, 입금 계좌 ID: {}, 금액: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        TransactionResponseDto response = transactionService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal-transfer")
    @Operation(summary = "계좌 간 송금 (통합)", description = "일반 계좌 ↔ IRP 계좌 간 송금을 처리합니다. 은행 서버 연동 포함.")
    public ResponseEntity<TransactionResponseDto> internalTransfer(
            @Parameter(description = "송금 요청 정보") @Valid @RequestBody InternalTransferRequestDto request) {

        log.info("계좌 간 송금 API 호출 - 출금 계좌 ID: {}, 입금 계좌 ID: {}, 금액: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        TransactionResponseDto response = transferIntegrationService.transferBetweenAccounts(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "거래 내역 조회", description = "계좌의 거래 내역을 조회합니다 (계좌번호 또는 계좌 ID 사용)")
    public ResponseEntity<Page<TransactionDto>> getTransactionHistory(
            @Parameter(description = "계좌번호 (우선 사용)") @RequestParam(required = false) String accountNumber,
            @Parameter(description = "계좌 ID (하위 호환성)") @RequestParam(required = false) Long accountId,
            @Parameter(description = "거래 유형") @RequestParam(required = false) Transaction.TransactionType transactionType,
            @Parameter(description = "거래 카테고리") @RequestParam(required = false) Transaction.TransactionCategory transactionCategory,
            @Parameter(description = "시작 날짜") @RequestParam(required = false) String startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) String endDate,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("거래 내역 조회 API 호출 - 계좌번호: {}, 계좌 ID: {}, 페이지: {}", accountNumber, accountId, page);

        if (accountNumber == null && accountId == null) {
            throw new IllegalArgumentException("계좌번호 또는 계좌 ID 중 하나는 필수입니다");
        }

        TransactionHistoryRequestDto request = TransactionHistoryRequestDto.builder()
                .accountNumber(accountNumber)
                .accountId(accountId)
                .transactionType(transactionType)
                .transactionCategory(transactionCategory)
                .startDate(startDate != null ? LocalDateTime.parse(startDate + "T00:00:00") : null)
                .endDate(endDate != null ? LocalDateTime.parse(endDate + "T23:59:59") : null)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<TransactionDto> transactions = transactionService.getTransactionHistory(request);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "거래 상세 조회", description = "거래 ID로 거래 상세 정보를 조회합니다")
    public ResponseEntity<TransactionDto> getTransaction(
            @Parameter(description = "거래 ID") @PathVariable Long transactionId) {

        log.info("거래 상세 조회 API 호출 - 거래 ID: {}", transactionId);

        Optional<TransactionDto> transaction = transactionService.getTransaction(transactionId);
        return transaction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{transactionNumber}")
    @Operation(summary = "거래번호로 거래 조회", description = "거래번호로 거래 정보를 조회합니다")
    public ResponseEntity<TransactionDto> getTransactionByNumber(
            @Parameter(description = "거래번호") @PathVariable String transactionNumber) {

        log.info("거래번호로 거래 조회 API 호출 - 거래번호: {}", transactionNumber);

        Optional<TransactionDto> transaction = transactionService.getTransactionByNumber(transactionNumber);
        return transaction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/verify-account")
    @Operation(summary = "외부 계좌 검증", description = "계좌번호로 외부 은행 계좌의 존재 여부 및 유형을 확인합니다")
    public ResponseEntity<AccountVerificationResponseDto> verifyAccount(
            @Parameter(description = "계좌 검증 요청") @RequestBody Map<String, String> request) {

        String accountNumber = request.get("accountNumber");
        log.info("외부 계좌 검증 API 호출 - 계좌번호: {}", accountNumber);

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AccountVerificationResponseDto.error("계좌번호는 필수입니다"));
        }

        AccountVerificationResponseDto response = verificationService.verifyExternalAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer-to-irp")
    @Operation(summary = "IRP 계좌로 송금", description = "일반 계좌에서 IRP 계좌로 송금을 처리합니다")
    public ResponseEntity<TransactionResponseDto> transferToIrp(
            @Parameter(description = "IRP 송금 요청 정보") @Valid @RequestBody TransferToIrpRequestDto request) {

        log.info("IRP 계좌 송금 API 호출 - 출금 계좌 ID: {}, IRP 계좌번호: {}, 금액: {}",
                request.getFromAccountId(), request.getToIrpAccountNumber(), request.getAmount());

        TransactionResponseDto response = irpTransferService.transferToIrp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/external-transfer")
    @Operation(summary = "일반 외부 계좌로 송금", description = "일반 계좌에서 다른 은행의 일반 계좌로 송금을 처리합니다")
    public ResponseEntity<TransactionResponseDto> externalTransfer(
            @Parameter(description = "외부 송금 요청 정보") @Valid @RequestBody ExternalTransferRequestDto request) {

        log.info("외부 계좌 송금 API 호출 - 출금 계좌 ID: {}, 수신 계좌번호: {}, 금액: {}",
                request.getFromAccountId(), request.getToAccountNumber(), request.getAmount());

        TransactionResponseDto response = externalTransferService.transferToExternalAccount(request);
        return ResponseEntity.ok(response);
    }
}