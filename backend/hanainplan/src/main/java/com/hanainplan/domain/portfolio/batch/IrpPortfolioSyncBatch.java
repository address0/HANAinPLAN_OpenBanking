package com.hanainplan.domain.portfolio.batch;

import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.portfolio.entity.IrpHolding;
import com.hanainplan.domain.portfolio.repository.IrpHoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class IrpPortfolioSyncBatch {

    private final IrpAccountRepository irpAccountRepository;
    private final IrpHoldingRepository irpHoldingRepository;
    
    // TODO: 하나은행 API 클라이언트 주입 (실제 구현 시)
    // private final HanaBankApiClient hanaBankApiClient;

    /**
     * 매일 오전 1시에 IRP 포트폴리오 동기화 실행
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void syncIrpPortfolios() {
        log.info("IRP 포트폴리오 동기화 배치 시작");

        try {
            // tb_irp_account 테이블에서 활성화된 모든 IRP 계좌 조회
            List<IrpAccount> activeIrpAccounts = irpAccountRepository.findByAccountStatusOrderByCreatedDateDesc("ACTIVE");
            log.info("활성화된 IRP 계좌 수: {}", activeIrpAccounts.size());

            int successCount = 0;
            int errorCount = 0;

            for (IrpAccount irpAccount : activeIrpAccounts) {
                try {
                    syncIrpAccountPortfolio(irpAccount);
                    successCount++;
                } catch (Exception e) {
                    log.error("IRP 계좌 {} 포트폴리오 동기화 실패: {}", irpAccount.getAccountNumber(), e.getMessage());
                    errorCount++;
                }
            }

            log.info("IRP 포트폴리오 동기화 배치 완료 - 성공: {}, 실패: {}", successCount, errorCount);

        } catch (Exception e) {
            log.error("IRP 포트폴리오 동기화 배치 중 오류 발생", e);
        }
    }

    /**
     * 개별 IRP 계좌의 포트폴리오 동기화
     */
    private void syncIrpAccountPortfolio(IrpAccount irpAccount) {
        log.debug("IRP 계좌 {} 포트폴리오 동기화 시작", irpAccount.getAccountNumber());

        String irpAccountNumber = irpAccount.getAccountNumber();
        Long customerId = irpAccount.getCustomerId();

        // 1. 현금 잔액 동기화
        syncCashBalance(irpAccount);

        // 2. 예금 상품 동기화
        syncDepositProducts(irpAccount);

        // 3. 펀드 보유 동기화
        syncFundHoldings(irpAccount);

        log.debug("IRP 계좌 {} 포트폴리오 동기화 완료", irpAccountNumber);
    }

    /**
     * 현금 잔액 동기화
     */
    private void syncCashBalance(IrpAccount irpAccount) {
        try {
            // TODO: 하나은행 API 호출하여 현금 잔액 조회
            // BigDecimal cashBalance = hanaBankApiClient.getIrpCashBalance(irpAccount.getAccountNumber());
            
            // 임시로 더미 데이터 사용 (실제 구현 시 제거)
            BigDecimal cashBalance = BigDecimal.valueOf(2500000); // 250만원

            // 기존 현금 보유 조회
            Optional<IrpHolding> existingCash = irpHoldingRepository
                .findByCustomerAndAssetTypeAndCode(irpAccount.getCustomerId(), IrpHolding.AssetType.CASH, null);

            if (existingCash.isPresent()) {
                // 기존 현금 보유 업데이트
                IrpHolding cashHolding = existingCash.get();
                cashHolding.updateCashAmount(cashBalance);
                cashHolding.setLastSyncedAt(LocalDateTime.now());
                irpHoldingRepository.save(cashHolding);
            } else {
                // 새로운 현금 보유 생성
                IrpHolding cashHolding = IrpHolding.builder()
                    .customerId(irpAccount.getCustomerId())
                    .irpAccountNumber(irpAccount.getAccountNumber())
                    .assetType(IrpHolding.AssetType.CASH)
                    .assetCode(null)
                    .assetName("현금")
                    .units(null)
                    .purchaseAmount(cashBalance)
                    .currentValue(cashBalance)
                    .totalReturn(BigDecimal.ZERO)
                    .returnRate(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .lastSyncedAt(LocalDateTime.now())
                    .build();
                irpHoldingRepository.save(cashHolding);
            }

            log.debug("고객 ID {} 현금 잔액 동기화 완료: {}", irpAccount.getCustomerId(), cashBalance);

        } catch (Exception e) {
            log.error("고객 ID {} 현금 잔액 동기화 실패: {}", irpAccount.getCustomerId(), e.getMessage());
        }
    }

    /**
     * 예금 상품 동기화
     */
    private void syncDepositProducts(IrpAccount irpAccount) {
        try {
            // TODO: 하나은행 API 호출하여 예금 가입 내역 조회
            // List<DepositProduct> deposits = hanaBankApiClient.getIrpDeposits(irpAccount.getAccountNumber());
            
            // 임시로 더미 데이터 사용 (실제 구현 시 제거)
            // 예금 상품 1: 1년 정기예금 1000만원, 금리 3.5%
            syncDepositProduct(irpAccount, "DEP001", "하나IRP 1년 정기예금", 
                BigDecimal.valueOf(10000000), BigDecimal.valueOf(0.035), 
                LocalDateTime.now().plusYears(1));

            // 예금 상품 2: 2년 정기예금 1000만원, 금리 3.8%
            syncDepositProduct(irpAccount, "DEP002", "하나IRP 2년 정기예금", 
                BigDecimal.valueOf(10000000), BigDecimal.valueOf(0.038), 
                LocalDateTime.now().plusYears(2));

            log.debug("고객 ID {} 예금 상품 동기화 완료", irpAccount.getCustomerId());

        } catch (Exception e) {
            log.error("고객 ID {} 예금 상품 동기화 실패: {}", irpAccount.getCustomerId(), e.getMessage());
        }
    }

    /**
     * 개별 예금 상품 동기화
     */
    private void syncDepositProduct(IrpAccount irpAccount, 
                                  String productCode, String productName, 
                                  BigDecimal amount, BigDecimal interestRate, 
                                  LocalDateTime maturityDate) {
        
        Optional<IrpHolding> existingDeposit = irpHoldingRepository
            .findByCustomerAndAssetTypeAndCode(irpAccount.getCustomerId(), IrpHolding.AssetType.DEPOSIT, productCode);

        if (existingDeposit.isPresent()) {
            // 기존 예금 업데이트
            IrpHolding depositHolding = existingDeposit.get();
            depositHolding.updateDepositAmount(amount, interestRate, maturityDate);
            depositHolding.setLastSyncedAt(LocalDateTime.now());
            irpHoldingRepository.save(depositHolding);
        } else {
            // 새로운 예금 보유 생성
            IrpHolding depositHolding = IrpHolding.builder()
                .customerId(irpAccount.getCustomerId())
                .irpAccountNumber(irpAccount.getAccountNumber())
                .assetType(IrpHolding.AssetType.DEPOSIT)
                .assetCode(productCode)
                .assetName(productName)
                .units(null)
                .purchaseAmount(amount)
                .currentValue(amount)
                .totalReturn(BigDecimal.ZERO)
                .returnRate(BigDecimal.ZERO)
                .interestRate(interestRate)
                .maturityDate(maturityDate)
                .status("ACTIVE")
                .lastSyncedAt(LocalDateTime.now())
                .build();
            irpHoldingRepository.save(depositHolding);
        }
    }

    /**
     * 펀드 보유 동기화
     */
    private void syncFundHoldings(IrpAccount irpAccount) {
        try {
            // TODO: 하나은행 API 호출하여 펀드 보유 내역 조회
            // List<FundHolding> fundHoldings = hanaBankApiClient.getIrpFundHoldings(irpAccount.getAccountNumber());
            
            // 임시로 더미 데이터 사용 (실제 구현 시 제거)
            // 펀드 1: 하나글로벌주식펀드 500만원, NAV 10,000원, 500좌
            syncFundHolding(irpAccount, "FUND001", "하나글로벌주식펀드", 
                "KR001", BigDecimal.valueOf(500), BigDecimal.valueOf(10000), 
                BigDecimal.valueOf(10000), BigDecimal.valueOf(5000000));

            // 펀드 2: 하나국내채권펀드 750만원, NAV 15,000원, 500좌
            syncFundHolding(irpAccount, "FUND002", "하나국내채권펀드", 
                "KR002", BigDecimal.valueOf(500), BigDecimal.valueOf(15000), 
                BigDecimal.valueOf(15000), BigDecimal.valueOf(7500000));

            log.debug("고객 ID {} 펀드 보유 동기화 완료", irpAccount.getCustomerId());

        } catch (Exception e) {
            log.error("고객 ID {} 펀드 보유 동기화 실패: {}", irpAccount.getCustomerId(), e.getMessage());
        }
    }

    /**
     * 개별 펀드 보유 동기화
     */
    private void syncFundHolding(IrpAccount irpAccount, 
                               String fundCode, String fundName, String classCode,
                               BigDecimal units, BigDecimal currentNav, 
                               BigDecimal purchaseNav, BigDecimal purchaseAmount) {
        
        Optional<IrpHolding> existingFund = irpHoldingRepository
            .findByCustomerAndAssetTypeAndCode(irpAccount.getCustomerId(), IrpHolding.AssetType.FUND, fundCode);

        if (existingFund.isPresent()) {
            // 기존 펀드 업데이트
            IrpHolding fundHolding = existingFund.get();
            fundHolding.setUnits(units);
            fundHolding.setCurrentNav(currentNav);
            fundHolding.setPurchaseNav(purchaseNav);
            fundHolding.updateValuation(currentNav);
            fundHolding.setLastSyncedAt(LocalDateTime.now());
            irpHoldingRepository.save(fundHolding);
        } else {
            // 새로운 펀드 보유 생성
            IrpHolding fundHolding = IrpHolding.builder()
                .customerId(irpAccount.getCustomerId())
                .irpAccountNumber(irpAccount.getAccountNumber())
                .assetType(IrpHolding.AssetType.FUND)
                .assetCode(fundCode)
                .assetName(fundName)
                .units(units)
                .purchaseAmount(purchaseAmount)
                .purchaseNav(purchaseNav)
                .currentNav(currentNav)
                .status("ACTIVE")
                .lastSyncedAt(LocalDateTime.now())
                .build();
            
            // 평가금액 및 수익률 계산
            fundHolding.updateValuation(currentNav);
            irpHoldingRepository.save(fundHolding);
        }
    }

    /**
     * 수동 동기화 (테스트용)
     */
    public void syncCustomerIrpPortfolioManually(Long customerId) {
        log.info("고객 ID {} IRP 포트폴리오 수동 동기화 시작", customerId);
        
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE");
        if (irpAccountOpt.isEmpty()) {
            log.warn("고객 ID {}의 활성화된 IRP 계좌를 찾을 수 없습니다", customerId);
            return;
        }
        
        syncIrpAccountPortfolio(irpAccountOpt.get());
        
        log.info("고객 ID {} IRP 포트폴리오 수동 동기화 완료", customerId);
    }
}
