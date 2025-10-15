package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.*;
import com.hanainplan.domain.banking.entity.AutoTransfer;
import com.hanainplan.domain.banking.service.AutoTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/banking/auto-transfers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "자동이체 관리", description = "자동이체 생성, 조회, 수정, 삭제 API")
public class AutoTransferController {

    private final AutoTransferService autoTransferService;

    @PostMapping
    @Operation(summary = "자동이체 생성", description = "새로운 자동이체를 생성합니다")
    public ResponseEntity<AutoTransferResponseDto> createAutoTransfer(
            @Parameter(description = "자동이체 생성 요청") @Valid @RequestBody AutoTransferRequestDto request) {

        log.info("자동이체 생성 API 호출 - 출금 계좌 ID: {}, 입금 계좌 ID: {}, 자동이체명: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getTransferName());

        AutoTransferResponseDto response = autoTransferService.createAutoTransfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "계좌 자동이체 목록 조회", description = "특정 계좌의 자동이체 목록을 조회합니다")
    public ResponseEntity<List<AutoTransferDto>> getAutoTransfers(
            @Parameter(description = "계좌 ID") @PathVariable Long accountId) {

        log.info("계좌 자동이체 목록 조회 API 호출 - 계좌 ID: {}", accountId);

        List<AutoTransferDto> autoTransfers = autoTransferService.getAutoTransfers(accountId);
        return ResponseEntity.ok(autoTransfers);
    }

    @GetMapping("/{autoTransferId}")
    @Operation(summary = "자동이체 상세 조회", description = "자동이체 ID로 상세 정보를 조회합니다")
    public ResponseEntity<AutoTransferDto> getAutoTransfer(
            @Parameter(description = "자동이체 ID") @PathVariable Long autoTransferId) {

        log.info("자동이체 상세 조회 API 호출 - 자동이체 ID: {}", autoTransferId);

        Optional<AutoTransferDto> autoTransfer = autoTransferService.getAutoTransfer(autoTransferId);
        return autoTransfer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/auto-transfer/{autoTransferId}")
    @Operation(summary = "사용자 자동이체 조회", description = "사용자 권한으로 자동이체 정보를 조회합니다")
    public ResponseEntity<AutoTransferDto> getUserAutoTransfer(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "자동이체 ID") @PathVariable Long autoTransferId) {

        log.info("사용자 자동이체 조회 API 호출 - 사용자 ID: {}, 자동이체 ID: {}", userId, autoTransferId);

        Optional<AutoTransferDto> autoTransfer = autoTransferService.getUserAutoTransfer(userId, autoTransferId);
        return autoTransfer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{autoTransferId}")
    @Operation(summary = "자동이체 수정", description = "자동이체 정보를 수정합니다")
    public ResponseEntity<AutoTransferResponseDto> updateAutoTransfer(
            @Parameter(description = "자동이체 ID") @PathVariable Long autoTransferId,
            @Parameter(description = "자동이체 수정 요청") @Valid @RequestBody AutoTransferRequestDto request) {

        log.info("자동이체 수정 API 호출 - 자동이체 ID: {}", autoTransferId);

        AutoTransferResponseDto response = autoTransferService.updateAutoTransfer(autoTransferId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{autoTransferId}/status")
    @Operation(summary = "자동이체 상태 변경", description = "자동이체의 상태를 변경합니다")
    public ResponseEntity<AutoTransferResponseDto> updateAutoTransferStatus(
            @Parameter(description = "자동이체 ID") @PathVariable Long autoTransferId,
            @Parameter(description = "새 상태") @RequestParam AutoTransfer.TransferStatus status) {

        log.info("자동이체 상태 변경 API 호출 - 자동이체 ID: {}, 새 상태: {}", autoTransferId, status);

        AutoTransferResponseDto response = autoTransferService.updateAutoTransferStatus(autoTransferId, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{autoTransferId}")
    @Operation(summary = "자동이체 삭제", description = "자동이체를 삭제(비활성화)합니다")
    public ResponseEntity<AutoTransferResponseDto> deleteAutoTransfer(
            @Parameter(description = "자동이체 ID") @PathVariable Long autoTransferId) {

        log.info("자동이체 삭제 API 호출 - 자동이체 ID: {}", autoTransferId);

        AutoTransferResponseDto response = autoTransferService.deleteAutoTransfer(autoTransferId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{autoTransferId}/execute")
    @Operation(summary = "자동이체 수동 실행", description = "자동이체를 수동으로 실행합니다 (테스트용)")
    public ResponseEntity<AutoTransferResponseDto> executeAutoTransfer(
            @Parameter(description = "자동이체 ID") @PathVariable Long autoTransferId) {

        log.info("자동이체 수동 실행 API 호출 - 자동이체 ID: {}", autoTransferId);

        AutoTransferResponseDto response = AutoTransferResponseDto.success(
                "자동이체 수동 실행이 요청되었습니다",
                autoTransferId,
                "수동 실행",
                "요청됨"
        );

        return ResponseEntity.ok(response);
    }
}