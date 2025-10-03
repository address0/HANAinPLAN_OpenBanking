package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.IrpAccountDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenRequestDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenResponseDto;
import com.hanainplan.domain.banking.dto.IrpAccountStatusResponseDto;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.client.KookminBankClient;
import com.hanainplan.domain.banking.client.ShinhanBankClient;
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
    private final KookminBankClient kookminBankClient;
    private final ShinhanBankClient shinhanBankClient;
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

        // 3. 연결 주계좌 유효성 확인 (간단한 형식 검사)
        if (request.getLinkedMainAccount() == null || request.getLinkedMainAccount().length() < 10) {
            return IrpAccountOpenResponseDto.failure("연결 주계좌 정보가 올바르지 않습니다.", "INVALID_LINKED_ACCOUNT");
        }

        // 4. 연결된 주계좌 조회 및 잔액 확인 (DB 변경 전 검증)
        BankingAccount linkedAccount = accountRepository.findByAccountNumber(request.getLinkedMainAccount())
                .orElseThrow(() -> new RuntimeException("연결 주계좌를 찾을 수 없습니다: " + request.getLinkedMainAccount()));
        
        BigDecimal initialDepositAmount = BigDecimal.valueOf(request.getInitialDeposit());
        BigDecimal newLinkedBalance = linkedAccount.getBalance().subtract(initialDepositAmount);
        if (newLinkedBalance.compareTo(BigDecimal.ZERO) < 0) {
            return IrpAccountOpenResponseDto.failure("연결 주계좌 잔액이 부족합니다", "INSUFFICIENT_BALANCE");
        }

        try {
            // ============================================================
            // 5. 외부 API 호출 (DB 변경 전에 먼저 실행)
            // ============================================================
            
            // 5-1. 실제 은행 서버에 출금 요청 (DB 변경 전)
            String bankCode = determineBankCode(request.getLinkedMainAccount());
            try {
                Map<String, Object> withdrawalRequest = new HashMap<>();
                withdrawalRequest.put("accountNumber", request.getLinkedMainAccount());
                withdrawalRequest.put("amount", initialDepositAmount);
                withdrawalRequest.put("description", "IRP 계좌 개설 초기 입금");
                
                Map<String, Object> withdrawalResponse;
                
                switch (bankCode) {
                    case "HANA":
                        withdrawalResponse = hanaBankClient.processWithdrawal(withdrawalRequest);
                        break;
                    case "KOOKMIN":
                        withdrawalResponse = kookminBankClient.processWithdrawal(withdrawalRequest);
                        break;
                    case "SHINHAN":
                        withdrawalResponse = shinhanBankClient.processWithdrawal(withdrawalRequest);
                        break;
                    default:
                        throw new RuntimeException("지원하지 않는 은행입니다: " + bankCode);
                }
                
                log.info("[{}] 연결 주계좌 출금 처리 완료 - 계좌: {}, 금액: {}원", 
                        bankCode, request.getLinkedMainAccount(), initialDepositAmount);
                
            } catch (Exception e) {
                log.error("연결 주계좌 출금 실패 - 은행: {}, 계좌: {}, 오류: {}", 
                        bankCode, request.getLinkedMainAccount(), e.getMessage());
                // 외부 API 실패 시 즉시 실패 응답 (DB 변경 없음)
                return IrpAccountOpenResponseDto.failure("연결 주계좌 출금에 실패했습니다: " + e.getMessage(), "WITHDRAWAL_FAILED");
            }

            // 5-2. 하나은행 서버에 IRP 계좌 개설 요청 (DB 변경 전)
            IrpAccountOpenResponseDto bankResponse = openIrpAccountAtHanaBank(request);

            if (!bankResponse.isSuccess()) {
                log.error("하나은행 IRP 계좌 개설 실패 - 응답: {}", bankResponse.getMessage());
                
                // 보상 트랜잭션: 이미 수행된 출금을 취소 (입금으로 원복)
                try {
                    log.info("보상 트랜잭션 시작 - 출금 취소 (입금으로 원복)");
                    
                    Map<String, Object> compensationRequest = new HashMap<>();
                    compensationRequest.put("accountNumber", request.getLinkedMainAccount());
                    compensationRequest.put("amount", initialDepositAmount);
                    compensationRequest.put("description", "IRP 계좌 개설 실패로 인한 출금 취소");
                    
                    Map<String, Object> compensationResponse;
                    
                    switch (bankCode) {
                        case "HANA":
                            compensationResponse = hanaBankClient.createDepositTransaction(compensationRequest);
                            break;
                        case "KOOKMIN":
                            // 국민은행 입금 API 호출 (클라이언트에 메서드가 있다고 가정)
                            compensationResponse = kookminBankClient.createDepositTransaction(compensationRequest);
                            break;
                        case "SHINHAN":
                            // 신한은행 입금 API 호출 (클라이언트에 메서드가 있다고 가정)
                            compensationResponse = shinhanBankClient.createDepositTransaction(compensationRequest);
                            break;
                        default:
                            throw new RuntimeException("지원하지 않는 은행입니다: " + bankCode);
                    }
                    
                    log.info("[{}] 보상 트랜잭션 완료 - 출금 취소 성공, 계좌: {}, 금액: {}원", 
                            bankCode, request.getLinkedMainAccount(), initialDepositAmount);
                    
                } catch (Exception compensationError) {
                    log.error("보상 트랜잭션 실패 - 수동 확인 필요! 은행: {}, 계좌: {}, 금액: {}원, 오류: {}", 
                            bankCode, request.getLinkedMainAccount(), initialDepositAmount, compensationError.getMessage());
                    // 보상 트랜잭션 실패는 심각한 문제이므로 로그에 기록하고 알림 필요
                }
                
                return bankResponse;
            }
            
            String realAccountNumber = bankResponse.getAccountNumber();
            log.info("하나은행 IRP 계좌 개설 완료 - 실제 계좌번호: {}", realAccountNumber);

            // ============================================================
            // 6. 외부 API 호출이 모두 성공한 후 DB 변경 시작
            //    (이 시점부터 오류 발생 시 Spring의 @Transactional이 자동 롤백)
            // ============================================================
            
            try {
                // 6-1. HanaInPlan DB에 연결 주계좌 출금 반영
                linkedAccount.setBalance(newLinkedBalance);
                accountRepository.save(linkedAccount);
                
                log.info("[HanaInPlan] 연결 주계좌 출금 완료 - 계좌: {}, 출금액: {}원, 남은 잔액: {}원", 
                        request.getLinkedMainAccount(), initialDepositAmount, newLinkedBalance);

                // 6-2. HANAinPLAN DB에 IRP 계좌 정보 저장
                IrpAccount account = IrpAccount.builder()
                        .customerId(request.getCustomerId())
                        .customerCi(customerCi)
                        .bankCode("HANA")
                        .accountNumber(realAccountNumber) // 실제 계좌번호 사용
                        .accountStatus("ACTIVE") // 하나은행에서 이미 생성되었으므로 ACTIVE
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
                        .syncStatus("SUCCESS")
                        .externalAccountId(realAccountNumber)
                        .externalLastUpdated(LocalDateTime.now())
                        .build();

                IrpAccount savedAccount = irpAccountRepository.save(account);
                log.info("하나인플랜 IRP 계좌 저장 완료 - 계좌번호: {}", realAccountNumber);

                // 6-3. IRP 계좌를 일반 계좌 테이블에도 등록 (account_type=6: IRP)
                BankingAccount irpBankingAccount = BankingAccount.builder()
                        .userId(request.getCustomerId())
                        .customerCi(customerCi)
                        .accountNumber(realAccountNumber)
                        .accountName("IRP 연금저축")
                        .accountType(6) // IRP 계좌
                        .accountStatus(BankingAccount.AccountStatus.ACTIVE)
                        .balance(initialDepositAmount)
                        .currencyCode("KRW")
                        .openedDate(LocalDateTime.now())
                        .description("하나은행 IRP 계좌")
                        .build();
                accountRepository.save(irpBankingAccount);
                
                log.info("IRP 계좌를 일반 계좌 테이블에 등록 완료 - 계좌번호: {}, account_type: 6", realAccountNumber);

                // 6-4. 거래내역 생성 - IRP 계좌에 입금(+), 연결 주계좌에 출금(-)
                createIrpAccountOpenTransactions(request, customerCi, realAccountNumber, irpBankingAccount, linkedAccount);

                log.info("IRP 계좌 개설 완료 - 계좌번호: {}, 고객 CI: {}", realAccountNumber, customerCi);

                return bankResponse;
                
            } catch (Exception dbError) {
                log.error("DB 저장 중 오류 발생 - 보상 트랜잭션 시작", dbError);
                
                // DB 저장 실패 시 보상 트랜잭션: 외부 API로 생성된 데이터 취소
                try {
                    // 보상 1: 연결 주계좌 출금 취소 (입금으로 원복)
                    Map<String, Object> compensationRequest = new HashMap<>();
                    compensationRequest.put("accountNumber", request.getLinkedMainAccount());
                    compensationRequest.put("amount", initialDepositAmount);
                    compensationRequest.put("description", "IRP 계좌 개설 DB 저장 실패로 인한 출금 취소");
                    
                    switch (bankCode) {
                        case "HANA":
                            hanaBankClient.createDepositTransaction(compensationRequest);
                            break;
                        case "KOOKMIN":
                            kookminBankClient.createDepositTransaction(compensationRequest);
                            break;
                        case "SHINHAN":
                            shinhanBankClient.createDepositTransaction(compensationRequest);
                            break;
                    }
                    
                    log.info("[{}] 보상 트랜잭션 1 완료 - 연결 주계좌 출금 취소, 계좌: {}, 금액: {}원", 
                            bankCode, request.getLinkedMainAccount(), initialDepositAmount);
                    
                    // 보상 2: 하나은행 IRP 계좌 삭제 요청
                    try {
                        Map<String, Object> deleteRequest = new HashMap<>();
                        deleteRequest.put("accountNumber", realAccountNumber);
                        deleteRequest.put("reason", "DB 저장 실패로 인한 계좌 삭제");
                        
                        hanaBankClient.deleteIrpAccount(deleteRequest);
                        log.info("[하나은행] 보상 트랜잭션 2 완료 - IRP 계좌 삭제, 계좌번호: {}", realAccountNumber);
                        
                    } catch (Exception deleteError) {
                        log.error("[하나은행] IRP 계좌 삭제 실패 - 수동 확인 필요! 계좌번호: {}, 오류: {}", 
                                realAccountNumber, deleteError.getMessage());
                    }
                    
                } catch (Exception compensationError) {
                    log.error("보상 트랜잭션 실패 - 수동 확인 필요! 은행: {}, 계좌: {}, IRP계좌: {}, 금액: {}원, 오류: {}", 
                            bankCode, request.getLinkedMainAccount(), realAccountNumber, initialDepositAmount, compensationError.getMessage());
                }
                
                // @Transactional에 의해 DB는 자동 롤백되고, 예외를 다시 던짐
                throw new Exception("DB 저장 중 오류 발생: " + dbError.getMessage());
            }

        } catch (Exception e) {
            log.error("IRP 계좌 개설 실패 - 고객 CI: {}, 오류: {}", request.getCustomerCi(), e.getMessage(), e);
            // @Transactional에 의해 자동으로 롤백됨
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

            // 사용자 정보 조회 (고객 정보를 하나은행에 전달하기 위함)
            User user = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getCustomerId()));

            // 하나은행 서버 요청 데이터 구성
            Map<String, Object> hanaBankRequest = new HashMap<>();
            hanaBankRequest.put("customerCi", request.getCustomerCi());
            hanaBankRequest.put("customerName", user.getUserName()); // 고객 이름
            hanaBankRequest.put("birthDate", user.getBirthDate().toString().replace("-", "")); // YYYYMMDD 형식
            hanaBankRequest.put("gender", user.getGender().toString()); // M/F
            hanaBankRequest.put("phone", user.getPhoneNumber()); // 전화번호
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
            
            // 1. HanaInPlan DB: 연결 주계좌에 출금(-) 거래내역 생성
            Transaction withdrawalTransaction = Transaction.builder()
                        .transactionNumber(transactionNumber + "_OUT")
                        .fromAccountId(linkedAccount.getAccountId())
                        .toAccountId(null)
                        .transactionType(Transaction.TransactionType.TRANSFER)
                        .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                        .amount(initialDepositAmount)
                        .balanceAfter(linkedAccount.getBalance())
                        .transactionDirection(Transaction.TransactionDirection.DEBIT)
                        .description("IRP 계좌 개설 초기 입금")
                        .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                        .transactionDate(LocalDateTime.now())
                        .processedDate(LocalDateTime.now())
                        .referenceNumber("IRP_OPEN_" + irpAccountNumber)
                        .memo("IRP 계좌(" + irpAccountNumber + ") 개설")
                        .build();
            
            transactionRepository.save(withdrawalTransaction);
            log.info("[HanaInPlan] 연결 주계좌 출금 거래내역 생성 완료 - 계좌: {}, 금액: {}", 
                    linkedAccount.getAccountNumber(), initialDepositAmount);
            
            // 2. HanaInPlan DB: IRP 계좌에 입금(+) 거래내역 생성
            Transaction irpDepositTransaction = Transaction.builder()
                    .transactionNumber(transactionNumber + "_IN")
                    .fromAccountId(null)
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
            log.info("[HanaInPlan] IRP 계좌 입금 거래내역 생성 완료 - 계좌: {}, 금액: {}", irpAccountNumber, initialDepositAmount);
            
            // 3. 하나은행 서버에 입금 거래내역 생성 (IRP 계좌)
            // 출금은 이미 openIrpAccount 메서드에서 처리되었으므로 입금만 처리
            createDepositTransactionAtHanaBank(irpAccountNumber, initialDepositAmount, "IRP 계좌 개설 초기 입금");
            
        } catch (Exception e) {
            log.error("IRP 계좌 개설 거래내역 생성 실패", e);
            // 거래내역 생성 실패는 계좌 개설 자체를 실패시키지 않음
        }
    }

    /**
     * 하나은행 서버에 입금 거래내역 생성
     */
    private void createDepositTransactionAtHanaBank(String accountNumber, BigDecimal amount, String description) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("amount", amount);
            request.put("description", description);
            
            Map<String, Object> response = hanaBankClient.createDepositTransaction(request);
            log.info("[하나은행] 입금 거래내역 생성 완료 - 계좌: {}, 금액: {}", accountNumber, amount);
            
        } catch (Exception e) {
            log.error("하나은행 서버 입금 거래내역 생성 실패 - 계좌: {}, 오류: {}", accountNumber, e.getMessage());
            // 실패해도 계속 진행
        }
    }

    /**
     * 계좌번호로 은행 코드 판별 (앞 3자리 기준)
     */
    private String determineBankCode(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            log.warn("계좌번호가 null이거나 길이가 3자리 미만입니다: {}", accountNumber);
            return "UNKNOWN";
        }
        
        String prefix = accountNumber.substring(0, 3);
        
        // 은행별 계좌번호 prefix
        // 하나은행: 110, 117, 118, 176
        if (prefix.equals("110") || prefix.equals("117") || prefix.equals("118") || prefix.equals("176")) {
            return "HANA";
        }
        // 국민은행: 004, 040, 060
        else if (prefix.equals("004") || prefix.equals("040") || prefix.equals("060")) {
            return "KOOKMIN";
        }
        // 신한은행: 088, 110, 140
        else if (prefix.equals("088") || prefix.equals("140")) {
            return "SHINHAN";
        }
        else {
            log.warn("알 수 없는 계좌번호 prefix: {} (계좌번호: {})", prefix, accountNumber);
            return "UNKNOWN";
        }
    }

}
