package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.client.*;
import com.hanainplan.domain.banking.dto.*;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.DepositPortfolio;
import com.hanainplan.domain.banking.entity.DepositSubscription;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.DepositPortfolioRepository;
import com.hanainplan.domain.banking.repository.DepositSubscriptionRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepositSubscriptionService {

    private final HanaBankClient hanaBankClient;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final DepositPortfolioRepository depositPortfolioRepository;
    private final DepositSubscriptionRepository depositSubscriptionRepository;

    /**
     * 정기예금 가입
     * - IRP 계좌는 항상 하나은행에만 있으므로, 모든 정기예금 가입은 하나은행 IRP 서버로 요청
     * - bankCode는 추천된 은행의 금리 정보를 표시하기 위한 참고용
     */
    public DepositSubscriptionResponseDto subscribeDeposit(DepositSubscriptionRequestDto request) throws Exception {
        log.info("정기예금 가입 요청 - 사용자 ID: {}, 추천 은행: {}, IRP 계좌: {}", 
                request.getUserId(), request.getBankCode(), request.getIrpAccountNumber());

        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 1. IRP 계좌 조회 (하나인플랜 DB)
            BankingAccount irpAccount = accountRepository.findByAccountNumber(request.getIrpAccountNumber())
                    .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));

            // 2. IRP 계좌 잔액 확인
            if (irpAccount.getBalance().compareTo(request.getSubscriptionAmount()) < 0) {
                throw new RuntimeException("IRP 계좌 잔액이 부족합니다. 현재 잔액: " + 
                        irpAccount.getBalance() + "원, 요청 금액: " + request.getSubscriptionAmount() + "원");
            }

            // 3. 하나은행 IRP 서버로 정기예금 가입 요청 (IRP 출금 포함)
            Map<String, Object> bankRequest = buildBankRequest(request, user);
            
            log.info("하나은행 IRP 정기예금 가입 요청 전송 - 상품코드: {}, 금액: {}원", 
                    request.getDepositCode(), request.getSubscriptionAmount());
            
            Map<String, Object> response = hanaBankClient.subscribeDeposit(bankRequest);
            
            // 4. 응답 파싱
            DepositSubscriptionResponseDto subscriptionResponse = parseResponse(response, request);
            
            // 5. 하나인플랜 DB에 IRP 계좌 출금 거래내역 저장
            String productName = request.getProductName() != null ? request.getProductName() : request.getDepositCode();
            saveIrpWithdrawalTransaction(irpAccount, request.getSubscriptionAmount(), 
                    user.getUserId(), request.getDepositCode(), productName);
            
            // 6. 하나인플랜 DB에 IRP 계좌 잔액 업데이트
            BigDecimal newBalance = irpAccount.getBalance().subtract(request.getSubscriptionAmount());
            irpAccount.setBalance(newBalance);
            accountRepository.save(irpAccount);
            
            log.info("하나인플랜 IRP 계좌 출금 완료 - 계좌: {}, 출금액: {}원, 남은 잔액: {}원", 
                    request.getIrpAccountNumber(), request.getSubscriptionAmount(), newBalance);
            
            // 7. 정기예금 포트폴리오 저장
            saveDepositPortfolio(request, user, subscriptionResponse);
            
            // 8. HANAinPLAN tb_deposit_subscription 테이블에 저장
            saveDepositSubscription(request, user, subscriptionResponse);
            
            return subscriptionResponse;
            
        } catch (Exception e) {
            log.error("정기예금 가입 실패", e);
            throw new Exception("정기예금 가입 실패: " + e.getMessage());
        }
    }

    private Map<String, Object> buildBankRequest(DepositSubscriptionRequestDto request, User user) {
        // 계약 기간(개월)으로 만기일 계산
        LocalDate subscriptionDate = LocalDate.now();
        LocalDate maturityDate = subscriptionDate.plusMonths(request.getContractPeriod());
        
        // 만기 기간 문자열 생성 (6개월, 1년, 2년 등)
        String maturityPeriod = request.getContractPeriod() >= 12 
                ? (request.getContractPeriod() / 12) + "년" 
                : request.getContractPeriod() + "개월";

        // 금리 계산 (HANAinPLAN의 InterestRateService 로직 기반)
        BigDecimal baseRate = calculateBaseRate(request.getBankCode(), maturityPeriod);
        // 우대금리는 사용자가 명시적으로 요청할 때만 적용 (현재는 기본금리만 사용)
        BigDecimal preferentialRate = BigDecimal.ZERO;
        BigDecimal finalRate = baseRate.add(preferentialRate);
        
        log.info("정기예금 금리 계산 - 은행: {}, 만기: {}, 기본금리: {}%, 우대: {}%, 최종: {}%",
                request.getBankCode(), maturityPeriod, baseRate, preferentialRate, finalRate);

        Map<String, Object> req = new HashMap<>();
        req.put("customerCi", user.getCi());
        req.put("customerName", user.getUserName());
        req.put("birthDate", user.getBirthDate().toString().replace("-", ""));
        req.put("gender", user.getGender().toString());
        req.put("phone", user.getPhoneNumber());
        req.put("productCode", request.getDepositCode());
        req.put("accountNumber", request.getLinkedAccountNumber()); // 출금할 연결 주계좌
        req.put("irpAccountNumber", request.getIrpAccountNumber()); // IRP 계좌번호
        req.put("status", "ACTIVE");
        req.put("subscriptionDate", subscriptionDate.toString());
        req.put("maturityDate", maturityDate.toString());
        req.put("contractPeriod", request.getContractPeriod());
        req.put("maturityPeriod", maturityPeriod);
        req.put("rateType", "FIXED");
        req.put("baseRate", baseRate);
        req.put("preferentialRate", preferentialRate);
        req.put("finalAppliedRate", finalRate);
        req.put("interestCalculationBasis", "단리");
        req.put("interestPaymentMethod", "만기일시");
        req.put("contractPrincipal", request.getSubscriptionAmount());
        req.put("currentBalance", request.getSubscriptionAmount());
        req.put("branchName", "본점");
        
        return req;
    }
    
    /**
     * 은행별 기본 금리 계산 (InterestRateService와 동일)
     */
    private BigDecimal calculateBaseRate(String bankCode, String maturityPeriod) {
        Map<String, Map<String, String>> bankRates = new HashMap<>();
        
        // 하나은행 금리
        Map<String, String> hanaRates = new HashMap<>();
        hanaRates.put("6개월", "0.0207");
        hanaRates.put("1년", "0.0240");
        hanaRates.put("2년", "0.0200");
        hanaRates.put("3년", "0.0210");
        hanaRates.put("5년", "0.0202");
        bankRates.put("HANA", hanaRates);
        
        // 국민은행 금리
        Map<String, String> kookminRates = new HashMap<>();
        kookminRates.put("6개월", "0.0203");
        kookminRates.put("1년", "0.0230");
        kookminRates.put("2년", "0.0187");
        kookminRates.put("3년", "0.0197");
        kookminRates.put("5년", "0.0182");
        bankRates.put("KOOKMIN", kookminRates);
        
        // 신한은행 금리
        Map<String, String> shinhanRates = new HashMap<>();
        shinhanRates.put("6개월", "0.0198");
        shinhanRates.put("1년", "0.0233");
        shinhanRates.put("2년", "0.0197");
        shinhanRates.put("3년", "0.0202");
        shinhanRates.put("5년", "0.0205");
        bankRates.put("SHINHAN", shinhanRates);
        
        String rateStr = bankRates.getOrDefault(bankCode, new HashMap<>()).get(maturityPeriod);
        return rateStr != null ? new BigDecimal(rateStr) : new BigDecimal("0.0200"); // 기본 2.0%
    }
    
    /**
     * 우대 금리 계산
     */
    private BigDecimal calculatePreferentialRateByPeriod(Integer contractPeriod) {
        if (contractPeriod == null || contractPeriod < 12) {
            return BigDecimal.ZERO;
        }
        
        // 1년 이상: 0.003 (0.3%p)
        if (contractPeriod >= 12 && contractPeriod < 24) {
            return new BigDecimal("0.003");
        }
        
        // 2년 이상: 0.005 (0.5%p)
        if (contractPeriod >= 24) {
            return new BigDecimal("0.005");
        }
        
        return BigDecimal.ZERO;
    }

    private DepositSubscriptionResponseDto parseResponse(Map<String, Object> body, DepositSubscriptionRequestDto request) {
        Object idObj = body.get("subscriptionId");
        Long id = idObj instanceof Number ? ((Number) idObj).longValue() : null;

        LocalDate subDate = body.get("subscriptionDate") != null ? LocalDate.parse(body.get("subscriptionDate").toString()) : null;
        LocalDate matDate = body.get("maturityDate") != null ? LocalDate.parse(body.get("maturityDate").toString()) : null;

        Object rateObj = body.get("finalAppliedRate");
        BigDecimal rate = rateObj instanceof Number ? BigDecimal.valueOf(((Number) rateObj).doubleValue()) : null;
        
        // 예상 이자 및 만기금액 계산 (단리 계산)
        BigDecimal contractPrincipal = request.getSubscriptionAmount();
        BigDecimal annualRate = rate != null ? rate : BigDecimal.ZERO; // 연이자율 (예: 0.024 = 2.4%)
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 6, java.math.RoundingMode.HALF_UP); // 월이자율
        BigDecimal expectedInterest = contractPrincipal
                .multiply(monthlyRate)
                .multiply(BigDecimal.valueOf(request.getContractPeriod()))
                .setScale(0, java.math.RoundingMode.HALF_UP); // 원 단위로 반올림
        BigDecimal expectedMaturityAmount = contractPrincipal.add(expectedInterest);
        
        // 은행명 매핑
        String bankName = getBankName(request.getBankCode());

        return DepositSubscriptionResponseDto.success(
                id,
                (String) body.get("productCode"),
                (String) body.get("accountNumber"),
                subDate,
                matDate,
                rate,
                contractPrincipal,
                expectedInterest,
                expectedMaturityAmount,
                bankName
        );
    }
    
    /**
     * 은행 코드를 은행명으로 변환
     */
    private String getBankName(String bankCode) {
        switch (bankCode) {
            case "HANA": return "하나은행";
            case "KOOKMIN": return "국민은행";
            case "SHINHAN": return "신한은행";
            default: return "기타은행";
        }
    }
    
    /**
     * 하나인플랜 DB에 IRP 계좌 출금 거래내역 저장
     */
    private void saveIrpWithdrawalTransaction(BankingAccount irpAccount, BigDecimal amount, 
                                               Long userId, String depositCode, String productName) {
        try {
            String transactionNumber = Transaction.generateTransactionNumber();
            BigDecimal balanceAfter = irpAccount.getBalance().subtract(amount);
            
            Transaction transaction = Transaction.builder()
                    .transactionNumber(transactionNumber + "_OUT")
                    .fromAccountId(irpAccount.getAccountId())
                    .toAccountId(null)
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .transactionDirection(Transaction.TransactionDirection.DEBIT)
                    .description("정기예금 가입 - " + productName)
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .transactionDate(LocalDateTime.now())
                    .processedDate(LocalDateTime.now())
                    .referenceNumber("DEPOSIT_" + depositCode)
                    .memo("정기예금(" + depositCode + ") 가입")
                    .build();
            
            transactionRepository.save(transaction);
            
            log.info("[하나인플랜] IRP 계좌 출금 거래내역 저장 완료 - 계좌ID: {}, 금액: {}원", 
                    irpAccount.getAccountId(), amount);
            
        } catch (Exception e) {
            log.error("IRP 계좌 출금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", 
                    irpAccount.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }
    
    /**
     * 정기예금 포트폴리오 저장
     */
    private void saveDepositPortfolio(DepositSubscriptionRequestDto request, User user, 
                                       DepositSubscriptionResponseDto subscriptionResponse) {
        try {
            String portfolioProductName = request.getProductName() != null 
                    ? request.getProductName() 
                    : request.getDepositCode() + " 정기예금";
            
            DepositPortfolio portfolio = DepositPortfolio.builder()
                    .userId(user.getUserId())
                    .customerCi(user.getCi())
                    .bankCode(request.getBankCode())
                    .bankName(getBankName(request.getBankCode()))
                    .productCode(request.getDepositCode())
                    .productName(portfolioProductName)
                    .subscriptionId(subscriptionResponse.getSubscriptionId())
                    .subscriptionDate(subscriptionResponse.getSubscriptionDate())
                    .maturityDate(subscriptionResponse.getMaturityDate())
                    .contractPeriod(request.getContractPeriod())
                    .maturityPeriod(request.getContractPeriod() + "개월")
                    .principalAmount(request.getSubscriptionAmount())
                    .interestRate(subscriptionResponse.getFinalAppliedRate())
                    .expectedInterest(subscriptionResponse.getExpectedInterest())
                    .maturityAmount(subscriptionResponse.getExpectedMaturityAmount())
                    .status("ACTIVE")
                    .irpAccountNumber(request.getIrpAccountNumber())
                    .build();
            
            depositPortfolioRepository.save(portfolio);
            
            log.info("[하나인플랜] 정기예금 포트폴리오 저장 완료 - 사용자ID: {}, 상품코드: {}, 금액: {}원", 
                    user.getUserId(), request.getDepositCode(), request.getSubscriptionAmount());
            
        } catch (Exception e) {
            log.error("정기예금 포트폴리오 저장 실패 - 사용자ID: {}, 오류: {}", 
                    user.getUserId(), e.getMessage());
            throw new RuntimeException("포트폴리오 저장 실패: " + e.getMessage());
        }
    }
    
    /**
     * HANAinPLAN tb_deposit_subscription 테이블에 저장
     */
    private void saveDepositSubscription(DepositSubscriptionRequestDto request, User user, 
                                         DepositSubscriptionResponseDto subscriptionResponse) {
        try {
            DepositSubscription subscription = DepositSubscription.builder()
                    .userId(user.getUserId())
                    .customerCi(user.getCi())
                    .accountNumber(request.getIrpAccountNumber()) // IRP 계좌번호
                    .status("ACTIVE")
                    .subscriptionDate(subscriptionResponse.getSubscriptionDate())
                    .maturityDate(subscriptionResponse.getMaturityDate())
                    .contractPeriod(request.getContractPeriod())
                    .productType(request.getProductType() != null ? request.getProductType() : 0) // 기본값: 일반상품
                    .bankName(getBankName(request.getBankCode()))
                    .bankCode(request.getBankCode())
                    .depositCode(request.getDepositCode())
                    .rate(subscriptionResponse.getFinalAppliedRate())
                    .currentBalance(request.getSubscriptionAmount())
                    .unpaidInterest(BigDecimal.ZERO)
                    .lastInterestCalculationDate(LocalDate.now())
                    .nextInterestPaymentDate(subscriptionResponse.getMaturityDate()) // 만기일에 이자 지급
                    .build();
            
            depositSubscriptionRepository.save(subscription);
            
            log.info("[하나인플랜] 정기예금 가입내역 저장 완료 - 사용자ID: {}, 상품코드: {}, 금액: {}원", 
                    user.getUserId(), request.getDepositCode(), request.getSubscriptionAmount());
            
        } catch (Exception e) {
            log.error("정기예금 가입내역 저장 실패 - 사용자ID: {}, 오류: {}", 
                    user.getUserId(), e.getMessage());
            throw new RuntimeException("가입내역 저장 실패: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface BankSubscriber {
        Map<String, Object> subscribe(Map<String, Object> request);
    }
}
