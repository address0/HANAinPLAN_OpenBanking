package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.*;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.AutoTransfer;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.AutoTransferRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AutoTransferService {

    private final AutoTransferRepository autoTransferRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public AutoTransferResponseDto createAutoTransfer(AutoTransferRequestDto request) {
        log.info("자동이체 생성 요청 - 출금 계좌 ID: {}, 입금 계좌 ID: {}, 자동이체명: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getTransferName());

        try {
            BankingAccount fromAccount = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new RuntimeException("출금 계좌를 찾을 수 없습니다: " + request.getFromAccountId()));

            BankingAccount toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("입금 계좌를 찾을 수 없습니다: " + request.getToAccountId()));

            if (fromAccount.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return AutoTransferResponseDto.failure("출금 계좌가 비활성 상태입니다", 
                        "계좌 상태: " + fromAccount.getAccountStatus().getDescription());
            }

            if (toAccount.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return AutoTransferResponseDto.failure("입금 계좌가 비활성 상태입니다", 
                        "계좌 상태: " + toAccount.getAccountStatus().getDescription());
            }

            AutoTransfer autoTransfer = AutoTransfer.builder()
                    .fromAccountId(request.getFromAccountId())
                    .toAccountId(request.getToAccountId())
                    .transferName(request.getTransferName())
                    .amount(request.getAmount())
                    .transferCycle(request.getTransferCycle())
                    .transferDay(request.getTransferDay())
                    .transferWeekday(request.getTransferWeekday())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .transferStatus(AutoTransfer.TransferStatus.ACTIVE)
                    .counterpartAccountNumber(request.getCounterpartAccountNumber())
                    .counterpartName(request.getCounterpartName())
                    .counterpartBankCode(request.getCounterpartBankCode())
                    .description(request.getDescription())
                    .transferCount(0)
                    .maxTransferCount(request.getMaxTransferCount())
                    .failureCount(0)
                    .maxFailureCount(request.getMaxFailureCount())
                    .build();

            autoTransfer.calculateNextTransferDate();

            AutoTransfer savedAutoTransfer = autoTransferRepository.save(autoTransfer);

            log.info("자동이체 생성 완료 - 자동이체 ID: {}, 다음 이체일: {}", 
                    savedAutoTransfer.getAutoTransferId(), savedAutoTransfer.getNextTransferDate());

            return AutoTransferResponseDto.success(
                    "자동이체가 생성되었습니다",
                    savedAutoTransfer.getAutoTransferId(),
                    savedAutoTransfer.getTransferName(),
                    savedAutoTransfer.getTransferStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("자동이체 생성 실패 - 출금 계좌 ID: {}, 입금 계좌 ID: {}, 오류: {}", 
                    request.getFromAccountId(), request.getToAccountId(), e.getMessage());
            return AutoTransferResponseDto.failure("자동이체 생성 중 오류가 발생했습니다", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AutoTransferDto> getAutoTransfers(Long accountId) {
        log.info("자동이체 목록 조회 - 계좌 ID: {}", accountId);

        List<AutoTransfer> autoTransfers = autoTransferRepository.findByAccountId(accountId);
        return autoTransfers.stream()
                .map(AutoTransferDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AutoTransferDto> getAutoTransfer(Long autoTransferId) {
        log.info("자동이체 상세 조회 - 자동이체 ID: {}", autoTransferId);

        return autoTransferRepository.findById(autoTransferId)
                .map(AutoTransferDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<AutoTransferDto> getUserAutoTransfer(Long userId, Long autoTransferId) {
        log.info("사용자 자동이체 조회 - 사용자 ID: {}, 자동이체 ID: {}", userId, autoTransferId);

        return autoTransferRepository.findByAutoTransferIdAndAccountId(autoTransferId, userId)
                .map(AutoTransferDto::fromEntity);
    }

    public AutoTransferResponseDto updateAutoTransfer(Long autoTransferId, AutoTransferRequestDto request) {
        log.info("자동이체 수정 요청 - 자동이체 ID: {}", autoTransferId);

        try {
            AutoTransfer autoTransfer = autoTransferRepository.findById(autoTransferId)
                    .orElseThrow(() -> new RuntimeException("자동이체를 찾을 수 없습니다: " + autoTransferId));

            if (autoTransfer.getTransferStatus() != AutoTransfer.TransferStatus.ACTIVE) {
                return AutoTransferResponseDto.failure("수정할 수 없는 자동이체입니다", 
                        "자동이체 상태: " + autoTransfer.getTransferStatus().getDescription());
            }

            if (request.getTransferName() != null) {
                autoTransfer.setTransferName(request.getTransferName());
            }
            if (request.getAmount() != null) {
                autoTransfer.setAmount(request.getAmount());
            }
            if (request.getTransferCycle() != null) {
                autoTransfer.setTransferCycle(request.getTransferCycle());
            }
            if (request.getTransferDay() != null) {
                autoTransfer.setTransferDay(request.getTransferDay());
            }
            if (request.getTransferWeekday() != null) {
                autoTransfer.setTransferWeekday(request.getTransferWeekday());
            }
            if (request.getEndDate() != null) {
                autoTransfer.setEndDate(request.getEndDate());
            }
            if (request.getDescription() != null) {
                autoTransfer.setDescription(request.getDescription());
            }
            if (request.getMaxTransferCount() != null) {
                autoTransfer.setMaxTransferCount(request.getMaxTransferCount());
            }
            if (request.getMaxFailureCount() != null) {
                autoTransfer.setMaxFailureCount(request.getMaxFailureCount());
            }

            autoTransfer.calculateNextTransferDate();

            AutoTransfer savedAutoTransfer = autoTransferRepository.save(autoTransfer);

            log.info("자동이체 수정 완료 - 자동이체 ID: {}", savedAutoTransfer.getAutoTransferId());

            return AutoTransferResponseDto.success(
                    "자동이체가 수정되었습니다",
                    savedAutoTransfer.getAutoTransferId(),
                    savedAutoTransfer.getTransferName(),
                    savedAutoTransfer.getTransferStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("자동이체 수정 실패 - 자동이체 ID: {}, 오류: {}", autoTransferId, e.getMessage());
            return AutoTransferResponseDto.failure("자동이체 수정 중 오류가 발생했습니다", e.getMessage());
        }
    }

    public AutoTransferResponseDto updateAutoTransferStatus(Long autoTransferId, AutoTransfer.TransferStatus status) {
        log.info("자동이체 상태 변경 요청 - 자동이체 ID: {}, 새 상태: {}", autoTransferId, status);

        try {
            AutoTransfer autoTransfer = autoTransferRepository.findById(autoTransferId)
                    .orElseThrow(() -> new RuntimeException("자동이체를 찾을 수 없습니다: " + autoTransferId));

            autoTransfer.setTransferStatus(status);
            AutoTransfer savedAutoTransfer = autoTransferRepository.save(autoTransfer);

            log.info("자동이체 상태 변경 완료 - 자동이체 ID: {}, 상태: {}", 
                    savedAutoTransfer.getAutoTransferId(), savedAutoTransfer.getTransferStatus());

            return AutoTransferResponseDto.success(
                    "자동이체 상태가 변경되었습니다",
                    savedAutoTransfer.getAutoTransferId(),
                    savedAutoTransfer.getTransferName(),
                    savedAutoTransfer.getTransferStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("자동이체 상태 변경 실패 - 자동이체 ID: {}, 오류: {}", autoTransferId, e.getMessage());
            return AutoTransferResponseDto.failure("자동이체 상태 변경 중 오류가 발생했습니다", e.getMessage());
        }
    }

    public AutoTransferResponseDto deleteAutoTransfer(Long autoTransferId) {
        log.info("자동이체 삭제 요청 - 자동이체 ID: {}", autoTransferId);

        return updateAutoTransferStatus(autoTransferId, AutoTransfer.TransferStatus.CANCELLED);
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void executeAutoTransfers() {
        log.info("자동이체 실행 스케줄러 시작");

        LocalDate today = LocalDate.now();
        List<AutoTransfer> executableTransfers = autoTransferRepository.findExecutableAutoTransfers(today);

        log.info("실행 가능한 자동이체 수: {}", executableTransfers.size());

        for (AutoTransfer autoTransfer : executableTransfers) {
            try {
                executeSingleAutoTransfer(autoTransfer);
            } catch (Exception e) {
                log.error("자동이체 실행 실패 - 자동이체 ID: {}, 오류: {}", 
                        autoTransfer.getAutoTransferId(), e.getMessage());
                autoTransfer.failTransfer();
                autoTransferRepository.save(autoTransfer);
            }
        }

        log.info("자동이체 실행 스케줄러 완료");
    }

    private void executeSingleAutoTransfer(AutoTransfer autoTransfer) {
        log.info("자동이체 실행 - 자동이체 ID: {}, 금액: {}", 
                autoTransfer.getAutoTransferId(), autoTransfer.getAmount());

        try {
            TransferRequestDto transferRequest = TransferRequestDto.builder()
                    .fromAccountId(autoTransfer.getFromAccountId())
                    .toAccountId(autoTransfer.getToAccountId())
                    .amount(autoTransfer.getAmount())
                    .description(autoTransfer.getTransferName() + " 자동이체")
                    .memo(autoTransfer.getDescription())
                    .build();

            TransactionResponseDto response = transactionService.transfer(transferRequest);

            if (response.isSuccess()) {
                autoTransfer.executeTransfer();
                log.info("자동이체 실행 성공 - 자동이체 ID: {}, 거래번호: {}", 
                        autoTransfer.getAutoTransferId(), response.getTransactionNumber());
            } else {
                autoTransfer.failTransfer();
                log.warn("자동이체 실행 실패 - 자동이체 ID: {}, 사유: {}", 
                        autoTransfer.getAutoTransferId(), response.getFailureReason());
            }

            autoTransferRepository.save(autoTransfer);

        } catch (Exception e) {
            log.error("자동이체 실행 중 오류 - 자동이체 ID: {}, 오류: {}", 
                    autoTransfer.getAutoTransferId(), e.getMessage());
            autoTransfer.failTransfer();
            autoTransferRepository.save(autoTransfer);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupExpiredAutoTransfers() {
        log.info("만료된 자동이체 정리 스케줄러 시작");

        LocalDate today = LocalDate.now();
        List<AutoTransfer> expiredTransfers = autoTransferRepository.findExpiredAutoTransfers(today);

        log.info("만료된 자동이체 수: {}", expiredTransfers.size());

        for (AutoTransfer autoTransfer : expiredTransfers) {
            autoTransfer.setTransferStatus(AutoTransfer.TransferStatus.COMPLETED);
            autoTransferRepository.save(autoTransfer);
        }

        log.info("만료된 자동이체 정리 스케줄러 완료");
    }
}