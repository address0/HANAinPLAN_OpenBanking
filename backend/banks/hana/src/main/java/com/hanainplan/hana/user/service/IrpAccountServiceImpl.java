package com.hanainplan.hana.user.service;

import com.hanainplan.hana.user.entity.IrpAccount;
import com.hanainplan.hana.user.entity.Customer;
import com.hanainplan.hana.user.dto.IrpAccountRequest;
import com.hanainplan.hana.user.dto.IrpAccountResponse;
import com.hanainplan.hana.user.repository.IrpAccountRepository;
import com.hanainplan.hana.user.repository.CustomerRepository;
import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.TransactionRepository;
import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IrpAccountServiceImpl implements IrpAccountService {

    private final IrpAccountRepository irpAccountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public IrpAccountResponse openIrpAccount(IrpAccountRequest request) throws Exception {
        log.info("하나은행 IRP 계좌 개설 시작 - 고객 CI: {}", request.getCustomerCi());

        try {
            ensureCustomerExists(request);

            List<IrpAccount> customerIrpAccounts = irpAccountRepository.findAllByCustomerCiAndBankCodeAndAccountStatus(
                    request.getCustomerCi(), "HANA", "ACTIVE");

            if (!customerIrpAccounts.isEmpty()) {
                return IrpAccountResponse.builder()
                        .success(false)
                        .message("이미 하나은행 IRP 계좌를 보유하고 있습니다.")
                        .accountNumber(customerIrpAccounts.get(0).getAccountNumber())
                        .build();
            }

            if (request.getLinkedMainAccount() == null || request.getLinkedMainAccount().length() < 10) {
                return IrpAccountResponse.builder()
                        .success(false)
                        .message("연결할 주계좌 정보가 올바르지 않습니다.")
                        .build();
            }

            String accountNumber = generateAccountNumber(request.getCustomerCi());

            IrpAccount account = IrpAccount.builder()
                    .customerCi(request.getCustomerCi())
                    .bankCode("HANA")
                    .accountNumber(accountNumber)
                    .accountStatus("ACTIVE")
                    .initialDeposit(request.getInitialDeposit())
                    .monthlyDeposit(request.getMonthlyDeposit() != null ? request.getMonthlyDeposit() : BigDecimal.ZERO)
                    .currentBalance(request.getInitialDeposit())
                    .totalContribution(request.getInitialDeposit())
                    .totalReturn(BigDecimal.ZERO)
                    .returnRate(BigDecimal.ZERO)
                    .investmentStyle(request.getInvestmentStyle())
                    .isAutoDeposit(request.getIsAutoDeposit())
                    .depositDay(request.getDepositDay())
                    .linkedMainAccount(request.getLinkedMainAccount())
                    .productCode("IRP001")
                    .productName("하나은행 개인형퇴직연금")
                    .managementFeeRate(BigDecimal.valueOf(0.5))
                    .trustFeeRate(BigDecimal.valueOf(0.1))
                    .openDate(LocalDate.now())
                    .maturityDate(LocalDate.now().plusYears(55))
                    .lastContributionDate(LocalDate.now())
                    .syncStatus("PENDING")
                    .externalAccountId(accountNumber)
                    .build();

            IrpAccount savedAccount = irpAccountRepository.save(account);

            Account irpGeneralAccount = Account.builder()
                    .accountNumber(accountNumber)
                    .accountType(6)
                    .balance(request.getInitialDeposit())
                    .openingDate(LocalDate.now())
                    .customerCi(request.getCustomerCi())
                    .build();
            accountRepository.save(irpGeneralAccount);

            log.info("IRP 계좌를 일반 계좌 테이블에 등록 완료 - 계좌번호: {}, account_type: 6, 초기잔액: {}원", 
                    accountNumber, request.getInitialDeposit());

            updateCustomerIrpStatus(request.getCustomerCi(), true, accountNumber);

        if (request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            try {
                processWithdrawalFromLinkedAccount(request.getLinkedMainAccount(), request.getInitialDeposit(), 
                        "IRP 계좌(" + accountNumber + ") 초기 입금");

                savedAccount.setCurrentBalance(request.getInitialDeposit());
                savedAccount.setTotalContribution(request.getInitialDeposit());
                irpAccountRepository.save(savedAccount);

                String transactionId = "HANA-IRP-" + System.currentTimeMillis() + "-" +
                        String.format("%04d", (int)(Math.random() * 10000));

                log.info("=== IRP 거래내역 저장 시작 ===");
                log.info("거래ID: {}", transactionId);
                log.info("IRP 계좌번호: {}", accountNumber);
                log.info("IRP 계좌 객체: {}", irpGeneralAccount);
                log.info("금액: {}원", request.getInitialDeposit());

                Transaction transaction = Transaction.builder()
                        .transactionId(transactionId)
                        .account(irpGeneralAccount)
                        .transactionDatetime(LocalDateTime.now())
                        .transactionType("입금")
                        .transactionCategory("IRP 초기 입금")
                        .amount(request.getInitialDeposit())
                        .balanceAfter(request.getInitialDeposit())
                        .description("IRP 계좌 초기 입금")
                        .referenceNumber(accountNumber)
                        .branchName("하나은행 본점")
                        .build();

                log.info("Transaction 객체 생성 완료: {}", transaction);

                Transaction savedTransaction = transactionRepository.save(transaction);

                log.info("=== IRP 거래내역 저장 완료 ===");
                log.info("저장된 거래ID: {}", savedTransaction.getTransactionId());
                log.info("하나은행 IRP 계좌 초기 입금 완료 - 출금계좌: {}, IRP계좌: {}, 금액: {}원",
                        request.getLinkedMainAccount(), accountNumber, request.getInitialDeposit());

            } catch (Exception e) {
                log.error("=== 하나은행 IRP 계좌 초기 입금 실패 ===");
                log.error("출금계좌: {}", request.getLinkedMainAccount());
                log.error("IRP계좌: {}", accountNumber);
                log.error("오류 메시지: {}", e.getMessage());
                log.error("오류 상세:", e);
            }
        }

            log.info("하나은행 IRP 계좌 개설 완료 - 계좌번호: {}, 고객 CI: {}", accountNumber, request.getCustomerCi());

            return IrpAccountResponse.builder()
                    .success(true)
                    .message("IRP 계좌가 성공적으로 개설되었습니다.")
                    .accountNumber(accountNumber)
                    .accountStatus("ACTIVE")
                    .initialDeposit(request.getInitialDeposit())
                    .monthlyDeposit(request.getMonthlyDeposit())
                    .investmentStyle(request.getInvestmentStyle())
                    .openDate(LocalDate.now())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("하나은행 IRP 계좌 개설 실패 - 고객 CI: {}", request.getCustomerCi(), e);
            throw new Exception("IRP 계좌 개설에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public Optional<IrpAccount> getIrpAccountByCustomerCi(String customerCi) {
        return irpAccountRepository.findByCustomerCiAndAccountStatus(customerCi, "ACTIVE");
    }

    @Override
    public Optional<IrpAccount> getIrpAccountByAccountNumber(String accountNumber) {
        return irpAccountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public boolean hasExistingIrpAccount(String customerCi) {
        return irpAccountRepository.findByCustomerCiAndAccountStatus(customerCi, "ACTIVE").isPresent();
    }

    @Override
    public void updateAccountStatus(String accountNumber, String status) {
        Optional<IrpAccount> accountOpt = irpAccountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            IrpAccount account = accountOpt.get();
            account.setAccountStatus(status);
            irpAccountRepository.save(account);
        }
    }

    @Override
    public void processMonthlyDeposit(String accountNumber, BigDecimal amount) {
        Optional<IrpAccount> accountOpt = irpAccountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            IrpAccount account = accountOpt.get();
            account.setCurrentBalance(account.getCurrentBalance().add(BigDecimal.valueOf(amount.longValue())));
            account.setTotalContribution(account.getTotalContribution().add(BigDecimal.valueOf(amount.longValue())));
            account.setLastContributionDate(LocalDate.now());
            irpAccountRepository.save(account);
        }
    }

    @Override
    public boolean closeIrpAccount(String accountNumber) {
        try {
            Optional<IrpAccount> accountOpt = irpAccountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isPresent()) {
                IrpAccount account = accountOpt.get();
                account.setAccountStatus("CLOSED");
                account.setClosedAt(LocalDateTime.now());
                irpAccountRepository.save(account);

                updateCustomerIrpStatus(account.getCustomerCi(), false, null);

                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("IRP 계좌 해지 실패 - 계좌번호: {}", accountNumber, e);
            return false;
        }
    }

    @Override
    public boolean syncWithHanaInPlan(String customerCi) {
        try {
            log.info("HANAinPLAN과 IRP 계좌 정보 동기화 - 고객 CI: {}", customerCi);

            Optional<IrpAccount> accountOpt = getIrpAccountByCustomerCi(customerCi);
            if (accountOpt.isPresent()) {
                log.info("IRP 계좌 동기화 완료 - 계좌번호: {}", accountOpt.get().getAccountNumber());
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("IRP 계좌 동기화 실패 - 고객 CI: {}", customerCi, e);
            return false;
        }
    }

    @Override
    public List<IrpAccount> getAllIrpAccounts() {
        return irpAccountRepository.findByAccountStatusOrderByCreatedAtDesc("ACTIVE");
    }

    @Override
    public List<IrpAccount> getChangedIrpAccounts(LocalDateTime sinceDateTime) {
        try {
            return irpAccountRepository.findByUpdatedAtAfter(sinceDateTime);
        } catch (Exception e) {
            log.error("변경된 IRP 계좌 조회 실패", e);
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getIrpStatistics() {
        List<IrpAccount> activeAccounts = getAllIrpAccounts();

        long totalAccounts = activeAccounts.size();
        BigDecimal totalBalance = activeAccounts.stream()
                .map(IrpAccount::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAccounts", totalAccounts);
        stats.put("totalBalance", totalBalance);
        stats.put("averageBalance", totalAccounts > 0 ? totalBalance.divide(BigDecimal.valueOf((long)totalAccounts), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        return stats;
    }

    private void ensureCustomerExists(IrpAccountRequest request) {
        try {
            Optional<Customer> existingCustomer = customerRepository.findByCi(request.getCustomerCi());

            if (existingCustomer.isEmpty()) {
                log.info("하나은행에 고객 정보가 없어 자동 생성 - CI: {}, 이름: {}", 
                        request.getCustomerCi(), request.getCustomerName());

                Customer newCustomer = Customer.builder()
                        .ci(request.getCustomerCi())
                        .name(request.getCustomerName())
                        .birthDate(request.getBirthDate())
                        .gender(request.getGender())
                        .phone(request.getPhone())
                        .hasIrpAccount(false)
                        .build();

                customerRepository.save(newCustomer);
                log.info("하나은행에 고객 정보 자동 생성 완료 - CI: {}", request.getCustomerCi());
            } else {
                log.info("하나은행에 고객 정보 이미 존재 - CI: {}", request.getCustomerCi());
            }
        } catch (Exception e) {
            log.error("고객 정보 확인/생성 중 오류 발생 - CI: {}", request.getCustomerCi(), e);
            throw new RuntimeException("고객 정보 처리 실패: " + e.getMessage());
        }
    }

    private String generateAccountNumber(String customerCi) {
        Random random = new Random();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
        String randomDigits = String.format("%03d", random.nextInt(1000));

        return "081" + timestamp + randomDigits;
    }

    private void updateCustomerIrpStatus(String customerCi, boolean hasIrpAccount, String accountNumber) {
        try {
            Optional<Customer> customerOpt = customerRepository.findByCi(customerCi);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                customer.setHasIrpAccount(hasIrpAccount);
                if (hasIrpAccount && accountNumber != null) {
                    customer.setIrpAccountNumber(accountNumber);
                } else {
                    customer.setIrpAccountNumber(null);
                }
                customerRepository.save(customer);
            }
        } catch (Exception e) {
            log.error("고객 IRP 상태 업데이트 실패 - 고객 CI: {}", customerCi, e);
        }
    }

    private void processWithdrawalFromLinkedAccount(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("연결 계좌 출금 처리 시작 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);

        String bankCode = getBankCodeFromAccountNumber(linkedAccountNumber);
        log.info("연결 계좌 은행 구분 - 계좌번호: {}, 은행코드: {}", linkedAccountNumber, bankCode);

        if ("081".equals(bankCode)) {
            processHanaBankWithdrawal(linkedAccountNumber, amount, description);
        } else if ("004".equals(bankCode)) {
            processKookminBankWithdrawal(linkedAccountNumber, amount, description);
        } else if ("088".equals(bankCode)) {
            processShinhanBankWithdrawal(linkedAccountNumber, amount, description);
        } else {
            throw new Exception("지원하지 않는 은행입니다. 은행코드: " + bankCode + ", 계좌번호: " + linkedAccountNumber);
        }

        log.info("연결 계좌 출금 완료 - 계좌번호: {}, 출금액: {}원", linkedAccountNumber, amount);
    }

    private String getBankCodeFromAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            return "UNKNOWN";
        }

        String cleanAccountNumber = accountNumber.replace("-", "");
        String prefix = cleanAccountNumber.substring(0, 3);

        switch (prefix) {
            case "081": return "081";
            case "117": return "081";
            case "004": return "004";
            case "088": return "088";
            case "020": return "020";
            case "011": return "011";
            default: 
                log.warn("알 수 없는 은행코드 - 계좌번호: {}, 추출된 코드: {}", accountNumber, prefix);
                return prefix;
        }
    }

    private void processHanaBankWithdrawal(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("하나은행 계좌 출금 처리 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);

        Account linkedAccount = accountRepository.findByAccountNumber(linkedAccountNumber)
                .orElseThrow(() -> new Exception("하나은행 연결 계좌를 찾을 수 없습니다: " + linkedAccountNumber));

        if (linkedAccount.getBalance() == null || linkedAccount.getBalance().compareTo(amount) < 0) {
            throw new Exception("하나은행 연결 계좌 잔액이 부족합니다. 현재 잔액: " + 
                    (linkedAccount.getBalance() != null ? linkedAccount.getBalance() : BigDecimal.ZERO) + "원");
        }

        BigDecimal newBalance = linkedAccount.getBalance().subtract(amount);
        linkedAccount.setBalance(newBalance);
        accountRepository.save(linkedAccount);

        saveWithdrawalTransaction(linkedAccountNumber, amount, newBalance, description);

        log.info("하나은행 계좌 출금 완료 - 계좌번호: {}, 출금액: {}원, 새 잔액: {}원", 
                linkedAccountNumber, amount, newBalance);
    }

    private void processKookminBankWithdrawal(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("국민은행 계좌 출금 요청 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);

        try {
            String kookminServerUrl = "http://localhost:8082"; // 국민은행 서버 URL
            String withdrawalUrl = kookminServerUrl + "/api/kookmin/accounts/withdrawal";

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accountNumber", linkedAccountNumber);
            requestData.put("amount", amount);
            requestData.put("description", description);
            requestData.put("memo", "하나은행 IRP 계좌 초기 입금");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(withdrawalUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");

                if (success != null && success) {
                    String transactionId = (String) responseBody.get("transactionId");
                    log.info("국민은행 계좌 출금 성공 - 계좌번호: {}, 금액: {}원, 거래ID: {}", 
                            linkedAccountNumber, amount, transactionId);
                } else {
                    String errorMessage = (String) responseBody.get("message");
                    throw new Exception("국민은행 출금 실패: " + errorMessage);
                }
            } else {
                throw new Exception("국민은행 서버 응답 오류 - 상태코드: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("국민은행 계좌 출금 실패 - 계좌번호: {}, 오류: {}", linkedAccountNumber, e.getMessage());
            throw new Exception("국민은행 계좌 출금 실패: " + e.getMessage());
        }
    }

    private void processShinhanBankWithdrawal(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("신한은행 계좌 출금 요청 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);

        try {
            String shinhanServerUrl = "http://localhost:8083"; // 신한은행 서버 URL
            String withdrawalUrl = shinhanServerUrl + "/api/shinhan/accounts/withdrawal";

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accountNumber", linkedAccountNumber);
            requestData.put("amount", amount);
            requestData.put("description", description);
            requestData.put("memo", "하나은행 IRP 계좌 초기 입금");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(withdrawalUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");

                if (success != null && success) {
                    String transactionId = (String) responseBody.get("transactionId");
                    log.info("신한은행 계좌 출금 성공 - 계좌번호: {}, 금액: {}원, 거래ID: {}", 
                            linkedAccountNumber, amount, transactionId);
                } else {
                    String errorMessage = (String) responseBody.get("message");
                    throw new Exception("신한은행 출금 실패: " + errorMessage);
                }
            } else {
                throw new Exception("신한은행 서버 응답 오류 - 상태코드: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("신한은행 계좌 출금 실패 - 계좌번호: {}, 오류: {}", linkedAccountNumber, e.getMessage());
            throw new Exception("신한은행 계좌 출금 실패: " + e.getMessage());
        }
    }

    private void saveWithdrawalTransaction(String accountNumber, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElse(null);

            if (account == null) {
                log.warn("출금 거래내역 저장 실패 - 계좌를 찾을 수 없음: {}", accountNumber);
                return;
            }

            String transactionId = "HANA-WD-" + System.currentTimeMillis() + "-" +
                    String.format("%04d", (int)(Math.random() * 10000));

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("출금")
                    .transactionCategory("IRP 초기 입금")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .branchName("하나은행 본점")
                    .account(account)
                    .build();

            transactionRepository.save(transaction);

            log.info("하나은행 출금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원",
                    transactionId, accountNumber, amount);

        } catch (Exception e) {
            log.error("하나은행 출금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
        }
    }

    @Override
    public com.hanainplan.hana.user.dto.IrpDepositResponse processIrpDeposit(String accountNumber, BigDecimal amount, String description) {
        log.info("하나은행 IRP 계좌 입금 처리 시작 - 계좌번호: {}, 금액: {}원", accountNumber, amount);

        try {
            Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByAccountNumber(accountNumber);
            if (irpAccountOpt.isEmpty()) {
                log.error("IRP 계좌를 찾을 수 없음 - 계좌번호: {}", accountNumber);
                return com.hanainplan.hana.user.dto.IrpDepositResponse.failure("IRP 계좌를 찾을 수 없습니다: " + accountNumber);
            }

            IrpAccount irpAccount = irpAccountOpt.get();

            if (!"ACTIVE".equals(irpAccount.getAccountStatus())) {
                log.error("비활성 IRP 계좌 - 계좌번호: {}, 상태: {}", accountNumber, irpAccount.getAccountStatus());
                return com.hanainplan.hana.user.dto.IrpDepositResponse.failure("비활성 상태의 IRP 계좌입니다");
            }

            BigDecimal currentBalance = irpAccount.getCurrentBalance() != null ? irpAccount.getCurrentBalance() : BigDecimal.ZERO;
            BigDecimal totalContribution = irpAccount.getTotalContribution() != null ? irpAccount.getTotalContribution() : BigDecimal.ZERO;

            BigDecimal newBalance = currentBalance.add(amount);
            BigDecimal newTotalContribution = totalContribution.add(amount);

            irpAccount.setCurrentBalance(newBalance);
            irpAccount.setTotalContribution(newTotalContribution);
            irpAccount.setLastContributionDate(LocalDate.now());
            irpAccountRepository.save(irpAccount);

            log.info("IRP 계좌 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원, 총 납입금: {}원", 
                    accountNumber, newBalance, newTotalContribution);

            Optional<Account> generalAccountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (generalAccountOpt.isPresent()) {
                Account generalAccount = generalAccountOpt.get();
                generalAccount.setBalance(newBalance);
                accountRepository.save(generalAccount);

                log.info("일반 계좌 테이블 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원", accountNumber, newBalance);

                String transactionId = "HANA-IRP-DP-" + System.currentTimeMillis() + "-" + 
                        String.format("%04d", (int)(Math.random() * 10000));

                Transaction transaction = Transaction.builder()
                        .transactionId(transactionId)
                        .accountNumber(accountNumber)
                        .transactionDatetime(LocalDateTime.now())
                        .transactionType("입금")
                        .transactionCategory("IRP 입금")
                        .transactionStatus("COMPLETED")
                        .transactionDirection("CREDIT")
                        .amount(amount)
                        .balanceAfter(newBalance)
                        .description(description != null ? description : "IRP 계좌 입금")
                        .branchName("하나은행 본점")
                        .referenceNumber(transactionId)
                        .account(generalAccount)
                        .build();

                transactionRepository.save(transaction);

                log.info("IRP 입금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                        transactionId, accountNumber, amount);

                return com.hanainplan.hana.user.dto.IrpDepositResponse.success(
                        "IRP 계좌 입금이 완료되었습니다",
                        transactionId,
                        accountNumber,
                        amount,
                        newBalance,
                        newTotalContribution
                );
            } else {
                log.warn("일반 계좌 테이블에 IRP 계좌 없음 - 계좌번호: {}", accountNumber);
                return com.hanainplan.hana.user.dto.IrpDepositResponse.success(
                        "IRP 계좌 입금이 완료되었습니다 (거래내역 미저장)",
                        "N/A",
                        accountNumber,
                        amount,
                        newBalance,
                        newTotalContribution
                );
            }

        } catch (Exception e) {
            log.error("IRP 계좌 입금 처리 실패 - 계좌번호: {}, 금액: {}원, 오류: {}", accountNumber, amount, e.getMessage());
            return com.hanainplan.hana.user.dto.IrpDepositResponse.failure("IRP 계좌 입금 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}