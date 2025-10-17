package com.hanainplan.domain.portfolio.service;

import com.hanainplan.domain.banking.entity.DepositPortfolio;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.repository.DepositPortfolioRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.fund.entity.FundPortfolio;
import com.hanainplan.domain.fund.repository.FundPortfolioRepository;
import com.hanainplan.domain.portfolio.dto.IrpPortfolioResponse;
import com.hanainplan.domain.portfolio.entity.IrpHolding;
import com.hanainplan.domain.portfolio.repository.IrpHoldingRepository;
import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IrpPortfolioService {

    private final IrpHoldingRepository irpHoldingRepository;
    private final CustomerRepository customerRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final DepositPortfolioRepository depositPortfolioRepository;
    private final FundPortfolioRepository fundPortfolioRepository;

    /**
     * 고객의 IRP 포트폴리오 조회 (슬리브별 분리)
     */
    public IrpPortfolioResponse getIrpPortfolio(Long customerId) {
        log.info("IRP 포트폴리오 조회 요청 - 고객 ID: {}", customerId);

        // tb_irp_account 테이블에서 고객의 IRP 계좌 조회
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE");
        
        if (irpAccountOpt.isEmpty()) {
            log.warn("고객 ID {}는 활성화된 IRP 계좌가 없습니다", customerId);
            return IrpPortfolioResponse.from(List.of(), customerId, null);
        }

        IrpAccount irpAccount = irpAccountOpt.get();
        log.info("IRP 계좌 정보 - 계좌번호: {}, 상태: {}, 잔액: {}", 
                irpAccount.getAccountNumber(), irpAccount.getAccountStatus(), irpAccount.getCurrentBalance());

        // 실제 데이터 조회
        List<IrpHolding> holdings = getActualPortfolioData(customerId, irpAccount.getAccountNumber());
        
        log.info("고객 ID {} IRP 포트폴리오 조회 완료 - 보유 자산 수: {}", customerId, holdings.size());

        return IrpPortfolioResponse.from(holdings, customerId, irpAccount.getAccountNumber());
    }

    /**
     * 실제 deposit_portfolio와 fund_portfolio 테이블에서 데이터를 가져와서 IrpHolding 형태로 변환
     */
    private List<IrpHolding> getActualPortfolioData(Long customerId, String irpAccountNumber) {
        List<IrpHolding> holdings = new ArrayList<>();

        // 1. 예금 포트폴리오 조회 (userId로 조회)
        List<DepositPortfolio> deposits = depositPortfolioRepository.findByUserIdAndStatus(customerId, "ACTIVE");
        log.info("고객 ID {} 예금 포트폴리오 조회 - {}건", customerId, deposits.size());

        for (DepositPortfolio deposit : deposits) {
            IrpHolding holding = IrpHolding.builder()
                    .customerId(customerId)
                    .irpAccountNumber(irpAccountNumber)
                    .assetType(IrpHolding.AssetType.DEPOSIT)
                    .assetCode(deposit.getProductCode())
                    .assetName(deposit.getProductName())
                    .units(null) // 예금은 좌수 없음
                    .purchaseAmount(deposit.getPrincipalAmount())
                    .currentValue(deposit.getPrincipalAmount()) // 예금은 원금 기준
                    .totalReturn(BigDecimal.ZERO) // 예금 수익은 별도 계산 필요
                    .returnRate(deposit.getInterestRate())
                    .interestRate(deposit.getInterestRate())
                    .maturityDate(deposit.getMaturityDate() != null ? deposit.getMaturityDate().atStartOfDay() : null)
                    .status("ACTIVE")
                    .lastSyncedAt(LocalDateTime.now())
                    .build();
            holdings.add(holding);
        }

        // 2. 펀드 포트폴리오 조회 (irpAccountNumber로 조회)
        List<FundPortfolio> funds = fundPortfolioRepository.findByIrpAccountNumberOrderByCreatedAtDesc(irpAccountNumber);
        log.info("IRP 계좌 {} 펀드 포트폴리오 조회 - {}건", irpAccountNumber, funds.size());

        for (FundPortfolio fund : funds) {
            if (fund.isActive()) { // ACTIVE 또는 PARTIAL_SOLD 상태만 포함
                IrpHolding holding = IrpHolding.builder()
                        .customerId(customerId)
                        .irpAccountNumber(irpAccountNumber)
                        .assetType(IrpHolding.AssetType.FUND)
                        .assetCode(fund.getFundCode())
                        .assetName(fund.getFundName())
                        .units(fund.getCurrentUnits())
                        .purchaseAmount(fund.getPurchaseAmount())
                        .purchaseNav(fund.getPurchaseNav())
                        .currentNav(fund.getCurrentNav())
                        .currentValue(fund.getCurrentValue())
                        .totalReturn(fund.getTotalReturn())
                        .returnRate(fund.getReturnRate())
                        .status("ACTIVE")
                        .lastSyncedAt(LocalDateTime.now())
                        .build();
                holdings.add(holding);
            }
        }

        // 3. 현금 잔액 (IRP 계좌의 currentBalance)
        IrpAccount irpAccount = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE").orElse(null);
        if (irpAccount != null && irpAccount.getCurrentBalance() != null && irpAccount.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            IrpHolding cashHolding = IrpHolding.builder()
                    .customerId(customerId)
                    .irpAccountNumber(irpAccountNumber)
                    .assetType(IrpHolding.AssetType.CASH)
                    .assetCode("CASH_BALANCE")
                    .assetName("현금성 자산")
                    .units(null)
                    .purchaseAmount(irpAccount.getCurrentBalance())
                    .currentValue(irpAccount.getCurrentBalance())
                    .totalReturn(BigDecimal.ZERO)
                    .returnRate(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .lastSyncedAt(LocalDateTime.now())
                    .build();
            holdings.add(cashHolding);
        }

        log.info("고객 ID {} 전체 포트폴리오 구성 - 예금: {}건, 펀드: {}건, 현금: {}건", 
                customerId, deposits.size(), funds.stream().mapToInt(f -> f.isActive() ? 1 : 0).sum(), 
                (irpAccount != null && irpAccount.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) ? 1 : 0);

        return holdings;
    }

    /**
     * 특정 IRP 계좌의 포트폴리오 조회
     */
    public IrpPortfolioResponse getIrpPortfolioByAccount(Long customerId, String irpAccountNumber) {
        log.info("IRP 계좌 포트폴리오 조회 요청 - 고객 ID: {}, 계좌번호: {}", customerId, irpAccountNumber);

        // tb_irp_account 테이블에서 특정 계좌 조회
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByAccountNumber(irpAccountNumber);
        
        if (irpAccountOpt.isEmpty()) {
            log.warn("계좌번호 {}에 해당하는 IRP 계좌를 찾을 수 없습니다", irpAccountNumber);
            return IrpPortfolioResponse.from(List.of(), customerId, null);
        }

        IrpAccount irpAccount = irpAccountOpt.get();
        
        // 계좌 소유자가 맞는지 확인
        if (!irpAccount.getCustomerId().equals(customerId)) {
            log.warn("계좌번호 {}는 고객 ID {}의 계좌가 아닙니다", irpAccountNumber, customerId);
            return IrpPortfolioResponse.from(List.of(), customerId, null);
        }

        // 실제 데이터 조회
        List<IrpHolding> holdings = getActualPortfolioData(customerId, irpAccountNumber);
        
        log.info("고객 ID {} 계좌 {} 포트폴리오 조회 완료 - 보유 자산 수: {}", customerId, irpAccountNumber, holdings.size());

        return IrpPortfolioResponse.from(holdings, customerId, irpAccountNumber);
    }

    /**
     * 고객의 총 IRP 자산 가치 조회
     */
    public Double getTotalIrpValue(Long customerId) {
        log.debug("고객 ID {} 총 IRP 자산 가치 조회", customerId);

        // IRP 계좌 조회
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE");
        if (irpAccountOpt.isEmpty()) {
            return 0.0;
        }

        String irpAccountNumber = irpAccountOpt.get().getAccountNumber();

        // 예금 총액
        BigDecimal depositTotal = depositPortfolioRepository.findByUserIdAndStatus(customerId, "ACTIVE")
                .stream()
                .map(DepositPortfolio::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 펀드 총액
        BigDecimal fundTotal = fundPortfolioRepository.findByIrpAccountNumberOrderByCreatedAtDesc(irpAccountNumber)
                .stream()
                .filter(FundPortfolio::isActive)
                .map(FundPortfolio::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 현금 잔액
        BigDecimal cashBalance = irpAccountOpt.get().getCurrentBalance() != null ? 
                irpAccountOpt.get().getCurrentBalance() : BigDecimal.ZERO;

        return depositTotal.add(fundTotal).add(cashBalance).doubleValue();
    }

    /**
     * 고객의 슬리브별 자산 가치 조회
     */
    public Double getAssetValueByType(Long customerId, IrpHolding.AssetType assetType) {
        log.debug("고객 ID {} {} 슬리브 자산 가치 조회", customerId, assetType);

        switch (assetType) {
            case CASH:
                return getCashBalance(customerId);
            case DEPOSIT:
                return getDepositTotal(customerId);
            case FUND:
                return getFundTotal(customerId);
            default:
                return 0.0;
        }
    }

    /**
     * 고객의 현금 잔액 조회
     */
    public Double getCashBalance(Long customerId) {
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE");
        if (irpAccountOpt.isEmpty()) {
            return 0.0;
        }
        
        BigDecimal cashBalance = irpAccountOpt.get().getCurrentBalance() != null ? 
                irpAccountOpt.get().getCurrentBalance() : BigDecimal.ZERO;
        
        return cashBalance.doubleValue();
    }

    /**
     * 고객의 예금 총액 조회
     */
    public Double getDepositTotal(Long customerId) {
        BigDecimal depositTotal = depositPortfolioRepository.findByUserIdAndStatus(customerId, "ACTIVE")
                .stream()
                .map(DepositPortfolio::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return depositTotal.doubleValue();
    }

    /**
     * 고객의 펀드 총액 조회
     */
    public Double getFundTotal(Long customerId) {
        // IRP 계좌 조회
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE");
        if (irpAccountOpt.isEmpty()) {
            return 0.0;
        }

        String irpAccountNumber = irpAccountOpt.get().getAccountNumber();

        BigDecimal fundTotal = fundPortfolioRepository.findByIrpAccountNumberOrderByCreatedAtDesc(irpAccountNumber)
                .stream()
                .filter(FundPortfolio::isActive)
                .map(FundPortfolio::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return fundTotal.doubleValue();
    }

    /**
     * 고객의 펀드 비중 계산 (70% 상한 체크용)
     */
    public Double getFundWeight(Long customerId) {
        Double totalValue = getTotalIrpValue(customerId);
        Double fundValue = getFundTotal(customerId);
        
        if (totalValue == null || totalValue == 0.0) {
            return 0.0;
        }
        
        return (fundValue / totalValue) * 100.0;
    }

    /**
     * 펀드 비중이 70% 상한을 초과하는지 확인
     */
    public boolean isFundWeightExceeded(Long customerId) {
        Double fundWeight = getFundWeight(customerId);
        return fundWeight != null && fundWeight > 70.0;
    }

    /**
     * 만기된 예금 조회
     */
    public List<IrpHolding> getMaturedDeposits(Long customerId) {
        log.debug("고객 ID {} 만기된 예금 조회", customerId);
        
        List<DepositPortfolio> maturedDeposits = depositPortfolioRepository.findByUserIdAndStatus(customerId, "ACTIVE")
                .stream()
                .filter(deposit -> deposit.getMaturityDate() != null && 
                                 deposit.getMaturityDate().isBefore(LocalDate.now()))
                .toList();

        // IRP 계좌 조회
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE");
        String irpAccountNumber = irpAccountOpt.map(IrpAccount::getAccountNumber).orElse("");

        return maturedDeposits.stream()
                .map(deposit -> IrpHolding.builder()
                        .customerId(customerId)
                        .irpAccountNumber(irpAccountNumber)
                        .assetType(IrpHolding.AssetType.DEPOSIT)
                        .assetCode(deposit.getProductCode())
                        .assetName(deposit.getProductName())
                        .purchaseAmount(deposit.getPrincipalAmount())
                        .currentValue(deposit.getPrincipalAmount())
                        .maturityDate(deposit.getMaturityDate() != null ? deposit.getMaturityDate().atStartOfDay() : null)
                        .status("MATURED")
                        .build())
                .toList();
    }

    /**
     * 동기화가 필요한 보유 자산 조회 (마지막 동기화가 24시간 이전)
     */
    public List<IrpHolding> getHoldingsNeedingSync() {
        log.debug("동기화가 필요한 보유 자산 조회");
        
        // 현재는 실제 동기화 로직이 없으므로 빈 리스트 반환
        // 향후 실제 동기화 필요 시점을 판단하는 로직 구현
        return new ArrayList<>();
    }

    /**
     * 특정 자산의 보유 정보 조회
     */
    public IrpHolding getHoldingByAssetCode(Long customerId, String assetCode) {
        log.debug("고객 ID {} 자산코드 {} 보유 정보 조회", customerId, assetCode);
        
        // IRP 계좌 조회
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE");
        if (irpAccountOpt.isEmpty()) {
            return null;
        }
        
        String irpAccountNumber = irpAccountOpt.get().getAccountNumber();
        
        // 예금에서 찾기
        Optional<DepositPortfolio> deposit = depositPortfolioRepository.findByUserIdAndStatus(customerId, "ACTIVE")
                .stream()
                .filter(d -> assetCode.equals(d.getProductCode()))
                .findFirst();
        
        if (deposit.isPresent()) {
            DepositPortfolio dp = deposit.get();
            return IrpHolding.builder()
                    .customerId(customerId)
                    .irpAccountNumber(irpAccountNumber)
                    .assetType(IrpHolding.AssetType.DEPOSIT)
                    .assetCode(dp.getProductCode())
                    .assetName(dp.getProductName())
                    .purchaseAmount(dp.getPrincipalAmount())
                    .currentValue(dp.getPrincipalAmount())
                    .status("ACTIVE")
                    .build();
        }
        
        // 펀드에서 찾기
        Optional<FundPortfolio> fund = fundPortfolioRepository.findByIrpAccountNumberOrderByCreatedAtDesc(irpAccountNumber)
                .stream()
                .filter(f -> f.isActive() && assetCode.equals(f.getFundCode()))
                .findFirst();
        
        if (fund.isPresent()) {
            FundPortfolio fp = fund.get();
            return IrpHolding.builder()
                    .customerId(customerId)
                    .irpAccountNumber(irpAccountNumber)
                    .assetType(IrpHolding.AssetType.FUND)
                    .assetCode(fp.getFundCode())
                    .assetName(fp.getFundName())
                    .units(fp.getCurrentUnits())
                    .purchaseAmount(fp.getPurchaseAmount())
                    .currentValue(fp.getCurrentValue())
                    .status("ACTIVE")
                    .build();
        }
        
        return null;
    }

    /**
     * 고객의 포트폴리오 요약 정보 조회
     */
    public PortfolioSummary getPortfolioSummary(Long customerId) {
        log.debug("고객 ID {} 포트폴리오 요약 조회", customerId);

        Double totalValue = getTotalIrpValue(customerId);
        Double cashValue = getCashBalance(customerId);
        Double depositValue = getDepositTotal(customerId);
        Double fundValue = getFundTotal(customerId);
        Double fundWeight = getFundWeight(customerId);

        return PortfolioSummary.builder()
                .customerId(customerId)
                .totalValue(totalValue)
                .cashValue(cashValue)
                .depositValue(depositValue)
                .fundValue(fundValue)
                .fundWeight(fundWeight)
                .isFundWeightExceeded(isFundWeightExceeded(customerId))
                .maturedDepositsCount(getMaturedDeposits(customerId).size())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioSummary {
        private Long customerId;
        private Double totalValue;
        private Double cashValue;
        private Double depositValue;
        private Double fundValue;
        private Double fundWeight;
        private Boolean isFundWeightExceeded;
        private Integer maturedDepositsCount;
    }
}
