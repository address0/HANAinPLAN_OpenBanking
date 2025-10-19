package com.hanainplan.hana.irp.service;

import com.hanainplan.hana.irp.dto.IrpAccountBalanceResponseDto;
import com.hanainplan.hana.irp.dto.IrpDepositHoldingsResponseDto;
import com.hanainplan.hana.irp.dto.IrpFundHoldingsResponseDto;
import com.hanainplan.hana.irp.dto.IrpPortfolioResponseDto;
import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.repository.AccountRepository;
import com.hanainplan.hana.product.entity.ProductSubscription;
import com.hanainplan.hana.product.repository.ProductSubscriptionRepository;
import com.hanainplan.hana.fund.entity.FundSubscription;
import com.hanainplan.hana.fund.repository.FundSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IrpAccountService {

    private final AccountRepository accountRepository;
    private final ProductSubscriptionRepository productSubscriptionRepository;
    private final FundSubscriptionRepository fundSubscriptionRepository;

    /**
     * IRP 계좌 현금 잔액 조회
     */
    public IrpAccountBalanceResponseDto getAccountBalance(String accountNumber) {
        log.info("IRP 계좌 잔액 조회 시작 - 계좌번호: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("IRP 계좌를 찾을 수 없습니다: " + accountNumber));

        // IRP 계좌인지 확인 (계좌 타입이 IRP인지 확인)
        if (!"IRP".equals(account.getAccountType())) {
            throw new IllegalArgumentException("IRP 계좌가 아닙니다: " + accountNumber);
        }

        BigDecimal cashBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

        log.info("IRP 계좌 잔액 조회 완료 - 계좌번호: {}, 잔액: {}원", accountNumber, cashBalance);

        return IrpAccountBalanceResponseDto.builder()
                .accountNumber(accountNumber)
                .cashBalance(cashBalance)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * IRP 예금 보유 내역 조회
     */
    public List<IrpDepositHoldingsResponseDto> getDepositHoldings(String accountNumber) {
        log.info("IRP 예금 보유 내역 조회 시작 - 계좌번호: {}", accountNumber);

        // IRP 계좌의 고객 CI 조회
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("IRP 계좌를 찾을 수 없습니다: " + accountNumber));

        List<ProductSubscription> subscriptions = productSubscriptionRepository
                .findByCustomerCiAndStatus(account.getCustomerCi(), "ACTIVE");

        List<IrpDepositHoldingsResponseDto> holdings = subscriptions.stream()
                .filter(subscription -> isDepositProduct(subscription.getProductCode()))
                .map(this::convertToDepositHolding)
                .collect(Collectors.toList());

        log.info("IRP 예금 보유 내역 조회 완료 - 계좌번호: {}, 예금 상품 수: {}개", 
                accountNumber, holdings.size());

        return holdings;
    }

    /**
     * IRP 펀드 보유 내역 조회
     */
    public List<IrpFundHoldingsResponseDto> getFundHoldings(String accountNumber) {
        log.info("IRP 펀드 보유 내역 조회 시작 - 계좌번호: {}", accountNumber);

        // IRP 계좌의 고객 CI 조회
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("IRP 계좌를 찾을 수 없습니다: " + accountNumber));

        List<FundSubscription> fundSubscriptions = fundSubscriptionRepository
                .findByCustomerCiAndStatusOrderByCreatedAtDesc(account.getCustomerCi(), "ACTIVE");

        List<IrpFundHoldingsResponseDto> holdings = fundSubscriptions.stream()
                .map(this::convertToFundHolding)
                .collect(Collectors.toList());

        log.info("IRP 펀드 보유 내역 조회 완료 - 계좌번호: {}, 펀드 상품 수: {}개", 
                accountNumber, holdings.size());

        return holdings;
    }

    /**
     * IRP 포트폴리오 전체 조회
     */
    public IrpPortfolioResponseDto getPortfolio(String accountNumber) {
        log.info("IRP 포트폴리오 전체 조회 시작 - 계좌번호: {}", accountNumber);

        // 현금 잔액
        IrpAccountBalanceResponseDto balance = getAccountBalance(accountNumber);
        
        // 예금 보유
        List<IrpDepositHoldingsResponseDto> deposits = getDepositHoldings(accountNumber);
        
        // 펀드 보유
        List<IrpFundHoldingsResponseDto> funds = getFundHoldings(accountNumber);

        // 총 자산 계산
        BigDecimal totalValue = balance.getCashBalance()
                .add(deposits.stream()
                        .map(IrpDepositHoldingsResponseDto::getCurrentValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .add(funds.stream()
                        .map(IrpFundHoldingsResponseDto::getCurrentValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        log.info("IRP 포트폴리오 조회 완료 - 계좌번호: {}, 총 자산: {}원", accountNumber, totalValue);

        return IrpPortfolioResponseDto.builder()
                .accountNumber(accountNumber)
                .totalValue(totalValue)
                .cashBalance(balance.getCashBalance())
                .depositHoldings(deposits)
                .fundHoldings(funds)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * 예금 상품인지 확인
     */
    private boolean isDepositProduct(String productCode) {
        // 예금 상품 코드 패턴 확인 (예: DEP로 시작하는 코드)
        return productCode != null && productCode.startsWith("DEP");
    }

    /**
     * ProductSubscription을 IrpDepositHoldingsResponseDto로 변환
     */
    private IrpDepositHoldingsResponseDto convertToDepositHolding(ProductSubscription subscription) {
        // 예금의 현재 가치는 원금 기준 (이자 계산은 별도)
        BigDecimal currentValue = subscription.getContractPrincipal() != null ? 
                subscription.getContractPrincipal() : BigDecimal.ZERO;

        return IrpDepositHoldingsResponseDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .productCode(subscription.getProductCode())
                .productName(getDepositProductName(subscription.getProductCode()))
                .principalAmount(subscription.getContractPrincipal())
                .currentValue(currentValue)
                .interestRate(subscription.getFinalAppliedRate())
                .subscriptionDate(subscription.getSubscriptionDate().atStartOfDay())
                .maturityDate(subscription.getMaturityDate() != null ? 
                    subscription.getMaturityDate().atStartOfDay() : null)
                .status(subscription.getStatus())
                .build();
    }

    /**
     * FundSubscription을 IrpFundHoldingsResponseDto로 변환
     */
    private IrpFundHoldingsResponseDto convertToFundHolding(FundSubscription subscription) {
        BigDecimal currentValue = subscription.getCurrentValue() != null ? 
                subscription.getCurrentValue() : BigDecimal.ZERO;

        return IrpFundHoldingsResponseDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .fundCode(subscription.getChildFundCd())
                .fundName(subscription.getFundName())
                .units(subscription.getCurrentUnits())
                .currentNav(subscription.getCurrentNav())
                .purchaseNav(subscription.getPurchaseNav())
                .currentValue(currentValue)
                .purchaseAmount(subscription.getPurchaseAmount())
                .totalReturn(subscription.getTotalReturn())
                .returnRate(subscription.getReturnRate())
                .subscriptionDate(subscription.getPurchaseDate().atStartOfDay())
                .status(subscription.getStatus())
                .build();
    }

    /**
     * 예금 상품명 조회
     */
    private String getDepositProductName(String productCode) {
        // 실제로는 상품 마스터에서 조회해야 함
        switch (productCode) {
            case "DEP001":
                return "하나IRP 1년 정기예금";
            case "DEP002":
                return "하나IRP 2년 정기예금";
            case "DEP003":
                return "하나IRP 3년 정기예금";
            default:
                return "하나IRP 정기예금";
        }
    }
}
