package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.IrpAccountDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenRequestDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenResponseDto;
import com.hanainplan.domain.banking.dto.IrpAccountStatusResponseDto;
import com.hanainplan.domain.banking.dto.IrpProductDto;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.entity.IrpProduct;
import com.hanainplan.domain.banking.entity.IrpSyncLog;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.banking.repository.IrpProductRepository;
import com.hanainplan.domain.banking.repository.IrpSyncLogRepository;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final IrpProductRepository irpProductRepository;
    private final IrpSyncLogRepository irpSyncLogRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${external.api.hana-bank.base-url:http://localhost:8081}")
    private String hanaBankBaseUrl;

    @Value("${external.api.kookmin-bank.base-url:http://localhost:8082}")
    private String kookminBankBaseUrl;

    @Value("${external.api.shinhan-bank.base-url:http://localhost:8083}")
    private String shinhanBankBaseUrl;

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
            // 3. 하나은행 서버에 IRP 계좌 개설 요청
            IrpAccountOpenResponseDto bankResponse = openIrpAccountAtHanaBank(request);

            if (!bankResponse.isSuccess()) {
                return bankResponse;
            }

            // 4. 하나은행 서버에서 성공적으로 계좌가 생성되었으므로 HANAinPLAN DB에 저장
            IrpAccount account = IrpAccount.builder()
                    .customerId(request.getCustomerId())
                    .customerCi(customerCi)
                    .bankCode("HANA") // 하나은행 계좌이므로 HANA로 설정
                    .accountNumber(bankResponse.getAccountNumber())
                    .accountStatus("ACTIVE")
                    .initialDeposit(BigDecimal.valueOf(request.getInitialDeposit()))
                    .monthlyDeposit(request.getMonthlyDeposit() != null ? BigDecimal.valueOf(request.getMonthlyDeposit()) : BigDecimal.ZERO)
                    .isAutoDeposit(request.getIsAutoDeposit())
                    .depositDay(request.getDepositDay())
                    .investmentStyle(request.getInvestmentStyle())
                    .linkedMainAccount(request.getLinkedMainAccount())
                    .productCode("IRP001") // 기본 상품 코드 설정
                    .productName("하나은행 IRP") // 기본 상품명
                    .openDate(LocalDate.now())
                    .syncStatus("SUCCESS")
                    .externalAccountId(bankResponse.getAccountNumber()) // 은행 계좌번호 저장
                    .externalLastUpdated(LocalDateTime.now())
                    .build();

            // 5. HANAinPLAN 데이터베이스에 저장
            irpAccountRepository.save(account);

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

    // ===== 상품 관리 =====

    @Override
    public List<IrpProductDto> getAllIrpProducts() {
        List<IrpProduct> products = irpProductRepository.findByIsActiveTrueOrderByBankCodeAscProductNameAsc();
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrpProductDto> getBankIrpProducts(String bankCode) {
        List<IrpProduct> products = irpProductRepository.findByBankCodeAndIsActiveTrueOrderByCreatedDateDesc(bankCode);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrpProductDto> getAvailableIrpProducts() {
        List<IrpProduct> products = irpProductRepository.findAvailableProducts();
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public IrpProductDto getIrpProductDetail(Long irpProductId) {
        Optional<IrpProduct> productOpt = irpProductRepository.findById(irpProductId);
        return productOpt.map(this::convertToDto).orElse(null);
    }

    @Override
    public List<IrpProductDto> searchIrpProducts(String keyword) {
        List<IrpProduct> products = irpProductRepository.findByProductNameContainingIgnoreCaseOrderByBankCodeAscProductNameAsc(keyword);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ===== 동기화 관리 =====

    @Override
    @Transactional
    public IrpSyncLog syncAllIrpData() {
        log.info("전체 IRP 데이터 동기화 시작");

        IrpSyncLog syncLog = IrpSyncLog.builder()
                .syncType("FULL")
                .syncTarget("ALL")
                .syncStatus("RUNNING")
                .triggerSource("SCHEDULED")
                .isManual(false)
                .build();

        syncLog = irpSyncLogRepository.save(syncLog);
        syncLog.startSync();

        try {
            // 각 은행 동기화
            syncHanaBankIrpData();
            syncKookminBankIrpData();
            syncShinhanBankIrpData();

            syncLog.completeSync();
            log.info("전체 IRP 데이터 동기화 완료");

        } catch (Exception e) {
            log.error("전체 IRP 데이터 동기화 실패", e);
            syncLog.failSync(e.getMessage(), e.getStackTrace().toString());
        }

        return irpSyncLogRepository.save(syncLog);
    }

    @Override
    @Transactional
    public IrpSyncLog syncBankIrpData(String bankCode) {
        log.info("{} 은행 IRP 데이터 동기화 시작", bankCode);

        IrpSyncLog syncLog = IrpSyncLog.builder()
                .syncType("INCREMENTAL")
                .bankCode(bankCode)
                .syncTarget("ALL")
                .syncStatus("RUNNING")
                .triggerSource("API")
                .isManual(true)
                .build();

        syncLog = irpSyncLogRepository.save(syncLog);
        syncLog.startSync();

        try {
            switch (bankCode.toUpperCase()) {
                case "HANA":
                    syncHanaBankIrpData();
                    break;
                case "KOOKMIN":
                    syncKookminBankIrpData();
                    break;
                case "SHINHAN":
                    syncShinhanBankIrpData();
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 은행 코드: " + bankCode);
            }

            syncLog.completeSync();
            log.info("{} 은행 IRP 데이터 동기화 완료", bankCode);

        } catch (Exception e) {
            log.error("{} 은행 IRP 데이터 동기화 실패", bankCode, e);
            syncLog.failSync(e.getMessage(), e.getStackTrace().toString());
        }

        return irpSyncLogRepository.save(syncLog);
    }

    @Override
    @Transactional
    public IrpSyncLog syncCustomerIrpData(String customerCi) {
        log.info("고객 {} IRP 데이터 동기화 시작", customerCi);

        IrpSyncLog syncLog = IrpSyncLog.builder()
                .syncType("INCREMENTAL")
                .customerCi(customerCi)
                .syncTarget("ACCOUNT")
                .syncStatus("RUNNING")
                .triggerSource("API")
                .isManual(true)
                .build();

        syncLog = irpSyncLogRepository.save(syncLog);
        syncLog.startSync();

        try {
            // 각 은행에서 해당 고객의 IRP 계좌 정보 조회
            syncCustomerFromHanaBank(customerCi);
            syncCustomerFromKookminBank(customerCi);
            syncCustomerFromShinhanBank(customerCi);

            syncLog.completeSync();
            log.info("고객 {} IRP 데이터 동기화 완료", customerCi);

        } catch (Exception e) {
            log.error("고객 {} IRP 데이터 동기화 실패", customerCi, e);
            syncLog.failSync(e.getMessage(), e.getStackTrace().toString());
        }

        return irpSyncLogRepository.save(syncLog);
    }

    @Override
    @Transactional
    public IrpSyncLog syncCustomerIrpDataByCustomerId(Long customerId) {
        log.info("고객 {} IRP 데이터 동기화 시작", customerId);

        IrpSyncLog syncLog = IrpSyncLog.builder()
                .syncType("INCREMENTAL")
                .customerId(customerId)
                .syncTarget("ACCOUNT")
                .syncStatus("RUNNING")
                .triggerSource("API")
                .isManual(true)
                .build();

        syncLog = irpSyncLogRepository.save(syncLog);
        syncLog.startSync();

        try {
            // 각 은행에서 해당 고객의 IRP 계좌 정보 조회
            syncCustomerFromHanaBankByCustomerId(customerId);
            syncCustomerFromKookminBankByCustomerId(customerId);
            syncCustomerFromShinhanBankByCustomerId(customerId);

            syncLog.completeSync();
            log.info("고객 {} IRP 데이터 동기화 완료", customerId);

        } catch (Exception e) {
            log.error("고객 {} IRP 데이터 동기화 실패", customerId, e);
            syncLog.failSync(e.getMessage(), e.getStackTrace().toString());
        }

        return irpSyncLogRepository.save(syncLog);
    }

    @Override
    public IrpSyncLog getSyncLog(Long syncLogId) {
        return irpSyncLogRepository.findById(syncLogId).orElse(null);
    }

    @Override
    public List<IrpSyncLog> getRecentSyncLogs(int limit) {
        return irpSyncLogRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit))
                .getContent();
    }

    @Override
    public Map<String, Object> getSyncStatistics() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);

        List<Object[]> stats = irpSyncLogRepository.getSyncStatistics(since);
        List<Object[]> successRates = irpSyncLogRepository.getSyncSuccessRateByBank(since);

        Map<String, Object> result = new HashMap<>();
        result.put("syncStatistics", stats);
        result.put("successRates", successRates);

        return result;
    }

    @Override
    @Transactional
    public IrpSyncLog retryFailedSync(Long syncLogId) {
        IrpSyncLog failedSync = irpSyncLogRepository.findById(syncLogId)
                .orElseThrow(() -> new IllegalArgumentException("동기화 로그를 찾을 수 없습니다: " + syncLogId));

        if (!failedSync.canRetry()) {
            throw new IllegalStateException("재시도 가능한 횟수를 초과했습니다");
        }

        failedSync.incrementRetry();
        failedSync.startSync();

        try {
            // 실패한 동기화 재시도
            if ("HANA".equals(failedSync.getBankCode())) {
                syncHanaBankIrpData();
            } else if ("KOOKMIN".equals(failedSync.getBankCode())) {
                syncKookminBankIrpData();
            } else if ("SHINHAN".equals(failedSync.getBankCode())) {
                syncShinhanBankIrpData();
            }

            failedSync.completeSync();

        } catch (Exception e) {
            log.error("동기화 재시도 실패", e);
            failedSync.failSync(e.getMessage(), e.getStackTrace().toString());
        }

        return irpSyncLogRepository.save(failedSync);
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
    public Map<String, Object> getIrpProductStatistics() {
        List<Object[]> stats = irpProductRepository.getIrpProductStatisticsByBank();

        Map<String, Object> result = new HashMap<>();
        result.put("productStatistics", stats);

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

    // ===== 은행별 서비스 연동 =====

    @Override
    @Transactional
    public void syncHanaBankIrpData() {
        log.info("하나은행 IRP 데이터 동기화 시작");

        try {
            // 1. 상품 정보 동기화
            syncHanaBankIrpProducts();

            // 2. 계좌 정보 동기화 (최근 변경된 계좌만)
            syncHanaBankIrpAccounts();

            log.info("하나은행 IRP 데이터 동기화 완료");

        } catch (Exception e) {
            log.error("하나은행 IRP 데이터 동기화 실패", e);
            throw e;
        }
    }

    /**
     * 하나은행 IRP 상품 정보 동기화
     */
    private void syncHanaBankIrpProducts() {
        try {
            String url = hanaBankBaseUrl + "/api/v1/irp/products";
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), List.class);

            if (response.getBody() != null) {
                for (Object productData : response.getBody()) {
                    // 상품 정보 처리 로직 (실제 구현 시 IrpProduct 엔터티 업데이트)
                    log.debug("하나은행 상품 동기화: {}", productData);
                }
            }
        } catch (Exception e) {
            log.error("하나은행 상품 동기화 실패", e);
        }
    }

    /**
     * 하나은행 IRP 계좌 정보 동기화
     */
    private void syncHanaBankIrpAccounts() {
        try {
            // 최근 동기화된 계좌들 조회 (동기화 체크포인트 사용)
            String lastSyncTime = getLastSyncCheckpoint("HANA");

            String url = hanaBankBaseUrl + "/api/v1/irp/accounts/changed";
            Map<String, String> params = new HashMap<>();
            if (lastSyncTime != null) {
                params.put("since", lastSyncTime);
            }

            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), List.class);

            if (response.getBody() != null) {
                for (Object accountData : response.getBody()) {
                    // 계좌 정보 동기화 처리 로직
                    log.debug("하나은행 계좌 동기화: {}", accountData);
                    // 실제 구현 시 IrpAccount 엔터티 업데이트
                }
            }
        } catch (Exception e) {
            log.error("하나은행 계좌 동기화 실패", e);
        }
    }

    @Override
    @Transactional
    public void syncKookminBankIrpData() {
        log.info("국민은행 IRP 데이터 동기화 시작");

        try {
            // 국민은행 API 호출
            String url = kookminBankBaseUrl + "/api/v1/irp/products";
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), List.class);

            if (response.getBody() != null) {
                // 상품 정보 처리 로직
                for (Object productData : response.getBody()) {
                    log.debug("국민은행 상품 동기화: {}", productData);
                }
            }

            log.info("국민은행 IRP 데이터 동기화 완료");

        } catch (Exception e) {
            log.error("국민은행 IRP 데이터 동기화 실패", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void syncShinhanBankIrpData() {
        log.info("신한은행 IRP 데이터 동기화 시작");

        try {
            // 신한은행 API 호출
            String url = shinhanBankBaseUrl + "/api/v1/irp/products";
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), List.class);

            if (response.getBody() != null) {
                // 상품 정보 처리 로직
                for (Object productData : response.getBody()) {
                    log.debug("신한은행 상품 동기화: {}", productData);
                }
            }

            log.info("신한은행 IRP 데이터 동기화 완료");

        } catch (Exception e) {
            log.error("신한은행 IRP 데이터 동기화 실패", e);
            throw e;
        }
    }

    // ===== 배치 작업 =====

    @Override
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    @Transactional
    public void dailyIrpSyncBatch() {
        log.info("일일 IRP 동기화 배치 작업 시작");
        syncAllIrpData();
        log.info("일일 IRP 동기화 배치 작업 완료");
    }

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

    private IrpProductDto convertToDto(IrpProduct product) {
        return IrpProductDto.builder()
                .irpProductId(product.getIrpProductId())
                .bankCode(product.getBankCode())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .productType(product.getProductType())
                .managementCompany(product.getManagementCompany())
                .trustCompany(product.getTrustCompany())
                .minimumContribution(product.getMinimumContribution())
                .maximumContribution(product.getMaximumContribution())
                .annualContributionLimit(product.getAnnualContributionLimit())
                .managementFeeRate(product.getManagementFeeRate())
                .trustFeeRate(product.getTrustFeeRate())
                .salesFeeRate(product.getSalesFeeRate())
                .totalFeeRate(product.getTotalFeeRate())
                .investmentOptions(product.getInvestmentOptions())
                .riskLevel(product.getRiskLevel())
                .expectedReturnRate(product.getExpectedReturnRate())
                .guaranteeType(product.getGuaranteeType())
                .guaranteeRate(product.getGuaranteeRate())
                .maturityAge(product.getMaturityAge())
                .earlyWithdrawalPenalty(product.getEarlyWithdrawalPenalty())
                .taxBenefit(product.getTaxBenefit())
                .contributionFrequency(product.getContributionFrequency())
                .contributionMethod(product.getContributionMethod())
                .minimumHoldingPeriod(product.getMinimumHoldingPeriod())
                .autoRebalancing(product.getAutoRebalancing())
                .rebalancingFrequency(product.getRebalancingFrequency())
                .performanceFee(product.getPerformanceFee())
                .performanceFeeThreshold(product.getPerformanceFeeThreshold())
                .fundAllocation(product.getFundAllocation())
                .benchmarkIndex(product.getBenchmarkIndex())
                .description(product.getDescription())
                .precautions(product.getPrecautions())
                .isActive(product.getIsActive())
                .startDate(product.getStartDate())
                .endDate(product.getEndDate())
                .lastSyncDate(product.getLastSyncDate())
                .syncStatus(product.getSyncStatus())
                .externalProductId(product.getExternalProductId())
                .createdDate(product.getCreatedDate())
                .build();
    }

    private HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }

    private void syncCustomerFromHanaBank(String customerCi) {
        // 하나은행에서 특정 고객의 IRP 계좌 정보 조회 및 동기화
        try {
            String url = hanaBankBaseUrl + "/api/v1/irp/account/" + customerCi;
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), Object.class);

            if (response.getBody() != null) {
                // 계좌 정보 처리 로직
                log.debug("하나은행 고객 {} 계좌 동기화: {}", customerCi, response.getBody());
            }

        } catch (Exception e) {
            log.error("하나은행 고객 {} 계좌 동기화 실패", customerCi, e);
        }
    }

    private void syncCustomerFromHanaBankByCustomerId(Long customerId) {
        // 먼저 customerId로 customerCi를 조회한 후 기존 메소드 호출
        try {
            // 여기서는 간단하게 customerId를 문자열로 변환하여 사용 (실제로는 DB에서 customerCi 조회 필요)
            String customerCi = customerId.toString();
            syncCustomerFromHanaBank(customerCi);
        } catch (Exception e) {
            log.error("하나은행 고객 {} 계좌 동기화 실패", customerId, e);
        }
    }

    private void syncCustomerFromKookminBank(String customerCi) {
        // 국민은행에서 특정 고객의 IRP 계좌 정보 조회 및 동기화
        try {
            String url = kookminBankBaseUrl + "/api/v1/irp/account/" + customerCi;
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), Object.class);

            if (response.getBody() != null) {
                log.debug("국민은행 고객 {} 계좌 동기화: {}", customerCi, response.getBody());
            }

        } catch (Exception e) {
            log.error("국민은행 고객 {} 계좌 동기화 실패", customerCi, e);
        }
    }

    private void syncCustomerFromKookminBankByCustomerId(Long customerId) {
        try {
            String customerCi = customerId.toString();
            syncCustomerFromKookminBank(customerCi);
        } catch (Exception e) {
            log.error("국민은행 고객 {} 계좌 동기화 실패", customerId, e);
        }
    }

    private void syncCustomerFromShinhanBank(String customerCi) {
        // 신한은행에서 특정 고객의 IRP 계좌 정보 조회 및 동기화
        try {
            String url = shinhanBankBaseUrl + "/api/v1/irp/account/" + customerCi;
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), Object.class);

            if (response.getBody() != null) {
                log.debug("신한은행 고객 {} 계좌 동기화: {}", customerCi, response.getBody());
            }

        } catch (Exception e) {
            log.error("신한은행 고객 {} 계좌 동기화 실패", customerCi, e);
        }
    }

    private void syncCustomerFromShinhanBankByCustomerId(Long customerId) {
        try {
            String customerCi = customerId.toString();
            syncCustomerFromShinhanBank(customerCi);
        } catch (Exception e) {
            log.error("신한은행 고객 {} 계좌 동기화 실패", customerId, e);
        }
    }

    /**
     * 하나은행 서버에 IRP 계좌 개설 요청
     */
    private IrpAccountOpenResponseDto openIrpAccountAtHanaBank(IrpAccountOpenRequestDto request) throws Exception {
        try {
            log.info("하나은행 서버에 IRP 계좌 개설 요청 - 고객 CI: {}", request.getCustomerCi());

            // 하나은행 서버 요청 데이터 구성 (하나은행 서버의 IrpAccountRequest 형식에 맞춤)
            // 금액을 만원 단위로 변환 (원 → 만원)
            BigDecimal initialDepositInTenThousand = BigDecimal.valueOf(request.getInitialDeposit()).divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
            BigDecimal monthlyDepositInTenThousand = request.getMonthlyDeposit() != null ?
                BigDecimal.valueOf(request.getMonthlyDeposit()).divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

            Map<String, Object> hanaBankRequest = new HashMap<>();
            hanaBankRequest.put("customerCi", request.getCustomerCi());
            hanaBankRequest.put("initialDeposit", initialDepositInTenThousand);
            hanaBankRequest.put("monthlyDeposit", monthlyDepositInTenThousand);
            hanaBankRequest.put("isAutoDeposit", request.getIsAutoDeposit());
            hanaBankRequest.put("depositDay", request.getDepositDay());
            hanaBankRequest.put("investmentStyle", request.getInvestmentStyle());
            hanaBankRequest.put("linkedMainAccount", request.getLinkedMainAccount());
            hanaBankRequest.put("productCode", "IRP001"); // 기본 상품 코드 설정

            log.info("하나은행 IRP 계좌 개설 요청 데이터: {}", hanaBankRequest);

            // 하나은행 서버에 HTTP 요청
            String url = hanaBankBaseUrl + "/api/v1/irp/open";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(hanaBankRequest, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // 하나은행 서버 응답에서 계좌번호 추출
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
            } else {
                log.error("하나은행 서버 응답이 없습니다");
                return IrpAccountOpenResponseDto.failure("하나은행 서버 응답이 없습니다.", "NO_RESPONSE");
            }

        } catch (Exception e) {
            log.error("하나은행 서버에 IRP 계좌 개설 요청 실패", e);
            throw new Exception("하나은행 서버 연동 실패: " + e.getMessage());
        }
    }

    /**
     * 마지막 동기화 체크포인트 조회
     */
    private String getLastSyncCheckpoint(String bankCode) {
        try {
            Optional<IrpSyncLog> lastSync = irpSyncLogRepository.findFirstByBankCodeAndSyncStatusOrderByEndTimeDesc(bankCode, "SUCCESS");
            return lastSync.map(syncLog -> syncLog.getEndTime().toString()).orElse(null);
        } catch (Exception e) {
            log.error("동기화 체크포인트 조회 실패 - 은행 코드: {}", bankCode, e);
            return null;
        }
    }
}
