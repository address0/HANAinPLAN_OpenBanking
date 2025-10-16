package com.hanainplan.domain.fund.service;

import com.hanainplan.domain.fund.entity.FundPortfolio;
import com.hanainplan.domain.fund.entity.FundTransaction;
import com.hanainplan.domain.fund.repository.FundPortfolioRepository;
import com.hanainplan.domain.fund.repository.FundTransactionRepository;
import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundPortfolioSyncService {

    private final HanaBankClient hanaBankClient;
    private final FundPortfolioRepository fundPortfolioRepository;
    private final FundTransactionRepository fundTransactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void syncAllUserPortfolios() {
        log.info("====================================================");
        log.info("모든 사용자의 펀드 포트폴리오 동기화 시작");
        log.info("====================================================");

        try {
            List<BankingAccount> allAccounts = accountRepository.findAll();
            Map<String, List<BankingAccount>> accountsByCi = allAccounts.stream()
                    .filter(account -> account.getCustomerCi() != null && !account.getCustomerCi().isEmpty())
                    .collect(Collectors.groupingBy(BankingAccount::getCustomerCi));

            int syncedUserCount = 0;
            int syncedPortfolioCount = 0;
            int syncedTransactionCount = 0;

            for (String customerCi : accountsByCi.keySet()) {
                try {
                    int portfolioCount = syncUserPortfolio(customerCi);
                    int transactionCount = syncUserTransactions(customerCi);

                    if (portfolioCount > 0 || transactionCount > 0) {
                        syncedUserCount++;
                        syncedPortfolioCount += portfolioCount;
                        syncedTransactionCount += transactionCount;
                        log.info("사용자 CI: {} - 포트폴리오 {}건, 거래내역 {}건 동기화 완료", 
                                customerCi, portfolioCount, transactionCount);
                    }
                } catch (Exception e) {
                    log.error("사용자 CI: {} 포트폴리오 동기화 실패", customerCi, e);
                }
            }

            log.info("====================================================");
            log.info("펀드 포트폴리오 동기화 완료");
            log.info("- 동기화된 사용자 수: {}", syncedUserCount);
            log.info("- 동기화된 포트폴리오 수: {}", syncedPortfolioCount);
            log.info("- 동기화된 거래내역 수: {}", syncedTransactionCount);
            log.info("====================================================");

        } catch (Exception e) {
            log.error("펀드 포트폴리오 동기화 중 오류 발생", e);
            throw new RuntimeException("펀드 포트폴리오 동기화 실패", e);
        }
    }

    @Transactional
    public int syncUserPortfolio(String customerCi) {
        log.info("사용자 펀드 포트폴리오 동기화 시작 - CI: {}", customerCi);

        try {
            List<Map<String, Object>> hanaSubscriptions = hanaBankClient.getCustomerSubscriptions(customerCi);

            log.info("하나은행에서 조회된 펀드 가입 내역: {}건", hanaSubscriptions.size());

            int syncedCount = 0;

            Long userId = getUserIdByCustomerCi(customerCi);
            if (userId == null) {
                log.warn("CI에 해당하는 사용자를 찾을 수 없음 - CI: {}", customerCi);
                return 0;
            }

            for (Map<String, Object> hanaSubscription : hanaSubscriptions) {
                try {
                    Long subscriptionId = ((Number) hanaSubscription.get("subscriptionId")).longValue();

                    FundPortfolio portfolio = fundPortfolioRepository
                            .findByCustomerCiAndSubscriptionId(customerCi, subscriptionId)
                            .orElse(null);

                    if (portfolio == null) {
                        portfolio = createNewPortfolio(userId, customerCi, hanaSubscription);
                        log.info("신규 펀드 포트폴리오 생성 - subscriptionId: {}", subscriptionId);
                    } else {
                        updateExistingPortfolio(portfolio, hanaSubscription);
                        log.debug("펀드 포트폴리오 업데이트 - subscriptionId: {}", subscriptionId);
                    }

                    fundPortfolioRepository.save(portfolio);
                    syncedCount++;

                } catch (Exception e) {
                    log.error("포트폴리오 동기화 실패 - 가입 ID: {}", 
                            hanaSubscription.get("subscriptionId"), e);
                }
            }

            log.info("사용자 펀드 포트폴리오 동기화 완료 - CI: {}, {}건 동기화됨", customerCi, syncedCount);
            return syncedCount;

        } catch (Exception e) {
            log.error("사용자 펀드 포트폴리오 동기화 실패 - CI: {}", customerCi, e);
            return 0;
        }
    }

    @Transactional
    public int syncUserTransactions(String customerCi) {
        log.info("사용자 펀드 거래 내역 동기화 시작 - CI: {}", customerCi);

        try {
            List<Map<String, Object>> hanaTransactions = hanaBankClient.getCustomerTransactions(customerCi);

            log.info("하나은행에서 조회된 펀드 거래 내역: {}건", hanaTransactions.size());

            int syncedCount = 0;

            Long userId = getUserIdByCustomerCi(customerCi);
            if (userId == null) {
                log.warn("CI에 해당하는 사용자를 찾을 수 없음 - CI: {}", customerCi);
                return 0;
            }

            for (Map<String, Object> hanaTransaction : hanaTransactions) {
                try {
                    Long hanaTransactionId = ((Number) hanaTransaction.get("transactionId")).longValue();

                    boolean exists = fundTransactionRepository.existsByDescriptionContaining(
                            "HANA_TX_" + hanaTransactionId);

                    if (!exists) {
                        FundTransaction transaction = createNewTransaction(userId, customerCi, hanaTransaction);
                        fundTransactionRepository.save(transaction);
                        syncedCount++;
                        log.debug("신규 펀드 거래 내역 생성 - hanaTransactionId: {}", hanaTransactionId);
                    }

                } catch (Exception e) {
                    log.error("거래 내역 동기화 실패 - 거래 ID: {}", 
                            hanaTransaction.get("transactionId"), e);
                }
            }

            log.info("사용자 펀드 거래 내역 동기화 완료 - CI: {}, {}건 동기화됨", customerCi, syncedCount);
            return syncedCount;

        } catch (Exception e) {
            log.error("사용자 펀드 거래 내역 동기화 실패 - CI: {}", customerCi, e);
            return 0;
        }
    }

    private FundPortfolio createNewPortfolio(Long userId, String customerCi, Map<String, Object> hanaSubscription) {
        String fundType = hanaSubscription.get("fundType") != null ? 
                (String) hanaSubscription.get("fundType") : "기타";
        String riskLevel = hanaSubscription.get("riskLevel") != null ? 
                (String) hanaSubscription.get("riskLevel") : "정보없음";

        return FundPortfolio.builder()
                .userId(userId)
                .customerCi(customerCi)
                .bankCode("HANA")
                .bankName("하나은행")
                .fundCode((String) hanaSubscription.get("fundCode"))
                .childFundCd((String) hanaSubscription.get("childFundCd"))
                .fundName((String) hanaSubscription.get("fundName"))
                .classCode((String) hanaSubscription.get("classCode"))
                .fundType(fundType)
                .riskLevel(riskLevel)
                .purchaseDate(java.time.LocalDate.parse((String) hanaSubscription.get("purchaseDate")))
                .purchaseNav(new java.math.BigDecimal(hanaSubscription.get("purchaseNav").toString()))
                .purchaseAmount(new java.math.BigDecimal(hanaSubscription.get("purchaseAmount").toString()))
                .purchaseFee(new java.math.BigDecimal(hanaSubscription.get("purchaseFee").toString()))
                .purchaseUnits(new java.math.BigDecimal(hanaSubscription.get("purchaseUnits").toString()))
                .currentUnits(new java.math.BigDecimal(hanaSubscription.get("currentUnits").toString()))
                .currentNav(new java.math.BigDecimal(hanaSubscription.get("currentNav").toString()))
                .currentValue(new java.math.BigDecimal(hanaSubscription.get("currentValue").toString()))
                .totalReturn(new java.math.BigDecimal(hanaSubscription.get("totalReturn").toString()))
                .returnRate(new java.math.BigDecimal(hanaSubscription.get("returnRate").toString()))
                .accumulatedFees(new java.math.BigDecimal(hanaSubscription.get("accumulatedFees").toString()))
                .irpAccountNumber((String) hanaSubscription.get("irpAccountNumber"))
                .subscriptionId(((Number) hanaSubscription.get("subscriptionId")).longValue())
                .status((String) hanaSubscription.get("status"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void updateExistingPortfolio(FundPortfolio portfolio, Map<String, Object> hanaSubscription) {
        portfolio.setCurrentUnits(new java.math.BigDecimal(hanaSubscription.get("currentUnits").toString()));
        portfolio.setCurrentNav(new java.math.BigDecimal(hanaSubscription.get("currentNav").toString()));
        portfolio.setCurrentValue(new java.math.BigDecimal(hanaSubscription.get("currentValue").toString()));
        portfolio.setTotalReturn(new java.math.BigDecimal(hanaSubscription.get("totalReturn").toString()));
        portfolio.setReturnRate(new java.math.BigDecimal(hanaSubscription.get("returnRate").toString()));
        portfolio.setAccumulatedFees(new java.math.BigDecimal(hanaSubscription.get("accumulatedFees").toString()));
        portfolio.setStatus((String) hanaSubscription.get("status"));
        portfolio.setUpdatedAt(LocalDateTime.now());
    }

    private FundTransaction createNewTransaction(Long userId, String customerCi, Map<String, Object> hanaTransaction) {
        Long hanaTransactionId = ((Number) hanaTransaction.get("transactionId")).longValue();
        Long subscriptionId = ((Number) hanaTransaction.get("subscriptionId")).longValue();
        
        // subscriptionId로 FundPortfolio를 찾아서 실제 portfolioId를 가져옴
        FundPortfolio portfolio = fundPortfolioRepository
                .findByCustomerCiAndSubscriptionId(customerCi, subscriptionId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다 - subscriptionId: " + subscriptionId));

        return FundTransaction.builder()
                .portfolioId(portfolio.getPortfolioId()) // 실제 portfolioId 사용
                .userId(userId)
                .fundCode((String) hanaTransaction.get("childFundCd"))
                .transactionType((String) hanaTransaction.get("transactionType"))
                .transactionDate(java.time.LocalDateTime.of(
                        java.time.LocalDate.parse(hanaTransaction.get("transactionDate").toString()),
                        java.time.LocalTime.MIN))
                .settlementDate(hanaTransaction.get("settlementDate") != null ? 
                        java.time.LocalDate.parse(hanaTransaction.get("settlementDate").toString()) : null)
                .nav(new java.math.BigDecimal(hanaTransaction.get("nav").toString()))
                .units(new java.math.BigDecimal(hanaTransaction.get("units").toString()))
                .amount(new java.math.BigDecimal(hanaTransaction.get("amount").toString()))
                .fee(new java.math.BigDecimal(hanaTransaction.get("fee").toString()))
                .balanceUnits(new java.math.BigDecimal(hanaTransaction.get("units").toString()))
                .irpAccountNumber((String) hanaTransaction.get("irpAccountNumber"))
                .description("HANA_TX_" + hanaTransactionId + " - " + hanaTransaction.get("note"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Long getUserIdByCustomerCi(String customerCi) {
        List<BankingAccount> accounts = accountRepository.findAll().stream()
                .filter(account -> customerCi.equals(account.getCustomerCi()))
                .toList();

        if (accounts.isEmpty()) {
            return null;
        }

        return accounts.get(0).getUserId();
    }
}