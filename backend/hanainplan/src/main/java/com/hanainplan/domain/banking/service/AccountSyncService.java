package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSyncService {

    private final HanaBankClient hanaBankClient;
    private final AccountRepository accountRepository;

    @Transactional
    public int syncAllUserAccounts() {
        log.info("====================================================");
        log.info("모든 사용자의 계좌 동기화 시작");
        log.info("====================================================");

        try {
            List<BankingAccount> allAccounts = accountRepository.findAll();
            Map<String, List<BankingAccount>> accountsByCi = allAccounts.stream()
                    .filter(account -> account.getCustomerCi() != null && !account.getCustomerCi().isEmpty())
                    .collect(Collectors.groupingBy(BankingAccount::getCustomerCi));

            int syncedUserCount = 0;
            int syncedAccountCount = 0;

            for (String customerCi : accountsByCi.keySet()) {
                try {
                    int accountCount = syncUserAccounts(customerCi);
                    if (accountCount > 0) {
                        syncedUserCount++;
                        syncedAccountCount += accountCount;
                        log.info("사용자 CI: {} - {}개 계좌 동기화 완료", customerCi, accountCount);
                    }
                } catch (Exception e) {
                    log.error("사용자 CI: {} 계좌 동기화 실패", customerCi, e);
                }
            }

            log.info("====================================================");
            log.info("계좌 동기화 완료");
            log.info("- 동기화된 사용자 수: {}", syncedUserCount);
            log.info("- 동기화된 계좌 수: {}", syncedAccountCount);
            log.info("====================================================");

            return syncedUserCount;

        } catch (Exception e) {
            log.error("계좌 동기화 중 오류 발생", e);
            return 0;
        }
    }

    @Transactional
    public int syncUserAccountsByUserId(Long userId) {
        log.info("사용자 계좌 동기화 시작 - 사용자 ID: {}", userId);

        try {
            List<BankingAccount> userAccounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);

            if (userAccounts.isEmpty()) {
                log.warn("사용자의 계좌가 없음 - 사용자 ID: {}", userId);
                return 0;
            }

            String customerCi = userAccounts.get(0).getCustomerCi();

            if (customerCi == null || customerCi.isEmpty()) {
                log.warn("사용자 계좌에 CI 정보가 없음 - 사용자 ID: {}", userId);
                return 0;
            }

            return syncUserAccounts(customerCi);

        } catch (Exception e) {
            log.error("사용자 계좌 동기화 실패 - 사용자 ID: {}", userId, e);
            return 0;
        }
    }

    @Transactional
    public int syncUserAccounts(String customerCi) {
        log.info("사용자 계좌 동기화 시작 - CI: {}", customerCi);

        try {
            Map<String, Object> response = hanaBankClient.getCustomerAccountsByCi(customerCi);

            Boolean exists = response != null ? (Boolean) response.get("exists") : null;
            if (response == null || exists == null || !exists) {
                log.warn("하나은행에 고객 정보가 없음 - CI: {}", customerCi);
                return 0;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hanaAccounts = (List<Map<String, Object>>) response.get("accounts");

            if (hanaAccounts == null || hanaAccounts.isEmpty()) {
                log.info("동기화할 계좌가 없음 - CI: {}", customerCi);
                return 0;
            }

            log.info("하나은행에서 조회된 계좌: {}개", hanaAccounts.size());

            int syncedCount = 0;

            for (Map<String, Object> hanaAccount : hanaAccounts) {
                try {
                    String accountNumber = (String) hanaAccount.get("accountNumber");

                    BankingAccount account = accountRepository.findByAccountNumber(accountNumber)
                            .orElse(null);

                    if (account == null) {
                        log.warn("하나인플랜에 계좌가 없음 - 계좌번호: {}", accountNumber);
                        continue;
                    }

                    BigDecimal newBalance = new BigDecimal(hanaAccount.get("balance").toString());
                    account.setBalance(newBalance);
                    account.setUpdatedAt(java.time.LocalDateTime.now());

                    accountRepository.save(account);
                    syncedCount++;

                    log.debug("계좌 동기화 완료 - 계좌번호: {}, 잔액: {}원", accountNumber, newBalance);

                } catch (Exception e) {
                    log.error("계좌 동기화 실패 - 계좌번호: {}", 
                            hanaAccount.get("accountNumber"), e);
                }
            }

            log.info("사용자 계좌 동기화 완료 - CI: {}, {}개 계좌 동기화됨", customerCi, syncedCount);
            return syncedCount;

        } catch (Exception e) {
            log.error("사용자 계좌 동기화 실패 - CI: {}", customerCi, e);
            return 0;
        }
    }
}