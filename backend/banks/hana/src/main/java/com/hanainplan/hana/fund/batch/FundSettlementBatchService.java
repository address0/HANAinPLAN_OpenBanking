package com.hanainplan.hana.fund.batch;

import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.repository.AccountRepository;
import com.hanainplan.hana.fund.entity.FundTransaction;
import com.hanainplan.hana.fund.repository.FundTransactionRepository;
import com.hanainplan.hana.user.entity.IrpAccount;
import com.hanainplan.hana.user.repository.IrpAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundSettlementBatchService {

    private final FundTransactionRepository fundTransactionRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final AccountRepository accountRepository;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void processT2Settlements() {
        log.info("========== 펀드 T+2 결제 처리 배치 시작 ==========");

        LocalDate settlementDate = LocalDate.now();

        List<FundTransaction> pendingTransactions = fundTransactionRepository
                .findBySettlementDateAndStatus(settlementDate, "PENDING");

        log.info("T+2 결제 대상 거래: {}건", pendingTransactions.size());

        int successCount = 0;
        int failCount = 0;

        for (FundTransaction tx : pendingTransactions) {
            try {
                processSettlement(tx);
                successCount++;

                log.info("결제 완료 - 거래ID: {}, 유형: {}, 금액: {}원",
                        tx.getTransactionId(), tx.getTransactionType(), tx.getAmount());

            } catch (Exception e) {
                failCount++;
                log.error("결제 실패 - 거래ID: {}, 오류: {}",
                        tx.getTransactionId(), e.getMessage(), e);
            }
        }

        log.info("========== T+2 결제 처리 완료 - 성공: {}건, 실패: {}건 ==========", 
                successCount, failCount);
    }

    private void processSettlement(FundTransaction tx) {
        String irpAccountNumber = tx.getIrpAccountNumber();

        IrpAccount irpAccount = irpAccountRepository.findByAccountNumber(irpAccountNumber)
                .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + irpAccountNumber));

        if ("BUY".equals(tx.getTransactionType())) {
            BigDecimal newBalance = irpAccount.getCurrentBalance().subtract(tx.getAmount());
            
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("결제 실패 - IRP 잔액 부족: " + irpAccount.getCurrentBalance());
            }
            
            irpAccount.setCurrentBalance(newBalance);
            log.info("매수 결제 - IRP 출금: {}원, 새 잔액: {}원", tx.getAmount(), newBalance);

        } else if ("SELL".equals(tx.getTransactionType())) {
            BigDecimal newBalance = irpAccount.getCurrentBalance().add(tx.getAmount());
            irpAccount.setCurrentBalance(newBalance);
            log.info("매도 결제 - IRP 입금: {}원, 새 잔액: {}원", tx.getAmount(), newBalance);
        }

        irpAccountRepository.save(irpAccount);

        tx.setStatus("SETTLED");
        fundTransactionRepository.save(tx);
    }

    @Transactional
    public void processManualSettlement(Long transactionId) {
        log.info("수동 결제 처리 요청 - 거래ID: {}", transactionId);

        FundTransaction tx = fundTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("거래를 찾을 수 없습니다: " + transactionId));

        if (!"PENDING".equals(tx.getStatus())) {
            throw new RuntimeException("대기 중인 거래만 결제할 수 있습니다. 현재 상태: " + tx.getStatus());
        }

        processSettlement(tx);

        log.info("수동 결제 처리 완료 - 거래ID: {}", transactionId);
    }
}

