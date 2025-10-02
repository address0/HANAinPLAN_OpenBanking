package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.IrpAccountDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenRequestDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenResponseDto;
import com.hanainplan.domain.banking.dto.IrpAccountStatusResponseDto;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IRP 통합 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IrpIntegrationServiceImpl implements IrpIntegrationService {

    private final IrpAccountRepository irpAccountRepository;
    private final UserRepository userRepository;
    private final HanaBankClient hanaBankClient;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // ===== 계좌 관리 =====

    @Override
    public List<IrpAccountDto> getCustomerIrpAccounts(String customerCi) {
        List<IrpAccount> accounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);
        return accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrpAccountDto> getCustomerIrpAccountsByCustomerId(Long customerId) {
        Optional<User> userOpt = userRepository.findById(customerId);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }

        User user = userOpt.get();
        if (user.getCi() == null || user.getCi().isEmpty()) {
            return new ArrayList<>();
        }

        List<IrpAccount> accounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(user.getCi());
        return accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrpAccountDto> getBankIrpAccounts(String bankCode) {
        List<IrpAccount> accounts = irpAccountRepository.findByBankCodeAndAccountStatusOrderByCreatedDateDesc(
                bankCode, "ACTIVE");
        return accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrpAccountDto> getAllIrpAccounts() {
        List<IrpAccount> accounts = irpAccountRepository.findByAccountStatusOrderByCreatedDateDesc("ACTIVE");
        return accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public IrpAccountDto getIrpAccountDetail(Long irpAccountId) {
        Optional<IrpAccount> accountOpt = irpAccountRepository.findById(irpAccountId);
        return accountOpt.map(this::convertToDto).orElse(null);
    }

    @Override
    public boolean hasIrpAccount(String customerCi) {
        return irpAccountRepository.existsByCustomerCi(customerCi);
    }

    @Override
    public boolean hasIrpAccountByCustomerId(Long customerId) {
        Optional<User> userOpt = userRepository.findById(customerId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (user.getCi() == null || user.getCi().isEmpty()) {
            return false;
        }

        return irpAccountRepository.existsByCustomerCi(user.getCi());
    }

    @Override
    public IrpAccountStatusResponseDto checkIrpAccountStatus(String customerCi) {
        boolean hasAccount = hasIrpAccount(customerCi);
        return IrpAccountStatusResponseDto.success(customerCi, hasAccount);
    }

    @Override
    public IrpAccountStatusResponseDto checkIrpAccountStatusByCustomerId(Long customerId) {
        boolean hasAccount = hasIrpAccountByCustomerId(customerId);
        return IrpAccountStatusResponseDto.success(customerId.toString(), hasAccount);
    }

    @Override
    @Transactional
    public IrpAccountOpenResponseDto openIrpAccount(IrpAccountOpenRequestDto request) throws Exception {
        log.info("IRP 계좌 개설 시작 - 고객 ID: {}", request.getCustomerId());

        // 1. 중복 계좌 보유 여부 확인 (고객 ID 기반)
        if (hasIrpAccountByCustomerId(request.getCustomerId())) {
            return IrpAccountOpenResponseDto.failure("이미 IRP 계좌를 보유하고 있습니다.", "DUPLICATE_ACCOUNT");
        }

        // 2. 사용자 정보 조회 및 CI 추출
        Optional<User> userOpt = userRepository.findById(request.getCustomerId());
        if (userOpt.isEmpty()) {
            return IrpAccountOpenResponseDto.failure("사용자를 찾을 수 없습니다.", "USER_NOT_FOUND");
        }

        User user = userOpt.get();
        String customerCi = user.getCi();

        if (customerCi == null || customerCi.isEmpty()) {
            return IrpAccountOpenResponseDto.failure("사용자 CI 정보가 없습니다.", "CI_NOT_FOUND");
        }

        // 요청에서 customerCi가 제공되지 않은 경우 User에서 조회한 CI 사용
        if (request.getCustomerCi() == null || request.getCustomerCi().isEmpty()) {
            request.setCustomerCi(customerCi);
        }

        // 2. 연결 주계좌 유효성 확인 (간단한 형식 검사)
        if (request.getLinkedMainAccount() == null || request.getLinkedMainAccount().length() < 10) {
            return IrpAccountOpenResponseDto.failure("연결 주계좌 정보가 올바르지 않습니다.", "INVALID_LINKED_ACCOUNT");
        }

        try {
            // 3. 먼저 HANAinPLAN DB에 IRP 계좌 정보 저장 (계좌번호는 임시)
            BigDecimal initialDepositAmount = BigDecimal.valueOf(request.getInitialDeposit());
            String tempAccountNumber = "TEMP-" + System.currentTimeMillis();
            
            IrpAccount account = IrpAccount.builder()
                    .customerId(request.getCustomerId())
                    .customerCi(customerCi)
                    .bankCode("HANA")
                    .accountNumber(tempAccountNumber) // 임시 계좌번호
                    .accountStatus("PENDING") // 하나은행 생성 전이므로 PENDING
                    .initialDeposit(initialDepositAmount)
                    .currentBalance(initialDepositAmount)
                    .totalContribution(initialDepositAmount)
                    .monthlyDeposit(request.getMonthlyDeposit() != null ? BigDecimal.valueOf(request.getMonthlyDeposit()) : BigDecimal.ZERO)
                    .isAutoDeposit(request.getIsAutoDeposit())
                    .depositDay(request.getDepositDay())
                    .investmentStyle(request.getInvestmentStyle())
                    .linkedMainAccount(request.getLinkedMainAccount())
                    .productCode("IRP001")
                    .productName("하나은행 IRP")
                    .openDate(LocalDate.now())
                    .lastContributionDate(LocalDate.now())
                    .syncStatus("PENDING")
                    .build();

            // 4. HANAinPLAN 데이터베이스에 저장 (임시)
            IrpAccount savedAccount = irpAccountRepository.save(account);
            
            log.info("하나인플랜 IRP 계좌 임시 저장 완료 - 임시 계좌번호: {}", tempAccountNumber);

            // 5. 연결된 주계좌에서 출금 처리 (하나인플랜 DB)
            BankingAccount linkedAccount = accountRepository.findByAccountNumber(request.getLinkedMainAccount())
                    .orElseThrow(() -> new RuntimeException("연결 주계좌를 찾을 수 없습니다: " + request.getLinkedMainAccount()));
            
            BigDecimal newLinkedBalance = linkedAccount.getBalance().subtract(initialDepositAmount);
            if (newLinkedBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("연결 주계좌 잔액이 부족합니다");
            }
            
            linkedAccount.setBalance(newLinkedBalance);
            accountRepository.save(linkedAccount);
            
            log.info("하나인플랜 연결 주계좌 출금 완료 - 계좌: {}, 출금액: {}원, 남은 잔액: {}원", 
                    request.getLinkedMainAccount(), initialDepositAmount, newLinkedBalance);

            // 6. 하나은행 서버에 IRP 계좌 개설 요청
            IrpAccountOpenResponseDto bankResponse = openIrpAccountAtHanaBank(request);

            if (!bankResponse.isSuccess()) {
                // 하나은행 생성 실패 시 롤백
                linkedAccount.setBalance(linkedAccount.getBalance().add(initialDepositAmount));
                accountRepository.save(linkedAccount);
                irpAccountRepository.delete(savedAccount);
                return bankResponse;
            }

            // 7. 실제 계좌번호로 업데이트
            savedAccount.setAccountNumber(bankResponse.getAccountNumber());
            savedAccount.setAccountStatus("ACTIVE");
            savedAccount.setSyncStatus("SUCCESS");
            savedAccount.setExternalAccountId(bankResponse.getAccountNumber());
            savedAccount.setExternalLastUpdated(LocalDateTime.now());
            irpAccountRepository.save(savedAccount);
            
            log.info("하나인플랜 IRP 계좌번호 업데이트 완료 - 실제 계좌번호: {}", bankResponse.getAccountNumber());

            // 8. IRP 계좌를 일반 계좌 테이블에도 등록 (account_type=6: IRP)
            BankingAccount irpBankingAccount = BankingAccount.builder()
                    .userId(request.getCustomerId())
                    .customerCi(customerCi)
                    .accountNumber(bankResponse.getAccountNumber())
                    .accountName("IRP 연금저축")
                    .accountType(6) // IRP 계좌
                    .accountStatus(BankingAccount.AccountStatus.ACTIVE)
                    .balance(initialDepositAmount)
                    .currencyCode("KRW")
                    .openedDate(LocalDateTime.now())
                    .description("하나은행 IRP 계좌")
                    .build();
            accountRepository.save(irpBankingAccount);
            
            log.info("IRP 계좌를 일반 계좌 테이블에 등록 완료 - 계좌번호: {}, account_type: 6", bankResponse.getAccountNumber());

            // 9. 거래내역 생성 - IRP 계좌에 입금(+), 연결 주계좌에 출금(-)
            createIrpAccountOpenTransactions(request, customerCi, bankResponse.getAccountNumber(), irpBankingAccount, linkedAccount);

            log.info("IRP 계좌 개설 완료 - 계좌번호: {}, 고객 CI: {}", bankResponse.getAccountNumber(), customerCi);

            return bankResponse;

        } catch (Exception e) {
            log.error("IRP 계좌 개설 실패 - 고객 CI: {}", request.getCustomerCi(), e);
            throw new Exception("IRP 계좌 개설에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public IrpAccountDto getCustomerIrpAccount(String customerCi) {
        List<IrpAccount> accounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);

        if (accounts.isEmpty()) {
            return null;
        }

        // 최신 계좌 하나만 반환 (사용자는 하나의 IRP 계좌만 보유 가능)
        IrpAccount account = accounts.get(0);
        return convertToDto(account);
    }

    @Override
    public IrpAccountDto getCustomerIrpAccountByCustomerId(Long customerId) {
        Optional<User> userOpt = userRepository.findById(customerId);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        if (user.getCi() == null || user.getCi().isEmpty()) {
            return null;
        }

        List<IrpAccount> accounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(user.getCi());

        if (accounts.isEmpty()) {
            return null;
        }

        // 최신 계좌 하나만 반환 (사용자는 하나의 IRP 계좌만 보유 가능)
        IrpAccount account = accounts.get(0);
        return convertToDto(account);
    }

    @Override
    public long getActiveIrpAccountCount(String customerCi) {
        return irpAccountRepository.countActiveAccountsByCustomerCi(customerCi);
    }

    @Override
    public long getActiveIrpAccountCountByCustomerId(Long customerId) {
        Optional<User> userOpt = userRepository.findById(customerId);
        if (userOpt.isEmpty()) {
            return 0;
        }

        User user = userOpt.get();
        if (user.getCi() == null || user.getCi().isEmpty()) {
            return 0;
        }

        return irpAccountRepository.countActiveAccountsByCustomerCi(user.getCi());
    }

    // ===== 통계 및 분석 =====

    @Override
    public Map<String, Object> getIrpAccountStatistics() {
        List<Object[]> stats = irpAccountRepository.getIrpStatisticsByBank();

        Map<String, Object> result = new HashMap<>();
        result.put("accountStatistics", stats);

        return result;
    }

    @Override
    public Map<String, Object> getCustomerIrpPortfolio(String customerCi) {
        List<IrpAccount> accounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);

        BigDecimal totalBalance = accounts.stream()
                .map(IrpAccount::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalBalance", totalBalance);
        result.put("accountCount", accounts.size());
        result.put("accounts", accounts.stream().map(this::convertToDto).collect(Collectors.toList()));

        return result;
    }

    @Override
    public Map<String, Object> getCustomerIrpPortfolioByCustomerId(Long customerId) {
        Optional<User> userOpt = userRepository.findById(customerId);
        if (userOpt.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("totalBalance", BigDecimal.ZERO);
            result.put("accountCount", 0);
            result.put("accounts", new ArrayList<>());
            return result;
        }

        User user = userOpt.get();
        if (user.getCi() == null || user.getCi().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("totalBalance", BigDecimal.ZERO);
            result.put("accountCount", 0);
            result.put("accounts", new ArrayList<>());
            return result;
        }

        List<IrpAccount> accounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(user.getCi());

        BigDecimal totalBalance = accounts.stream()
                .map(IrpAccount::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalBalance", totalBalance);
        result.put("accountCount", accounts.size());
        result.put("accounts", accounts.stream().map(this::convertToDto).collect(Collectors.toList()));

        return result;
    }

    @Override
    public Map<String, Object> getTotalIrpBalanceByBank() {
        List<Object[]> balances = irpAccountRepository.getIrpStatisticsByBank();

        Map<String, Object> result = new HashMap<>();
        result.put("balanceByBank", balances);

        return result;
    }

    // ===== 배치 작업 =====

    // @Override
    // @Scheduled(cron = "0 0 2 * * SUN") // 매주 일요일 새벽 2시
    // @Transactional
    // public void weeklyIrpDataSyncBatch() {
    //     log.info("주간 IRP 데이터 배치 동기화 시작");
    //     
    //     // TODO: OpenFeign으로 마이그레이션 필요
    //     // RestTemplate 기반 코드를 Feign Client로 변경 필요
    //     
    //     log.info("주간 IRP 데이터 배치 동기화는 추후 구현 예정");
    // }

    @Override
    @Scheduled(cron = "0 0 3 1 * *") // 매월 1일 새벽 3시
    @Transactional
    public void monthlyIrpStatisticsBatch() {
        log.info("월별 IRP 통계 생성 배치 작업 시작");
        // 통계 생성 로직
        log.info("월별 IRP 통계 생성 배치 작업 완료");
    }

    @Override
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    @Transactional
    public void maturityNotificationBatch() {
        log.info("만기 도래 IRP 계좌 알림 배치 작업 시작");

        List<IrpAccount> maturedAccounts = irpAccountRepository.findMaturedAccounts();

        for (IrpAccount account : maturedAccounts) {
            // 만기 알림 로직
            log.info("만기 도래 계좌 알림: {}", account.getAccountNumber());
        }

        log.info("만기 도래 IRP 계좌 알림 배치 작업 완료");
    }

    // ===== 내부 헬퍼 메소드 =====

    private IrpAccountDto convertToDto(IrpAccount account) {
        return IrpAccountDto.builder()
                .irpAccountId(account.getIrpAccountId())
                .customerId(account.getCustomerId())
                .customerCi(account.getCustomerCi())
                .bankCode(account.getBankCode())
                .accountNumber(account.getAccountNumber())
                .accountStatus(account.getAccountStatus())
                .initialDeposit(account.getInitialDeposit())
                .monthlyDeposit(account.getMonthlyDeposit())
                .currentBalance(account.getCurrentBalance())
                .totalContribution(account.getTotalContribution())
                .totalReturn(account.getTotalReturn())
                .returnRate(account.getReturnRate())
                .investmentStyle(account.getInvestmentStyle())
                .isAutoDeposit(account.getIsAutoDeposit())
                .depositDay(account.getDepositDay())
                .linkedMainAccount(account.getLinkedMainAccount())
                .productCode(account.getProductCode())
                .productName(account.getProductName())
                .managementFeeRate(account.getManagementFeeRate())
                .trustFeeRate(account.getTrustFeeRate())
                .openDate(account.getOpenDate())
                .maturityDate(account.getMaturityDate())
                .lastContributionDate(account.getLastContributionDate())
                .lastSyncDate(account.getLastSyncDate())
                .syncStatus(account.getSyncStatus())
                .syncErrorMessage(account.getSyncErrorMessage())
                .externalAccountId(account.getExternalAccountId())
                .externalLastUpdated(account.getExternalLastUpdated())
                .createdDate(account.getCreatedDate())
                .build();
    }

    // 아래 메서드들은 RestTemplate 기반이므로 주석 처리 (Feign 마이그레이션 필요)
    // private void syncCustomerFromHanaBank(String customerCi) {
    //     // TODO: OpenFeign으로 마이그레이션 필요
    // }
    //
    // private void syncCustomerFromHanaBankByCustomerId(Long customerId) {
    //     // TODO: OpenFeign으로 마이그레이션 필요
    // }


    /**
     * 하나은행 서버에 IRP 계좌 개설 요청 (OpenFeign 사용)
     */
    private IrpAccountOpenResponseDto openIrpAccountAtHanaBank(IrpAccountOpenRequestDto request) throws Exception {
        try {
            log.info("하나은행 서버에 IRP 계좌 개설 요청 - 고객 CI: {}", request.getCustomerCi());

            // 하나은행 서버 요청 데이터 구성
            Map<String, Object> hanaBankRequest = new HashMap<>();
            hanaBankRequest.put("customerCi", request.getCustomerCi());
            hanaBankRequest.put("initialDeposit", request.getInitialDeposit());
            hanaBankRequest.put("monthlyDeposit", request.getMonthlyDeposit() != null ? request.getMonthlyDeposit() : 0);
            hanaBankRequest.put("isAutoDeposit", request.getIsAutoDeposit());
            hanaBankRequest.put("depositDay", request.getDepositDay());
            hanaBankRequest.put("investmentStyle", request.getInvestmentStyle());
            hanaBankRequest.put("linkedMainAccount", request.getLinkedMainAccount());
            hanaBankRequest.put("productCode", "IRP001");

            log.info("하나은행 IRP 계좌 개설 요청 데이터: {}", hanaBankRequest);

            // OpenFeign으로 하나은행 API 호출 (코드 간소화!)
            Map<String, Object> responseBody = hanaBankClient.openIrpAccount(hanaBankRequest);

            // 응답 처리
            String accountNumber = (String) responseBody.get("accountNumber");
            String message = (String) responseBody.get("message");
            Boolean success = (Boolean) responseBody.get("success");

            if (success != null && success) {
                log.info("하나은행 서버에서 IRP 계좌 개설 성공 - 계좌번호: {}", accountNumber);
                return IrpAccountOpenResponseDto.success(accountNumber, message != null ? message : "IRP 계좌가 성공적으로 개설되었습니다.");
            } else {
                String errorMessage = message != null ? message : "하나은행 서버에서 계좌 개설에 실패했습니다.";
                log.error("하나은행 서버에서 IRP 계좌 개설 실패 - {}", errorMessage);
                return IrpAccountOpenResponseDto.failure(errorMessage, "BANK_ERROR");
            }

        } catch (Exception e) {
            log.error("하나은행 서버에 IRP 계좌 개설 요청 실패", e);
            throw new Exception("하나은행 서버 연동 실패: " + e.getMessage());
        }
    }

    /**
     * IRP 계좌 개설 시 거래내역 생성
     */
    private void createIrpAccountOpenTransactions(IrpAccountOpenRequestDto request, String customerCi, 
                                                   String irpAccountNumber, BankingAccount irpBankingAccount,
                                                   BankingAccount linkedAccount) {
        try {
            BigDecimal initialDepositAmount = BigDecimal.valueOf(request.getInitialDeposit());
            String transactionNumber = Transaction.generateTransactionNumber();
            
            // 1. 연결 주계좌에 출금(-) 거래내역 생성
            // ⚠️ 주의: linkedAccount.getBalance()는 이미 차감된 잔액이므로 그대로 사용
            Transaction withdrawalTransaction = Transaction.builder()
                        .transactionNumber(transactionNumber + "_OUT")
                        .fromAccountId(linkedAccount.getAccountId())
                        .toAccountId(null) // IRP 계좌는 별도 시스템이므로 null
                        .transactionType(Transaction.TransactionType.TRANSFER)
                        .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                        .amount(initialDepositAmount)
                        .balanceAfter(linkedAccount.getBalance()) // 이미 차감된 잔액
                        .transactionDirection(Transaction.TransactionDirection.DEBIT)
                        .description("IRP 계좌 개설 초기 입금")
                        .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                        .transactionDate(LocalDateTime.now())
                        .processedDate(LocalDateTime.now())
                        .referenceNumber("IRP_OPEN_" + irpAccountNumber)
                        .memo("IRP 계좌(" + irpAccountNumber + ") 개설")
                        .build();
            
            transactionRepository.save(withdrawalTransaction);
            
            log.info("연결 주계좌 출금 거래내역 생성 완료 - 계좌: {}, 금액: {}", 
                    linkedAccount.getAccountNumber(), initialDepositAmount);
            
            // 2. IRP 계좌에 입금(+) 거래내역 생성 (일반 거래내역 테이블 사용)
            Transaction irpDepositTransaction = Transaction.builder()
                    .transactionNumber(transactionNumber + "_IN")
                    .fromAccountId(null) // 외부 계좌에서 입금
                    .toAccountId(irpBankingAccount.getAccountId())
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                    .amount(initialDepositAmount)
                    .balanceAfter(initialDepositAmount)
                    .transactionDirection(Transaction.TransactionDirection.CREDIT)
                    .description("IRP 계좌 초기 입금")
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .transactionDate(LocalDateTime.now())
                    .processedDate(LocalDateTime.now())
                    .referenceNumber("IRP_OPEN_" + irpAccountNumber)
                    .memo("IRP 계좌 개설 초기 입금")
                    .build();
            
            transactionRepository.save(irpDepositTransaction);
            
            log.info("IRP 계좌 입금 거래내역 생성 완료 - 계좌: {}, 금액: {}", irpAccountNumber, initialDepositAmount);
            
        } catch (Exception e) {
            log.error("IRP 계좌 개설 거래내역 생성 실패", e);
            // 거래내역 생성 실패는 계좌 개설 자체를 실패시키지 않음
        }
    }

}
