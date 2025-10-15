package com.hanainplan.hana.fund.service;

import com.hanainplan.hana.fund.dto.FundPurchaseRequestDto;
import com.hanainplan.hana.fund.dto.FundPurchaseResponseDto;
import com.hanainplan.hana.fund.entity.*;
import com.hanainplan.hana.fund.repository.FundClassRepository;
import com.hanainplan.hana.fund.repository.FundNavRepository;
import com.hanainplan.hana.fund.repository.FundSubscriptionRepository;
import com.hanainplan.hana.user.entity.IrpAccount;
import com.hanainplan.hana.user.repository.IrpAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FundSubscriptionService {

    private final FundClassRepository fundClassRepository;
    private final FundNavRepository fundNavRepository;
    private final FundSubscriptionRepository fundSubscriptionRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final com.hanainplan.hana.fund.repository.FundTransactionRepository fundTransactionRepository;
    private final com.hanainplan.hana.account.repository.AccountRepository accountRepository;
    private final com.hanainplan.hana.account.repository.TransactionRepository transactionRepository;

    public FundPurchaseResponseDto purchaseFund(FundPurchaseRequestDto request) {
        log.info("펀드 매수 시작 - customerCi: {}, childFundCd: {}, amount: {}원",
                request.getCustomerCi(), request.getChildFundCd(), request.getPurchaseAmount());

        try {
            request.validate();

            FundClass fundClass = fundClassRepository.findByChildFundCdWithDetails(request.getChildFundCd())
                    .orElseThrow(() -> new IllegalArgumentException("펀드 클래스를 찾을 수 없습니다: " + request.getChildFundCd()));

            if (!"ON".equals(fundClass.getSaleStatus())) {
                throw new IllegalArgumentException("현재 판매 중지된 펀드입니다");
            }

            log.info("IRP 계좌 조회 시도 - customerCi: {}, accountStatus: ACTIVE", request.getCustomerCi());

            Optional<IrpAccount> anyAccount = irpAccountRepository.findByCustomerCi(request.getCustomerCi());
            if (anyAccount.isEmpty()) {
                log.error("customerCi '{}' 로 등록된 IRP 계좌가 전혀 없습니다.", request.getCustomerCi());
            } else {
                log.info("customerCi '{}' 로 등록된 IRP 계좌 발견 - 계좌번호: {}, 상태: {}", 
                        request.getCustomerCi(), anyAccount.get().getAccountNumber(), anyAccount.get().getAccountStatus());
            }

            IrpAccount irpAccount = irpAccountRepository.findByCustomerCiAndAccountStatus(
                    request.getCustomerCi(), "ACTIVE")
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("활성화된 IRP 계좌를 찾을 수 없습니다 (customerCi: %s). IRP 계좌를 먼저 개설해주세요.", 
                                    request.getCustomerCi())));

            log.info("IRP 계좌 조회 완료 - 계좌번호: {}, 잔액: {}원", 
                    irpAccount.getAccountNumber(), irpAccount.getCurrentBalance());

            if (irpAccount.getCurrentBalance().compareTo(request.getPurchaseAmount()) < 0) {
                throw new IllegalArgumentException(
                        String.format("IRP 계좌 잔액이 부족합니다. 현재 잔액: %s원, 필요 금액: %s원",
                                irpAccount.getCurrentBalance(), request.getPurchaseAmount()));
            }

            FundRules rules = fundClass.getFundRules();
            if (rules != null) {
                if (rules.getMinInitialAmount() != null &&
                        request.getPurchaseAmount().compareTo(rules.getMinInitialAmount()) < 0) {
                    throw new IllegalArgumentException(
                            String.format("최소 투자금액은 %,d원입니다", rules.getMinInitialAmount().intValue()));
                }

            }

            LocalDate today = LocalDate.now();
            FundNav todayNav = fundNavRepository.findByChildFundCdAndNavDate(request.getChildFundCd(), today)
                    .orElseGet(() -> {
                        log.warn("당일 기준가가 없어 최신 기준가를 사용합니다");
                        return fundNavRepository.findLatestByChildFundCd(request.getChildFundCd())
                                .orElseThrow(() -> new IllegalArgumentException("기준가 정보가 없습니다"));
                    });

            BigDecimal nav = todayNav.getNav();
            log.info("적용 기준가: {} (날짜: {})", nav, todayNav.getNavDate());

            FundFees fees = fundClass.getFundFees();
            BigDecimal purchaseFee = BigDecimal.ZERO;

            if (fees != null && fees.getFrontLoadPct() != null) {
                purchaseFee = request.getPurchaseAmount()
                        .multiply(fees.getFrontLoadPct())
                        .setScale(2, RoundingMode.DOWN);
            } else if (fees != null && fees.getSalesFeeBps() != null) {
                BigDecimal salesFeeRate = fees.getSalesFeePercent();
                purchaseFee = request.getPurchaseAmount()
                        .multiply(salesFeeRate)
                        .setScale(2, RoundingMode.DOWN);
            }

            log.info("매수 수수료: {}원", purchaseFee);

            BigDecimal netAmount = request.getPurchaseAmount().subtract(purchaseFee);
            BigDecimal units = netAmount.divide(nav, 6, RoundingMode.DOWN);

            log.info("매수 좌수: {}좌 (실투자금: {}원)", units, netAmount);

            BigDecimal oldBalance = irpAccount.getCurrentBalance();
            BigDecimal newBalance = oldBalance.subtract(request.getPurchaseAmount());
            irpAccount.setCurrentBalance(newBalance);
            irpAccountRepository.save(irpAccount);

            log.info("하나은행 IRP 전용 테이블 출금 완료 - 계좌번호: {}, 잔액: {}원 -> {}원", 
                    irpAccount.getAccountNumber(), oldBalance, newBalance);

            log.info("하나은행 통합 계좌 테이블 동기화 시도 - 계좌번호: {}", irpAccount.getAccountNumber());
            accountRepository.findByAccountNumber(irpAccount.getAccountNumber())
                    .ifPresentOrElse(
                            account -> {
                                account.setBalance(newBalance);
                                accountRepository.save(account);
                                log.info("✅ 하나은행 통합 계좌 테이블 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원",
                                        irpAccount.getAccountNumber(), newBalance);
                            },
                            () -> log.error("❌ 하나은행 통합 계좌 테이블에서 IRP 계좌를 찾을 수 없습니다 - 계좌번호: {}",
                                    irpAccount.getAccountNumber())
                    );

            Optional<FundSubscription> existingOpt = fundSubscriptionRepository
                    .findActiveSubscription(request.getCustomerCi(), request.getChildFundCd());

            FundSubscription subscription;
            if (existingOpt.isPresent()) {
                subscription = existingOpt.get();
                log.info("기존 가입에 추가 매수 - subscriptionId: {}", subscription.getSubscriptionId());
                subscription.addPurchase(request.getPurchaseAmount(), nav, purchaseFee, units);
            } else {
                subscription = FundSubscription.builder()
                        .customerCi(request.getCustomerCi())
                        .irpAccountNumber(irpAccount.getAccountNumber())
                        .fundCode(fundClass.getFundMaster().getFundCd())
                        .childFundCd(request.getChildFundCd())
                        .fundName(fundClass.getFundMaster().getFundName())
                        .classCode(fundClass.getClassCode())
                        .fundType(fundClass.getFundMaster().getAssetType())
                        .riskLevel(fundClass.getFundMaster().getRiskGrade())
                        .purchaseDate(today)
                        .purchaseNav(nav)
                        .purchaseAmount(request.getPurchaseAmount())
                        .purchaseFee(purchaseFee)
                        .purchaseUnits(units)
                        .currentUnits(units)
                        .accumulatedFees(purchaseFee)
                        .status("ACTIVE")
                        .bankName("하나은행")
                        .bankCode("HANA")
                        .build();
            }

            subscription.updateValuation(nav);

            FundSubscription saved = fundSubscriptionRepository.save(subscription);
            log.info("펀드 매수 완료 - subscriptionId: {}, 보유좌수: {}좌", saved.getSubscriptionId(), saved.getCurrentUnits());

            int settleDays = rules != null && rules.getBuySettleDays() != null ? rules.getBuySettleDays() : 2;
            LocalDate settlementDate = today.plusDays(settleDays);

            com.hanainplan.hana.fund.entity.FundTransaction transaction = 
                    com.hanainplan.hana.fund.entity.FundTransaction.createPurchase(
                        request.getCustomerCi(),
                        saved.getSubscriptionId(),
                        request.getChildFundCd(),
                        fundClass.getFundMaster().getFundName(),
                        fundClass.getClassCode(),
                        today,
                        settlementDate,
                        nav,
                        units,
                        request.getPurchaseAmount(),
                        purchaseFee,
                        irpAccount.getAccountNumber(),
                        oldBalance,
                        newBalance
                    );
            fundTransactionRepository.save(transaction);
            log.info("거래 내역 저장 완료 - transactionId: {}", transaction.getTransactionId());

            com.hanainplan.hana.account.entity.Transaction irpTransaction = createFundPurchaseTransaction(
                irpAccount.getAccountNumber(),
                request.getPurchaseAmount(),
                newBalance,
                fundClass.getFundMaster().getFundName(),
                fundClass.getClassCode()
            );
            transactionRepository.save(irpTransaction);
            log.info("IRP 계좌 거래내역 저장 완료 - transactionId: {}", irpTransaction.getTransactionId());

            return FundPurchaseResponseDto.success(saved, newBalance, settlementDate);

        } catch (IllegalArgumentException e) {
            log.error("펀드 매수 실패 - 유효성 오류: {}", e.getMessage());
            return FundPurchaseResponseDto.failure(e.getMessage());
        } catch (Exception e) {
            log.error("펀드 매수 실패 - 시스템 오류", e);
            return FundPurchaseResponseDto.failure("시스템 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<FundSubscription> getCustomerSubscriptions(String customerCi) {
        log.info("고객 펀드 가입 목록 조회 - customerCi: {}", customerCi);
        return fundSubscriptionRepository.findByCustomerCiOrderByCreatedAtDesc(customerCi);
    }

    @Transactional(readOnly = true)
    public java.util.List<FundSubscription> getActiveSubscriptions(String customerCi) {
        log.info("활성 펀드 가입 목록 조회 - customerCi: {}", customerCi);
        return fundSubscriptionRepository.findActiveSubscriptionsByCustomerCi(customerCi);
    }

    public com.hanainplan.hana.fund.dto.FundRedemptionResponseDto redeemFund(
            com.hanainplan.hana.fund.dto.FundRedemptionRequestDto request) {

        log.info("펀드 매도 시작 - customerCi: {}, subscriptionId: {}", 
                request.getCustomerCi(), request.getSubscriptionId());

        try {
            request.validate();

            FundSubscription subscription = fundSubscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new IllegalArgumentException("펀드 가입 정보를 찾을 수 없습니다"));

            if (!subscription.getCustomerCi().equals(request.getCustomerCi())) {
                throw new IllegalArgumentException("해당 펀드에 대한 권한이 없습니다");
            }

            if (!subscription.isActive()) {
                throw new IllegalArgumentException("이미 매도된 펀드입니다");
            }

            BigDecimal sellUnits;
            if (request.isSellAll()) {
                sellUnits = subscription.getCurrentUnits();
                log.info("전량 매도 - 좌수: {}", sellUnits);
            } else {
                sellUnits = request.getSellUnits();
                if (sellUnits.compareTo(subscription.getCurrentUnits()) > 0) {
                    throw new IllegalArgumentException(
                            String.format("보유 좌수(%s)를 초과하여 매도할 수 없습니다", subscription.getCurrentUnits()));
                }
                log.info("일부 매도 - 좌수: {}/{}", sellUnits, subscription.getCurrentUnits());
            }

            FundClass fundClass = fundClassRepository.findByChildFundCdWithDetails(subscription.getChildFundCd())
                    .orElseThrow(() -> new IllegalArgumentException("펀드 클래스를 찾을 수 없습니다"));

            LocalDate today = LocalDate.now();
            FundNav todayNav = fundNavRepository.findByChildFundCdAndNavDate(subscription.getChildFundCd(), today)
                    .orElseGet(() -> {
                        log.warn("당일 기준가가 없어 최신 기준가를 사용합니다");
                        return fundNavRepository.findLatestByChildFundCd(subscription.getChildFundCd())
                                .orElseThrow(() -> new IllegalArgumentException("기준가 정보가 없습니다"));
                    });

            BigDecimal sellNav = todayNav.getNav();
            log.info("매도 기준가: {} (날짜: {})", sellNav, todayNav.getNavDate());

            BigDecimal sellAmount = sellUnits.multiply(sellNav).setScale(2, RoundingMode.DOWN);
            log.info("매도 금액: {}원 ({}좌 × {})", sellAmount, sellUnits, sellNav);

            BigDecimal redemptionFee = calculateRedemptionFee(
                    subscription, sellAmount, fundClass.getFundRules());
            log.info("환매수수료: {}원", redemptionFee);

            BigDecimal netAmount = sellAmount.subtract(redemptionFee);

            BigDecimal avgPurchasePrice = subscription.getPurchaseAmount()
                    .divide(subscription.getPurchaseUnits(), 4, RoundingMode.HALF_UP);

            BigDecimal costBasis = avgPurchasePrice.multiply(sellUnits);
            BigDecimal profit = sellAmount.subtract(costBasis).subtract(redemptionFee);

            BigDecimal profitRate = profit.divide(costBasis, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            log.info("실현 손익: {}원 (수익률: {}%)", profit, profitRate);

            IrpAccount irpAccount = irpAccountRepository.findByCustomerCiAndAccountStatus(
                    request.getCustomerCi(), "ACTIVE")
                    .orElseThrow(() -> new IllegalArgumentException(
                            "활성화된 IRP 계좌를 찾을 수 없습니다."));

            log.info("IRP 계좌 조회 완료 - 계좌번호: {}", irpAccount.getAccountNumber());

            BigDecimal oldBalance = irpAccount.getCurrentBalance();
            BigDecimal newBalance = oldBalance.add(netAmount);
            irpAccount.setCurrentBalance(newBalance);
            irpAccountRepository.save(irpAccount);

            log.info("하나은행 IRP 전용 테이블 입금 완료 - 계좌번호: {}, 잔액: {}원 -> {}원", 
                    irpAccount.getAccountNumber(), oldBalance, newBalance);

            log.info("하나은행 통합 계좌 테이블 동기화 시도 - 계좌번호: {}", irpAccount.getAccountNumber());
            accountRepository.findByAccountNumber(irpAccount.getAccountNumber())
                    .ifPresentOrElse(
                            account -> {
                                account.setBalance(newBalance);
                                accountRepository.save(account);
                                log.info("✅ 하나은행 통합 계좌 테이블 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원",
                                        irpAccount.getAccountNumber(), newBalance);
                            },
                            () -> log.error("❌ 하나은행 통합 계좌 테이블에서 IRP 계좌를 찾을 수 없습니다 - 계좌번호: {}",
                                    irpAccount.getAccountNumber())
                    );

            subscription.sellUnits(sellUnits);

            subscription.updateValuation(sellNav);

            fundSubscriptionRepository.save(subscription);

            log.info("펀드 매도 완료 - 남은 좌수: {}, 상태: {}", 
                    subscription.getCurrentUnits(), subscription.getStatus());

            FundRules rules = fundClass.getFundRules();
            int settleDays = rules != null && rules.getRedeemSettleDays() != null 
                    ? rules.getRedeemSettleDays() : 3;
            LocalDate settlementDate = today.plusDays(settleDays);

            com.hanainplan.hana.fund.entity.FundTransaction transaction = 
                    com.hanainplan.hana.fund.entity.FundTransaction.createRedemption(
                        request.getCustomerCi(),
                        request.getSubscriptionId(),
                        subscription.getChildFundCd(),
                        subscription.getFundName(),
                        subscription.getClassCode(),
                        today,
                        settlementDate,
                        sellNav,
                        sellUnits,
                        sellAmount,
                        redemptionFee,
                        profit,
                        profitRate,
                        irpAccount.getAccountNumber(),
                        oldBalance,
                        newBalance
                    );
            fundTransactionRepository.save(transaction);
            log.info("거래 내역 저장 완료 - transactionId: {}", transaction.getTransactionId());

            com.hanainplan.hana.account.entity.Transaction irpTransaction = createFundRedemptionTransaction(
                irpAccount.getAccountNumber(),
                netAmount,
                newBalance,
                subscription.getFundName(),
                subscription.getClassCode(),
                profit
            );
            transactionRepository.save(irpTransaction);
            log.info("IRP 계좌 거래내역 저장 완료 - transactionId: {}", irpTransaction.getTransactionId());

            return com.hanainplan.hana.fund.dto.FundRedemptionResponseDto.success(
                    subscription, sellUnits, sellNav, sellAmount, redemptionFee,
                    profit, profitRate, newBalance, settlementDate);

        } catch (IllegalArgumentException e) {
            log.error("펀드 매도 실패 - 유효성 오류: {}", e.getMessage());
            return com.hanainplan.hana.fund.dto.FundRedemptionResponseDto.failure(e.getMessage());
        } catch (Exception e) {
            log.error("펀드 매도 실패 - 시스템 오류", e);
            return com.hanainplan.hana.fund.dto.FundRedemptionResponseDto.failure(
                    "시스템 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private BigDecimal calculateRedemptionFee(
            FundSubscription subscription, 
            BigDecimal sellAmount, 
            FundRules rules) {

        if (rules == null || !rules.hasRedemptionFee()) {
            return BigDecimal.ZERO;
        }

        LocalDate purchaseDate = subscription.getPurchaseDate();
        LocalDate today = LocalDate.now();
        long holdingDays = java.time.temporal.ChronoUnit.DAYS.between(purchaseDate, today);

        log.info("보유 기간: {}일 (매수일: {})", holdingDays, purchaseDate);

        if (holdingDays < rules.getRedemptionFeeDays()) {
            BigDecimal fee = sellAmount.multiply(rules.getRedemptionFeeRate())
                    .setScale(2, RoundingMode.DOWN);
            log.info("환매수수료 적용 - {}일 이내 매도, 수수료율: {}%", 
                    rules.getRedemptionFeeDays(), rules.getRedemptionFeeRate().multiply(BigDecimal.valueOf(100)));
            return fee;
        } else {
            log.info("환매수수료 면제 - {}일 경과", rules.getRedemptionFeeDays());
            return BigDecimal.ZERO;
        }
    }

    private com.hanainplan.hana.account.entity.Transaction createFundPurchaseTransaction(
            String accountNumber,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String fundName,
            String classCode) {

        String transactionId = "HANA-IRP-FP-" + System.currentTimeMillis() + "-" + 
                              String.format("%04d", (int)(Math.random() * 10000));

        return com.hanainplan.hana.account.entity.Transaction.builder()
                .transactionId(transactionId)
                .accountNumber(accountNumber)
                .transactionDatetime(LocalDateTime.now())
                .transactionType("펀드 매수")
                .transactionCategory("투자")
                .transactionDirection("DEBIT")
                .amount(amount)
                .balanceAfter(balanceAfter)
                .branchName("하나은행 본점")
                .description(String.format("펀드 매수 - %s (%s클래스)", fundName, classCode))
                .transactionStatus("COMPLETED")
                .referenceNumber(transactionId)
                .build();
    }

    private com.hanainplan.hana.account.entity.Transaction createFundRedemptionTransaction(
            String accountNumber,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String fundName,
            String classCode,
            BigDecimal profit) {

        String transactionId = "HANA-IRP-FR-" + System.currentTimeMillis() + "-" + 
                              String.format("%04d", (int)(Math.random() * 10000));

        String profitText = profit.compareTo(BigDecimal.ZERO) >= 0 
                ? String.format("(+%,.0f원)", profit) 
                : String.format("(%,.0f원)", profit);

        return com.hanainplan.hana.account.entity.Transaction.builder()
                .transactionId(transactionId)
                .accountNumber(accountNumber)
                .transactionDatetime(LocalDateTime.now())
                .transactionType("펀드 매도")
                .transactionCategory("투자")
                .transactionDirection("CREDIT")
                .amount(amount)
                .balanceAfter(balanceAfter)
                .branchName("하나은행 본점")
                .description(String.format("펀드 매도 - %s (%s클래스) %s", fundName, classCode, profitText))
                .transactionStatus("COMPLETED")
                .referenceNumber(transactionId)
                .build();
    }
}