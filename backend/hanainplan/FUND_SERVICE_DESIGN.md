# 펀드 서비스 가입 및 수익 창출 비즈니스 로직 설계

## 📋 목차
1. [펀드 vs 정기예금 비교](#1-펀드-vs-정기예금-비교)
2. [데이터베이스 설계](#2-데이터베이스-설계)
3. [펀드 가입 로직](#3-펀드-가입-로직)
4. [수익 창출 로직](#4-수익-창출-로직)
5. [API 설계](#5-api-설계)
6. [배치 프로세스](#6-배치-프로세스)
7. [구현 우선순위](#7-구현-우선순위)

---

## 1. 펀드 vs 정기예금 비교

| 항목 | 정기예금 | 펀드 |
|------|----------|------|
| **수익 구조** | 고정 금리 | 변동 수익률 |
| **원금 보장** | 보장 (예금자보호) | 비보장 (원금 손실 가능) |
| **가격 변동** | 없음 | 매일 기준가 변동 |
| **매수/매도** | 가입/해지 | 매수/매도 (좌수 단위) |
| **수수료** | 없음 또는 낮음 | 판매보수, 운용보수, 환매수수료 |
| **수익 계산** | 이자 = 원금 × 금리 × 기간 | 수익 = (현재기준가 - 매수기준가) × 보유좌수 |
| **만기** | 정해진 만기일 | 만기 없음 (자유 환매) |
| **위험도** | 낮음 | 낮음~높음 (상품별) |

---

## 2. 데이터베이스 설계

### 2.1 펀드 상품 테이블 (`fund_products`)
```sql
CREATE TABLE fund_products (
    fund_code VARCHAR(20) PRIMARY KEY,
    fund_name VARCHAR(100) NOT NULL,
    fund_type VARCHAR(50) NOT NULL COMMENT '펀드 유형 (주식형, 채권형, 혼합형, MMF 등)',
    investment_region VARCHAR(50) COMMENT '투자 지역 (국내, 해외, 글로벌)',
    risk_level VARCHAR(20) NOT NULL COMMENT '위험등급 (1:매우높음, 2:높음, 3:보통, 4:낮음, 5:매우낮음)',
    
    -- 수수료 정보
    sales_fee_rate DECIMAL(5,4) COMMENT '선취판매수수료율 (%)',
    management_fee_rate DECIMAL(5,4) NOT NULL COMMENT '운용보수율 (연 %)',
    trust_fee_rate DECIMAL(5,4) COMMENT '수탁보수율 (연 %)',
    total_expense_ratio DECIMAL(5,4) COMMENT '총보수율 (연 %)',
    redemption_fee_rate DECIMAL(5,4) COMMENT '환매수수료율 (%, 90일 이내 환매 시)',
    
    -- 수익률 정보
    return_1month DECIMAL(10,4) COMMENT '1개월 수익률 (%)',
    return_3month DECIMAL(10,4) COMMENT '3개월 수익률 (%)',
    return_6month DECIMAL(10,4) COMMENT '6개월 수익률 (%)',
    return_1year DECIMAL(10,4) COMMENT '1년 수익률 (%)',
    return_3year DECIMAL(10,4) COMMENT '3년 수익률 (연평균, %)',
    
    -- 기타 정보
    management_company VARCHAR(100) NOT NULL COMMENT '운용사',
    trust_company VARCHAR(100) COMMENT '수탁사',
    min_investment_amount DECIMAL(15,2) DEFAULT 10000 COMMENT '최소 가입금액',
    is_irp_eligible BOOLEAN DEFAULT TRUE COMMENT 'IRP 편입 가능 여부',
    description TEXT COMMENT '상품 설명',
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_fund_type (fund_type),
    INDEX idx_risk_level (risk_level),
    INDEX idx_is_active (is_active)
) COMMENT='펀드 상품 정보';
```

### 2.2 펀드 기준가 이력 테이블 (`fund_nav_history`)
NAV = Net Asset Value (순자산가치, 기준가)

```sql
CREATE TABLE fund_nav_history (
    nav_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL,
    base_date DATE NOT NULL COMMENT '기준일',
    nav DECIMAL(15,4) NOT NULL COMMENT '기준가 (원)',
    previous_nav DECIMAL(15,4) COMMENT '전일 기준가',
    change_amount DECIMAL(15,4) COMMENT '전일 대비 변동액',
    change_rate DECIMAL(10,4) COMMENT '전일 대비 변동률 (%)',
    total_net_assets DECIMAL(20,2) COMMENT '순자산총액 (억원)',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_fund_date (fund_code, base_date),
    INDEX idx_base_date (base_date),
    FOREIGN KEY (fund_code) REFERENCES fund_products(fund_code)
) COMMENT='펀드 기준가 일별 이력';
```

### 2.3 펀드 포트폴리오 테이블 (`fund_portfolios`)
```sql
CREATE TABLE fund_portfolios (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_ci VARCHAR(64) NOT NULL,
    
    -- 펀드 정보
    fund_code VARCHAR(20) NOT NULL,
    fund_name VARCHAR(100) NOT NULL,
    fund_type VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    
    -- 매수 정보
    purchase_date DATE NOT NULL COMMENT '매수일',
    purchase_nav DECIMAL(15,4) NOT NULL COMMENT '매수 기준가',
    purchase_amount DECIMAL(15,2) NOT NULL COMMENT '매수 금액 (원금)',
    purchase_fee DECIMAL(15,2) DEFAULT 0 COMMENT '매수 수수료',
    purchase_units DECIMAL(15,6) NOT NULL COMMENT '매수 좌수',
    
    -- 현재 보유 정보
    current_units DECIMAL(15,6) NOT NULL COMMENT '현재 보유 좌수',
    current_nav DECIMAL(15,4) COMMENT '현재 기준가',
    current_value DECIMAL(15,2) COMMENT '현재 평가금액 (보유좌수 × 현재기준가)',
    
    -- 수익 정보
    total_return DECIMAL(15,2) COMMENT '평가손익 (평가금액 - 원금)',
    return_rate DECIMAL(10,4) COMMENT '수익률 (%)',
    
    -- 수수료 누적
    accumulated_fees DECIMAL(15,2) DEFAULT 0 COMMENT '누적 수수료 (판매수수료 + 운용보수 등)',
    
    -- IRP 연계
    irp_account_number VARCHAR(50) COMMENT 'IRP 계좌번호',
    
    -- 상태
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '상태 (ACTIVE: 보유중, SOLD: 전량매도, PARTIAL_SOLD: 일부매도)',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_customer_ci (customer_ci),
    INDEX idx_fund_code (fund_code),
    INDEX idx_status (status),
    FOREIGN KEY (fund_code) REFERENCES fund_products(fund_code)
) COMMENT='펀드 포트폴리오 (사용자 보유 내역)';
```

### 2.4 펀드 거래 내역 테이블 (`fund_transactions`)
```sql
CREATE TABLE fund_transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    fund_code VARCHAR(20) NOT NULL,
    
    -- 거래 정보
    transaction_type VARCHAR(20) NOT NULL COMMENT '거래 유형 (BUY: 매수, SELL: 매도)',
    transaction_date TIMESTAMP NOT NULL COMMENT '거래일시',
    settlement_date DATE COMMENT '결제일 (보통 T+2)',
    
    -- 매수/매도 정보
    nav DECIMAL(15,4) NOT NULL COMMENT '거래 시점 기준가',
    units DECIMAL(15,6) NOT NULL COMMENT '거래 좌수',
    amount DECIMAL(15,2) NOT NULL COMMENT '거래 금액',
    fee DECIMAL(15,2) DEFAULT 0 COMMENT '거래 수수료',
    
    -- 잔고 정보 (거래 후)
    balance_units DECIMAL(15,6) NOT NULL COMMENT '거래 후 보유 좌수',
    
    -- IRP 연계
    irp_account_number VARCHAR(50) COMMENT 'IRP 계좌번호',
    
    description TEXT COMMENT '거래 설명',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_type (transaction_type),
    FOREIGN KEY (portfolio_id) REFERENCES fund_portfolios(portfolio_id)
) COMMENT='펀드 매수/매도 거래 내역';
```

---

## 3. 펀드 가입 (매수) 로직

### 3.1 프로세스 플로우

```
[프론트엔드]
  └─> 펀드 상품 선택
  └─> 매수 금액 입력 (최소 1만원)
  └─> 매수 요청

[하나인플랜 백엔드]
  1. IRP 계좌 잔액 확인
  2. 최소 가입금액 확인
  3. 당일 기준가 조회
  4. 매수 좌수 계산: (매수금액 - 판매수수료) / 당일기준가
  5. IRP 계좌 출금 처리
  6. IRP 출금 거래내역 저장
  7. 펀드 포트폴리오 생성
  8. 펀드 거래내역 저장
  9. 응답 반환

[은행 서버 - 해당 펀드 운용사]
  (선택) 외부 펀드 판매사 연동 시 추가 프로세스
```

### 3.2 매수 좌수 계산 로직

```java
/**
 * 펀드 매수 좌수 계산
 * 
 * @param purchaseAmount 매수 금액
 * @param nav 당일 기준가
 * @param salesFeeRate 판매수수료율 (예: 0.01 = 1%)
 * @return 매수 좌수
 */
public BigDecimal calculatePurchaseUnits(
    BigDecimal purchaseAmount, 
    BigDecimal nav, 
    BigDecimal salesFeeRate
) {
    // 1. 판매수수료 계산
    BigDecimal salesFee = purchaseAmount
        .multiply(salesFeeRate)
        .setScale(2, RoundingMode.DOWN);
    
    // 2. 실제 투자금액 (매수금액 - 판매수수료)
    BigDecimal netAmount = purchaseAmount.subtract(salesFee);
    
    // 3. 매수 좌수 = 실제 투자금액 / 기준가
    BigDecimal units = netAmount
        .divide(nav, 6, RoundingMode.DOWN); // 좌수는 소수점 6자리까지
    
    return units;
}

// 예시:
// 매수금액: 1,000,000원
// 기준가: 10,500원
// 판매수수료: 1% (10,000원)
// 실투자금: 990,000원
// 매수좌수: 990,000 / 10,500 = 94.285714좌
```

### 3.3 Service 구현 예시

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FundSubscriptionService {

    private final FundProductRepository fundProductRepository;
    private final FundNavHistoryRepository fundNavHistoryRepository;
    private final FundPortfolioRepository fundPortfolioRepository;
    private final FundTransactionRepository fundTransactionRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    /**
     * 펀드 매수
     */
    public FundPurchaseResponse purchaseFund(FundPurchaseRequest request) {
        log.info("펀드 매수 시작 - userId: {}, fundCode: {}, amount: {}원", 
                request.getUserId(), request.getFundCode(), request.getPurchaseAmount());
        
        // 1. 펀드 상품 조회
        FundProduct fund = fundProductRepository.findById(request.getFundCode())
                .orElseThrow(() -> new RuntimeException("펀드 상품을 찾을 수 없습니다"));
        
        // 2. IRP 계좌 조회 및 잔액 확인
        BankingAccount irpAccount = accountRepository
                .findByAccountNumber(request.getIrpAccountNumber())
                .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다"));
        
        if (irpAccount.getBalance().compareTo(request.getPurchaseAmount()) < 0) {
            throw new RuntimeException("IRP 계좌 잔액이 부족합니다");
        }
        
        // 3. 최소 가입금액 확인
        if (request.getPurchaseAmount().compareTo(fund.getMinInvestmentAmount()) < 0) {
            throw new RuntimeException(String.format("최소 가입금액은 %s원입니다", 
                    fund.getMinInvestmentAmount()));
        }
        
        // 4. 당일 기준가 조회
        LocalDate today = LocalDate.now();
        FundNavHistory todayNav = fundNavHistoryRepository
                .findByFundCodeAndBaseDate(request.getFundCode(), today)
                .orElseGet(() -> {
                    // 당일 기준가가 없으면 전일 기준가 사용
                    return fundNavHistoryRepository
                            .findLatestByFundCode(request.getFundCode())
                            .orElseThrow(() -> new RuntimeException("기준가 정보가 없습니다"));
                });
        
        BigDecimal nav = todayNav.getNav();
        
        // 5. 매수 좌수 및 수수료 계산
        BigDecimal salesFeeRate = fund.getSalesFeeRate();
        BigDecimal salesFee = request.getPurchaseAmount()
                .multiply(salesFeeRate)
                .setScale(2, RoundingMode.DOWN);
        
        BigDecimal netAmount = request.getPurchaseAmount().subtract(salesFee);
        BigDecimal units = netAmount.divide(nav, 6, RoundingMode.DOWN);
        
        log.info("매수 계산 - 기준가: {}, 수수료: {}원, 좌수: {}", nav, salesFee, units);
        
        // 6. IRP 계좌 출금 처리
        BigDecimal newBalance = irpAccount.getBalance().subtract(request.getPurchaseAmount());
        irpAccount.setBalance(newBalance);
        accountRepository.save(irpAccount);
        
        // 7. IRP 출금 거래내역 저장
        Transaction irpTransaction = Transaction.builder()
                .transactionNumber(Transaction.generateTransactionNumber() + "_OUT")
                .fromAccountId(irpAccount.getAccountId())
                .transactionType(Transaction.TransactionType.TRANSFER)
                .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                .amount(request.getPurchaseAmount())
                .balanceAfter(newBalance)
                .transactionDirection(Transaction.TransactionDirection.DEBIT)
                .description("펀드 매수 - " + fund.getFundName())
                .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                .transactionDate(LocalDateTime.now())
                .processedDate(LocalDateTime.now())
                .referenceNumber("FUND_" + request.getFundCode())
                .memo("펀드(" + request.getFundCode() + ") 매수")
                .build();
        transactionRepository.save(irpTransaction);
        
        // 8. 펀드 포트폴리오 생성 또는 업데이트
        FundPortfolio portfolio = fundPortfolioRepository
                .findByUserIdAndFundCodeAndStatus(request.getUserId(), request.getFundCode(), "ACTIVE")
                .orElse(FundPortfolio.builder()
                        .userId(request.getUserId())
                        .customerCi(request.getCustomerCi())
                        .fundCode(fund.getFundCode())
                        .fundName(fund.getFundName())
                        .fundType(fund.getFundType())
                        .riskLevel(fund.getRiskLevel())
                        .irpAccountNumber(request.getIrpAccountNumber())
                        .purchaseDate(today)
                        .purchaseNav(nav)
                        .purchaseAmount(BigDecimal.ZERO)
                        .purchaseFee(BigDecimal.ZERO)
                        .purchaseUnits(BigDecimal.ZERO)
                        .currentUnits(BigDecimal.ZERO)
                        .accumulatedFees(BigDecimal.ZERO)
                        .status("ACTIVE")
                        .build());
        
        // 누적 정보 업데이트
        portfolio.setPurchaseAmount(portfolio.getPurchaseAmount().add(request.getPurchaseAmount()));
        portfolio.setPurchaseFee(portfolio.getPurchaseFee().add(salesFee));
        portfolio.setPurchaseUnits(portfolio.getPurchaseUnits().add(units));
        portfolio.setCurrentUnits(portfolio.getCurrentUnits().add(units));
        portfolio.setAccumulatedFees(portfolio.getAccumulatedFees().add(salesFee));
        
        // 평균 매수 기준가 재계산
        BigDecimal avgNav = portfolio.getPurchaseAmount()
                .subtract(portfolio.getPurchaseFee())
                .divide(portfolio.getPurchaseUnits(), 4, RoundingMode.HALF_UP);
        portfolio.setPurchaseNav(avgNav);
        
        FundPortfolio savedPortfolio = fundPortfolioRepository.save(portfolio);
        
        // 9. 펀드 거래내역 저장
        FundTransaction transaction = FundTransaction.builder()
                .portfolioId(savedPortfolio.getPortfolioId())
                .userId(request.getUserId())
                .fundCode(fund.getFundCode())
                .transactionType("BUY")
                .transactionDate(LocalDateTime.now())
                .settlementDate(today.plusDays(2)) // T+2 결제
                .nav(nav)
                .units(units)
                .amount(request.getPurchaseAmount())
                .fee(salesFee)
                .balanceUnits(portfolio.getCurrentUnits())
                .irpAccountNumber(request.getIrpAccountNumber())
                .description(String.format("펀드 매수 - %s (기준가: %s)", fund.getFundName(), nav))
                .build();
        fundTransactionRepository.save(transaction);
        
        log.info("펀드 매수 완료 - portfolioId: {}, 좌수: {}", savedPortfolio.getPortfolioId(), units);
        
        // 10. 응답 생성
        return FundPurchaseResponse.builder()
                .success(true)
                .portfolioId(savedPortfolio.getPortfolioId())
                .fundCode(fund.getFundCode())
                .fundName(fund.getFundName())
                .purchaseAmount(request.getPurchaseAmount())
                .purchaseNav(nav)
                .purchaseUnits(units)
                .salesFee(salesFee)
                .settlementDate(today.plusDays(2))
                .message("펀드 매수가 완료되었습니다")
                .build();
    }
}
```

---

## 4. 수익 창출 로직

### 4.1 수익률 계산 방식

```java
/**
 * 펀드 평가금액 및 수익률 계산
 */
public void updatePortfolioValuation(FundPortfolio portfolio, BigDecimal currentNav) {
    // 1. 현재 평가금액 = 보유좌수 × 현재기준가
    BigDecimal currentValue = portfolio.getCurrentUnits()
            .multiply(currentNav)
            .setScale(2, RoundingMode.DOWN);
    
    // 2. 평가손익 = 평가금액 - (원금 투자금액)
    BigDecimal totalReturn = currentValue.subtract(portfolio.getPurchaseAmount());
    
    // 3. 수익률 = (평가손익 / 원금) × 100
    BigDecimal returnRate = totalReturn
            .divide(portfolio.getPurchaseAmount(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    
    // 4. 포트폴리오 업데이트
    portfolio.setCurrentNav(currentNav);
    portfolio.setCurrentValue(currentValue);
    portfolio.setTotalReturn(totalReturn);
    portfolio.setReturnRate(returnRate);
}

// 예시:
// 매수금액: 1,000,000원 (수수료 포함)
// 매수좌수: 94.285714좌
// 매수기준가: 10,500원
// 현재기준가: 11,200원

// 현재평가금액 = 94.285714 × 11,200 = 1,056,000원
// 평가손익 = 1,056,000 - 1,000,000 = +56,000원
// 수익률 = (56,000 / 1,000,000) × 100 = +5.6%
```

### 4.2 기준가 변동 시뮬레이션

실제로는 외부 API에서 기준가를 받아오지만, 개발/테스트 목적으로 시뮬레이션:

```java
/**
 * 펀드 기준가 시뮬레이션 (일일 변동)
 * 실전에서는 외부 API 또는 증권사 데이터 연동
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundNavSimulationService {
    
    private final FundNavHistoryRepository fundNavHistoryRepository;
    private final Random random = new Random();
    
    /**
     * 펀드 유형별 일일 변동률 범위
     */
    private Map<String, VolatilityRange> volatilityRanges = Map.of(
        "MMF", new VolatilityRange(-0.001, 0.005),        // -0.1% ~ +0.5%
        "채권형", new VolatilityRange(-0.01, 0.01),        // -1% ~ +1%
        "혼합형", new VolatilityRange(-0.02, 0.02),        // -2% ~ +2%
        "주식형", new VolatilityRange(-0.05, 0.05),        // -5% ~ +5%
        "해외주식", new VolatilityRange(-0.07, 0.07)       // -7% ~ +7%
    );
    
    /**
     * 다음날 기준가 생성 (시뮬레이션)
     */
    public void generateNextDayNav(String fundCode, String fundType) {
        // 1. 전일 기준가 조회
        FundNavHistory previousNav = fundNavHistoryRepository
                .findLatestByFundCode(fundCode)
                .orElseThrow();
        
        // 2. 펀드 유형별 변동률 범위
        VolatilityRange range = volatilityRanges.getOrDefault(fundType, 
                new VolatilityRange(-0.03, 0.03));
        
        // 3. 랜덤 변동률 생성 (정규분포 고려)
        double changeRate = range.min + (range.max - range.min) * random.nextDouble();
        
        // 4. 새로운 기준가 계산
        BigDecimal newNav = previousNav.getNav()
                .multiply(BigDecimal.valueOf(1 + changeRate))
                .setScale(4, RoundingMode.HALF_UP);
        
        BigDecimal changeAmount = newNav.subtract(previousNav.getNav());
        
        // 5. 기준가 이력 저장
        FundNavHistory todayNav = FundNavHistory.builder()
                .fundCode(fundCode)
                .baseDate(LocalDate.now())
                .nav(newNav)
                .previousNav(previousNav.getNav())
                .changeAmount(changeAmount)
                .changeRate(BigDecimal.valueOf(changeRate * 100))
                .build();
        
        fundNavHistoryRepository.save(todayNav);
        
        log.info("기준가 업데이트 - {}: {} → {} ({:+.2f}%)", 
                fundCode, previousNav.getNav(), newNav, changeRate * 100);
    }
    
    @Data
    @AllArgsConstructor
    static class VolatilityRange {
        double min;
        double max;
    }
}
```

---

## 5. API 설계

### 5.1 펀드 상품 조회
```
GET /api/banking/fund-products
GET /api/banking/fund-products/type/{fundType}
GET /api/banking/fund-products/risk/{riskLevel}
GET /api/banking/fund-products/{fundCode}
```

### 5.2 펀드 매수
```
POST /api/banking/funds/purchase
Request Body:
{
  "userId": 1,
  "fundCode": "KR123456",
  "irpAccountNumber": "110-123-456789",
  "purchaseAmount": 1000000,
  "customerCi": "..."
}

Response:
{
  "success": true,
  "portfolioId": 123,
  "fundName": "하나 KOSPI200 펀드",
  "purchaseAmount": 1000000,
  "purchaseNav": 10500.00,
  "purchaseUnits": 94.285714,
  "salesFee": 10000,
  "settlementDate": "2025-01-04",
  "message": "펀드 매수가 완료되었습니다"
}
```

### 5.3 펀드 매도
```
POST /api/banking/funds/sell
Request Body:
{
  "userId": 1,
  "portfolioId": 123,
  "sellUnits": 50.0,  // 전량매도 시 "ALL"
  "redemptionFeeWaiver": false
}
```

### 5.4 펀드 포트폴리오 조회
```
GET /api/banking/funds/portfolio/user/{userId}
GET /api/banking/funds/portfolio/{portfolioId}
GET /api/banking/funds/portfolio/summary/{userId}
```

### 5.5 펀드 기준가 조회
```
GET /api/banking/funds/nav/{fundCode}
GET /api/banking/funds/nav/{fundCode}/history?days=30
```

---

## 6. 배치 프로세스

### 6.1 일일 배치 (매일 오후 6시)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class FundDailyBatchJob {
    
    private final FundNavSimulationService fundNavSimulationService;
    private final FundPortfolioRepository fundPortfolioRepository;
    private final FundProductRepository fundProductRepository;
    private final FundNavHistoryRepository fundNavHistoryRepository;
    
    /**
     * 1. 일일 기준가 업데이트 (시뮬레이션 또는 외부 API)
     */
    @Scheduled(cron = "0 0 18 * * *") // 매일 오후 6시
    public void updateDailyNav() {
        log.info("=== 일일 기준가 업데이트 시작 ===");
        
        List<FundProduct> activeFunds = fundProductRepository.findByIsActiveTrue();
        
        for (FundProduct fund : activeFunds) {
            try {
                // 시뮬레이션으로 기준가 생성
                fundNavSimulationService.generateNextDayNav(
                        fund.getFundCode(), 
                        fund.getFundType()
                );
            } catch (Exception e) {
                log.error("기준가 업데이트 실패 - {}", fund.getFundCode(), e);
            }
        }
        
        log.info("=== 일일 기준가 업데이트 완료 ===");
    }
    
    /**
     * 2. 모든 포트폴리오 평가금액 업데이트
     */
    @Scheduled(cron = "0 30 18 * * *") // 매일 오후 6시 30분
    public void updatePortfolioValuations() {
        log.info("=== 포트폴리오 평가금액 업데이트 시작 ===");
        
        List<FundPortfolio> activePortfolios = 
                fundPortfolioRepository.findByStatus("ACTIVE");
        
        LocalDate today = LocalDate.now();
        
        for (FundPortfolio portfolio : activePortfolios) {
            try {
                // 당일 기준가 조회
                FundNavHistory todayNav = fundNavHistoryRepository
                        .findByFundCodeAndBaseDate(portfolio.getFundCode(), today)
                        .orElse(null);
                
                if (todayNav != null) {
                    // 평가금액 및 수익률 계산
                    updatePortfolioValuation(portfolio, todayNav.getNav());
                    fundPortfolioRepository.save(portfolio);
                }
            } catch (Exception e) {
                log.error("포트폴리오 평가 업데이트 실패 - {}", 
                        portfolio.getPortfolioId(), e);
            }
        }
        
        log.info("=== 포트폴리오 평가금액 업데이트 완료 ===");
    }
    
    /**
     * 3. 펀드 수익률 통계 업데이트 (1개월, 3개월, 6개월, 1년)
     */
    @Scheduled(cron = "0 0 19 * * *") // 매일 오후 7시
    public void updateFundReturns() {
        log.info("=== 펀드 수익률 통계 업데이트 시작 ===");
        
        List<FundProduct> activeFunds = fundProductRepository.findByIsActiveTrue();
        LocalDate today = LocalDate.now();
        
        for (FundProduct fund : activeFunds) {
            try {
                // 당일 기준가
                BigDecimal currentNav = fundNavHistoryRepository
                        .findByFundCodeAndBaseDate(fund.getFundCode(), today)
                        .map(FundNavHistory::getNav)
                        .orElse(null);
                
                if (currentNav == null) continue;
                
                // 1개월 전 기준가
                BigDecimal nav1m = fundNavHistoryRepository
                        .findByFundCodeAndBaseDate(fund.getFundCode(), today.minusMonths(1))
                        .map(FundNavHistory::getNav)
                        .orElse(null);
                
                if (nav1m != null) {
                    BigDecimal return1m = currentNav.subtract(nav1m)
                            .divide(nav1m, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    fund.setReturn1month(return1m);
                }
                
                // 3개월, 6개월, 1년도 동일하게 계산...
                
                fundProductRepository.save(fund);
                
            } catch (Exception e) {
                log.error("수익률 통계 업데이트 실패 - {}", fund.getFundCode(), e);
            }
        }
        
        log.info("=== 펀드 수익률 통계 업데이트 완료 ===");
    }
}
```

---

## 7. 구현 우선순위

### Phase 1: 기본 인프라 (1주)
- [ ] 데이터베이스 스키마 생성
- [ ] Entity 클래스 작성
- [ ] Repository 작성
- [ ] 샘플 펀드 상품 데이터 삽입

### Phase 2: 펀드 조회 기능 (3일)
- [ ] 펀드 상품 목록 조회 API
- [ ] 펀드 상세 정보 조회 API
- [ ] 펀드 기준가 조회 API
- [ ] 프론트엔드 펀드 상품 페이지

### Phase 3: 펀드 매수 기능 (1주)
- [ ] 펀드 매수 Service 구현
- [ ] 펀드 매수 Controller 구현
- [ ] IRP 계좌 연동
- [ ] 거래내역 저장
- [ ] 프론트엔드 펀드 매수 UI

### Phase 4: 포트폴리오 관리 (5일)
- [ ] 포트폴리오 조회 API
- [ ] 포트폴리오 상세 조회
- [ ] 포트폴리오 요약 (총 투자금, 평가금액, 수익률)
- [ ] 프론트엔드 포트폴리오 통합 페이지

### Phase 5: 기준가 시뮬레이션 (5일)
- [ ] 기준가 시뮬레이션 Service
- [ ] 일일 기준가 업데이트 배치
- [ ] 포트폴리오 평가금액 업데이트 배치
- [ ] 수익률 통계 업데이트 배치

### Phase 6: 펀드 매도 기능 (1주)
- [ ] 펀드 매도 Service
- [ ] 환매수수료 계산
- [ ] IRP 계좌 입금 처리
- [ ] 프론트엔드 매도 UI

### Phase 7: 펀드 추천 (1주)
- [ ] 위험성향 기반 추천
- [ ] 수익률 기반 추천
- [ ] 프론트엔드 추천 UI

---

## 8. 정기예금 vs 펀드 비교 요약

| 구분 | 정기예금 | 펀드 |
|------|----------|------|
| **데이터 구조** | 단순 (가입 → 만기) | 복잡 (매수 → 보유 → 매도) |
| **수익 계산** | 이자 = 원금 × 금리 × 기간/12 | 평가손익 = (현재기준가 - 매수기준가) × 좌수 |
| **일일 업데이트** | 불필요 | 필요 (기준가, 평가금액) |
| **배치 프로세스** | 없음 | 일일 배치 (기준가 업데이트, 평가금액 계산) |
| **포트폴리오** | 간단 (가입 내역) | 복잡 (보유 좌수, 평가금액, 수익률) |
| **거래 내역** | 간단 (가입, 해지) | 복잡 (매수, 매도, 추가매수) |
| **IRP 연동** | 출금 → 가입 | 출금 → 매수, 매도 → 입금 |

---

## 9. 다음 단계

1. **데이터베이스 스키마 생성 SQL 작성**
2. **Entity 및 Repository 생성**
3. **샘플 펀드 상품 데이터 준비**
4. **펀드 매수 Service 구현**
5. **펀드 포트폴리오 통합 페이지 구현**

이 설계를 기반으로 단계적으로 구현하시면 됩니다! 🚀





