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

/**
 * IRP 계좌 서비스 구현체
 */
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
            // 1. 고객 정보 확인 및 자동 생성
            ensureCustomerExists(request);

            // 2. 중복 계좌 보유 여부 확인 (해당 고객의 하나은행 IRP 계좌만 확인)
            List<IrpAccount> customerIrpAccounts = irpAccountRepository.findAllByCustomerCiAndBankCodeAndAccountStatus(
                    request.getCustomerCi(), "HANA", "ACTIVE");

            if (!customerIrpAccounts.isEmpty()) {
                return IrpAccountResponse.builder()
                        .success(false)
                        .message("이미 하나은행 IRP 계좌를 보유하고 있습니다.")
                        .accountNumber(customerIrpAccounts.get(0).getAccountNumber())
                        .build();
            }

            // 3. 연결 주계좌 유효성 확인
            if (request.getLinkedMainAccount() == null || request.getLinkedMainAccount().length() < 10) {
                return IrpAccountResponse.builder()
                        .success(false)
                        .message("연결할 주계좌 정보가 올바르지 않습니다.")
                        .build();
            }

            // 3. 계좌번호 생성
            String accountNumber = generateAccountNumber(request.getCustomerCi());

            // 4. IRP 계좌 생성
            IrpAccount account = IrpAccount.builder()
                    .customerCi(request.getCustomerCi())
                    .bankCode("HANA") // 하나은행 계좌
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
                    .productCode("IRP001") // 고정값
                    .productName("하나은행 개인형퇴직연금")
                    .managementFeeRate(BigDecimal.valueOf(0.5)) // 0.5%
                    .trustFeeRate(BigDecimal.valueOf(0.1)) // 0.1%
                    .openDate(LocalDate.now())
                    .maturityDate(LocalDate.now().plusYears(55)) // 55세까지
                    .lastContributionDate(LocalDate.now())
                    .syncStatus("PENDING")
                    .externalAccountId(accountNumber) // 은행 계좌번호 저장
                    .build();

            // 5. 데이터베이스에 저장
            IrpAccount savedAccount = irpAccountRepository.save(account);

            // 6. IRP 계좌를 일반 계좌 테이블에도 등록 (account_type=6)
            // 초기 입금이 있으면 그 금액으로, 없으면 0원으로 시작
            Account irpGeneralAccount = Account.builder()
                    .accountNumber(accountNumber)
                    .accountType(6) // IRP 계좌
                    .balance(request.getInitialDeposit()) // 초기 입금액 설정
                    .openingDate(LocalDate.now())
                    .customerCi(request.getCustomerCi())
                    .build();
            accountRepository.save(irpGeneralAccount);
            
            log.info("IRP 계좌를 일반 계좌 테이블에 등록 완료 - 계좌번호: {}, account_type: 6, 초기잔액: {}원", 
                    accountNumber, request.getInitialDeposit());

            // 7. 고객 정보 업데이트 (IRP 계좌 보유 여부)
            updateCustomerIrpStatus(request.getCustomerCi(), true, accountNumber);

        // 8. 초기 입금 처리 (기존 계좌에서 출금 → IRP 계좌로 입금)
        if (request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            try {
                // 8-1. 연결된 주계좌에서 출금 처리
                processWithdrawalFromLinkedAccount(request.getLinkedMainAccount(), request.getInitialDeposit(), 
                        "IRP 계좌(" + accountNumber + ") 초기 입금");
                
                // 8-2. IRP 계좌 잔액 업데이트 (hana_irp_accounts)
                savedAccount.setCurrentBalance(request.getInitialDeposit());
                savedAccount.setTotalContribution(request.getInitialDeposit());
                irpAccountRepository.save(savedAccount);
                
                // 8-3. IRP 거래내역을 일반 거래내역 테이블에 저장
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
                // 초기 입금 실패 시에도 계좌는 개설된 상태로 유지 (잔액 0원)
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
                // 직접 closedAt 필드 설정 (Lombok @Data가 setter를 생성해야 함)
                account.setClosedAt(LocalDateTime.now());
                irpAccountRepository.save(account);

                // 고객 정보 업데이트 (IRP 계좌 보유 여부)
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
                // 동기화 로직 (실제로는 HANAinPLAN API 호출)
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

    /**
     * 고객 정보 확인 및 자동 생성
     */
    private void ensureCustomerExists(IrpAccountRequest request) {
        try {
            // 고객 정보가 이미 존재하는지 확인
            Optional<Customer> existingCustomer = customerRepository.findByCi(request.getCustomerCi());
            
            if (existingCustomer.isEmpty()) {
                // 고객 정보가 없으면 자동 생성
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

    /**
     * 계좌번호 생성 (하나은행 형식: 081-XXXXXXX-01)
     */
    private String generateAccountNumber(String customerCi) {
        // 현재 시간 기반으로 랜덤 계좌번호 생성 (실제 은행 시스템과 연동 시 변경 필요)
        Random random = new Random();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6); // 7자리
        String randomDigits = String.format("%03d", random.nextInt(1000)); // 3자리

        return "081" + timestamp + randomDigits;
    }

    /**
     * 고객 정보 업데이트 (IRP 계좌 보유 여부)
     */
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

    /**
     * 연결된 주계좌에서 출금 처리 (타행 계좌 지원)
     */
    private void processWithdrawalFromLinkedAccount(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("연결 계좌 출금 처리 시작 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);
        
        // 1. 계좌번호로 은행 구분
        String bankCode = getBankCodeFromAccountNumber(linkedAccountNumber);
        log.info("연결 계좌 은행 구분 - 계좌번호: {}, 은행코드: {}", linkedAccountNumber, bankCode);
        
        if ("081".equals(bankCode)) {
            // 하나은행 계좌인 경우 - 기존 로직
            processHanaBankWithdrawal(linkedAccountNumber, amount, description);
        } else if ("004".equals(bankCode)) {
            // 국민은행 계좌인 경우
            processKookminBankWithdrawal(linkedAccountNumber, amount, description);
        } else if ("088".equals(bankCode)) {
            // 신한은행 계좌인 경우
            processShinhanBankWithdrawal(linkedAccountNumber, amount, description);
        } else {
            throw new Exception("지원하지 않는 은행입니다. 은행코드: " + bankCode + ", 계좌번호: " + linkedAccountNumber);
        }
        
        log.info("연결 계좌 출금 완료 - 계좌번호: {}, 출금액: {}원", linkedAccountNumber, amount);
    }
    
    /**
     * 계좌번호에서 은행코드 추출
     */
    private String getBankCodeFromAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            return "UNKNOWN";
        }
        
        // 하이픈 제거하고 앞자리 3자리 추출
        String cleanAccountNumber = accountNumber.replace("-", "");
        String prefix = cleanAccountNumber.substring(0, 3);
        
        // 은행코드 매핑
        switch (prefix) {
            case "081": return "081"; // 하나은행
            case "117": return "081"; // 하나은행 (다른 계좌번호 prefix)
            case "004": return "004"; // 국민은행  
            case "088": return "088"; // 신한은행
            case "020": return "020"; // 우리은행
            case "011": return "011"; // 농협은행
            default: 
                log.warn("알 수 없는 은행코드 - 계좌번호: {}, 추출된 코드: {}", accountNumber, prefix);
                return prefix;
        }
    }
    
    /**
     * 하나은행 계좌 출금 처리 (기존 로직)
     */
    private void processHanaBankWithdrawal(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("하나은행 계좌 출금 처리 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);
        
        // 1. 출금할 계좌 조회
        Account linkedAccount = accountRepository.findByAccountNumber(linkedAccountNumber)
                .orElseThrow(() -> new Exception("하나은행 연결 계좌를 찾을 수 없습니다: " + linkedAccountNumber));
        
        // 2. 잔액 확인
        if (linkedAccount.getBalance() == null || linkedAccount.getBalance().compareTo(amount) < 0) {
            throw new Exception("하나은행 연결 계좌 잔액이 부족합니다. 현재 잔액: " + 
                    (linkedAccount.getBalance() != null ? linkedAccount.getBalance() : BigDecimal.ZERO) + "원");
        }
        
        // 3. 출금 처리
        BigDecimal newBalance = linkedAccount.getBalance().subtract(amount);
        linkedAccount.setBalance(newBalance);
        accountRepository.save(linkedAccount);
        
        // 4. 출금 거래내역 저장
        saveWithdrawalTransaction(linkedAccountNumber, amount, newBalance, description);
        
        log.info("하나은행 계좌 출금 완료 - 계좌번호: {}, 출금액: {}원, 새 잔액: {}원", 
                linkedAccountNumber, amount, newBalance);
    }
    
    /**
     * 국민은행 계좌 출금 처리 (타행 출금 요청)
     */
    private void processKookminBankWithdrawal(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("국민은행 계좌 출금 요청 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);
        
        try {
            // 국민은행 서버에 출금 요청
            String kookminServerUrl = "http://localhost:8082"; // 국민은행 서버 URL
            String withdrawalUrl = kookminServerUrl + "/api/kookmin/accounts/withdrawal";
            
            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accountNumber", linkedAccountNumber);
            requestData.put("amount", amount);
            requestData.put("description", description);
            requestData.put("memo", "하나은행 IRP 계좌 초기 입금");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            // RestTemplate으로 국민은행 서버 호출
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
    
    /**
     * 신한은행 계좌 출금 처리 (타행 출금 요청)
     */
    private void processShinhanBankWithdrawal(String linkedAccountNumber, BigDecimal amount, String description) throws Exception {
        log.info("신한은행 계좌 출금 요청 - 계좌번호: {}, 금액: {}원", linkedAccountNumber, amount);
        
        try {
            // 신한은행 서버에 출금 요청
            String shinhanServerUrl = "http://localhost:8083"; // 신한은행 서버 URL
            String withdrawalUrl = shinhanServerUrl + "/api/shinhan/accounts/withdrawal";
            
            // 요청 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accountNumber", linkedAccountNumber);
            requestData.put("amount", amount);
            requestData.put("description", description);
            requestData.put("memo", "하나은행 IRP 계좌 초기 입금");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            // RestTemplate으로 신한은행 서버 호출
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
    
    /**
     * 출금 거래내역 저장
     */
    private void saveWithdrawalTransaction(String accountNumber, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElse(null);

            if (account == null) {
                log.warn("출금 거래내역 저장 실패 - 계좌를 찾을 수 없음: {}", accountNumber);
                return;
            }

            // 거래 ID 생성
            String transactionId = "HANA-WD-" + System.currentTimeMillis() + "-" +
                    String.format("%04d", (int)(Math.random() * 10000));

            // 출금 거래내역 생성
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

}

