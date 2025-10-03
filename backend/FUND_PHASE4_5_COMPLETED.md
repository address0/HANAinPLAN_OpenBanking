# 펀드 거래 내역 관리 & 배치 프로세스 완료 ✅

## 📋 개요

**Phase 4: 거래 내역 관리**와 **Phase 5: 배치 프로세스**를 완전히 구현했습니다.

---

## ✅ Phase 4: 거래 내역 관리

### 1️⃣ 하나은행 서버 (Port 8081)

```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/
├── entity/
│   └── ✅ FundTransaction.java (거래 내역 엔티티)
├── repository/
│   └── ✅ FundTransactionRepository.java (9개 조회 메서드)
├── dto/
│   ├── ✅ FundTransactionDto.java
│   └── ✅ FundTransactionStatsDto.java (통계 DTO)
├── service/
│   ├── ✅ FundSubscriptionService.java (매수/매도 시 거래 내역 자동 저장)
│   └── ✅ FundTransactionService.java (거래 내역 조회 서비스)
└── controller/
    └── ✅ FundTransactionController.java (5개 API 엔드포인트)
```

---

### 2️⃣ 하나인플랜 백엔드 (Port 8080)

```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/
├── dto/
│   ├── ✅ FundTransactionDto.java
│   └── ✅ FundTransactionStatsDto.java
├── service/
│   └── ✅ FundTransactionService.java (Feign Client 연동)
└── controller/
    └── ✅ FundTransactionController.java (3개 API 엔드포인트)
```

---

## 🎯 거래 내역 관리 기능

### 1. 자동 거래 내역 저장

**매수 시 자동 저장:**
```java
// FundSubscriptionService.purchaseFund() 메서드 내
FundTransaction transaction = FundTransaction.createPurchase(
    customerCi, subscriptionId, childFundCd, fundName, classCode,
    purchaseDate, settlementDate, nav, units, amount, fee,
    irpAccountNumber, oldBalance, newBalance
);
fundTransactionRepository.save(transaction);
```

**매도 시 자동 저장:**
```java
// FundSubscriptionService.redeemFund() 메서드 내
FundTransaction transaction = FundTransaction.createRedemption(
    customerCi, subscriptionId, childFundCd, fundName, classCode,
    redemptionDate, settlementDate, nav, units, amount, fee,
    profit, profitRate, irpAccountNumber, oldBalance, newBalance
);
fundTransactionRepository.save(transaction);
```

---

### 2. 거래 내역 API 엔드포인트

#### **하나은행 서버 (8081)**
```
GET   /api/hana/fund-transactions/customer/{ci}                  - 고객 전체 거래 내역
GET   /api/hana/fund-transactions/subscription/{id}              - 가입별 거래 내역
GET   /api/hana/fund-transactions/customer/{ci}/date-range       - 기간별 거래 내역
GET   /api/hana/fund-transactions/customer/{ci}/type/{type}      - 유형별 거래 (BUY/SELL)
GET   /api/hana/fund-transactions/customer/{ci}/stats            - 거래 통계
```

#### **하나인플랜 백엔드 (8080)**
```
GET   /api/banking/fund-transactions/customer/{ci}               - 고객 전체 거래 내역
GET   /api/banking/fund-transactions/subscription/{id}           - 가입별 거래 내역
GET   /api/banking/fund-transactions/customer/{ci}/stats         - 거래 통계
```

---

### 3. 거래 통계 정보

```json
{
  "customerCi": "ABC123...",
  "totalPurchaseCount": 15,
  "totalPurchaseAmount": 5000000.00,
  "totalPurchaseFee": 12500.00,
  "totalRedemptionCount": 5,
  "totalRedemptionAmount": 2000000.00,
  "totalRedemptionFee": 7000.00,
  "totalRealizedProfit": 150000.00,
  "totalFees": 19500.00,
  "totalTransactionCount": 20,
  "netCashFlow": 3000000.00
}
```

---

### 4. 거래 내역 엔티티 구조

```
FundTransaction (hana_fund_transactions)
├── transactionId           - 거래 ID (PK)
├── customerCi              - 고객 CI
├── subscriptionId          - 가입 ID
├── childFundCd             - 펀드 클래스 코드
├── fundName                - 펀드명
├── classCode               - 클래스 코드 (A/C/P)
│
├── transactionType         - BUY, SELL, DIVIDEND
├── transactionDate         - 거래일
├── settlementDate          - 결제일 (T+N)
│
├── nav                     - 거래 기준가
├── units                   - 거래 좌수
├── amount                  - 거래 금액
├── fee                     - 수수료
├── feeType                 - PURCHASE_FEE, REDEMPTION_FEE
│
├── profit                  - 실현 손익 (매도 시)
├── profitRate              - 실현 수익률 (매도 시)
│
├── irpAccountNumber        - IRP 계좌번호
├── irpBalanceBefore        - 거래 전 IRP 잔액
├── irpBalanceAfter         - 거래 후 IRP 잔액
│
├── status                  - PENDING, COMPLETED, CANCELLED
└── note                    - 비고
```

---

## ✅ Phase 5: 배치 프로세스

### 1️⃣ 일일 기준가 업데이트 배치

```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/batch/
└── ✅ FundNavBatchService.java
```

**배치 스케줄:**
```java
@Scheduled(cron = "0 0 18 * * *")  // 매일 18:00 실행
public void updateDailyNav() {
    // 1. 모든 판매중인 펀드 클래스 조회
    // 2. 전일 기준가 기반 신규 기준가 계산 (-2% ~ +2% 랜덤)
    // 3. 당일 기준가 저장
    // 4. 평가금액 재계산 호출
}
```

---

### 2️⃣ 평가금액 재계산 배치

```java
@Transactional
public void updateSubscriptionValuations(LocalDate navDate) {
    // 1. 모든 활성 펀드 가입 조회 (ACTIVE, PARTIAL_SOLD)
    // 2. 각 가입의 최신 기준가 조회
    // 3. 평가금액 업데이트 (보유좌수 × 기준가)
    // 4. 수익률 재계산
}
```

---

### 3️⃣ 수동 배치 실행 API

```
POST  /api/hana/fund-batch/update-nav  - 수동 기준가 업데이트 (관리자용)
```

**테스트 방법:**
```bash
curl -X POST http://localhost:8081/api/hana/fund-batch/update-nav
```

---

## 💡 배치 프로세스 동작 방식

### 1. 기준가 계산 로직

```java
// 전일 기준가: 1,013.2100원
// 랜덤 변동률: -2% ~ +2%
// 예: +1.5% 변동

double changeRate = (random.nextDouble() * 4.0 - 2.0) / 100.0;  // -0.02 ~ +0.02
BigDecimal change = previousNav.multiply(BigDecimal.valueOf(changeRate));
BigDecimal newNav = previousNav.add(change);

// 결과: 1,013.2100 × 1.015 = 1,028.3982원
```

---

### 2. 평가금액 재계산 로직

```java
// 보유 좌수: 987.123456좌
// 신규 기준가: 1,028.3982원

// 평가금액 = 보유좌수 × 기준가
currentValue = 987.123456 × 1,028.3982 = 1,015,123.45원

// 평가손익 = 평가금액 - 원금
totalReturn = 1,015,123.45 - 1,000,000 = 15,123.45원

// 수익률 = (평가손익 / 원금) × 100
returnRate = (15,123.45 / 1,000,000) × 100 = 1.51%
```

---

### 3. 배치 실행 흐름

```
18:00 배치 시작
    ↓
1. 판매중인 펀드 클래스 조회 (N개)
    ↓
2. 각 펀드의 전일 기준가 조회
    ↓
3. 신규 기준가 계산 (-2% ~ +2%)
    ↓
4. fund_nav 테이블에 저장
    ↓
5. 활성 펀드 가입 조회 (M건)
    ↓
6. 각 가입의 평가금액 재계산
    ↓
7. hana_fund_subscriptions 업데이트
    ↓
배치 종료 (로그 출력)
```

---

## 📊 배치 로그 예시

```
========== 일일 기준가 업데이트 배치 시작 ==========
업데이트 대상 펀드 클래스: 4개
기준가 업데이트 완료 - 51306P : 1028.3982
기준가 업데이트 완료 - 30810C : 1015.6543
기준가 업데이트 완료 - 51306A : 1045.2100
기준가 업데이트 완료 - 51306C : 1032.8765
일일 기준가 업데이트 완료 - 성공: 4개, 스킵: 0개
========== 펀드 평가금액 재계산 시작 ==========
평가금액 업데이트 대상: 25건
펀드 평가금액 재계산 완료 - 성공: 25건, 실패: 0건
========== 펀드 평가금액 재계산 종료 ==========
========== 일일 기준가 업데이트 배치 종료 ==========
```

---

## 🚀 테스트 방법

### 1. 거래 내역 조회 테스트

```bash
# 고객 전체 거래 내역 조회
curl http://localhost:8081/api/hana/fund-transactions/customer/test_customer_ci_001

# 특정 가입의 거래 내역 조회
curl http://localhost:8081/api/hana/fund-transactions/subscription/1

# 거래 통계 조회
curl http://localhost:8081/api/hana/fund-transactions/customer/test_customer_ci_001/stats

# 기간별 조회
curl "http://localhost:8081/api/hana/fund-transactions/customer/test_customer_ci_001/date-range?startDate=2025-01-01&endDate=2025-12-31"

# 매수 내역만 조회
curl http://localhost:8081/api/hana/fund-transactions/customer/test_customer_ci_001/type/BUY

# 매도 내역만 조회
curl http://localhost:8081/api/hana/fund-transactions/customer/test_customer_ci_001/type/SELL
```

---

### 2. 배치 프로세스 테스트

```bash
# 수동 배치 실행
curl -X POST http://localhost:8081/api/hana/fund-batch/update-nav

# 결과 확인 - 최신 기준가 조회
curl http://localhost:8081/api/hana/fund-classes/51306P

# 평가금액 변동 확인 - 활성 가입 조회
curl http://localhost:8081/api/hana/fund-subscription/customer/test_customer_ci_001/active
```

---

### 3. 데이터베이스 확인

```sql
-- 오늘 생성된 기준가 확인
SELECT * FROM fund_nav WHERE nav_date = CURDATE();

-- 거래 내역 확인
SELECT 
    transaction_type, 
    COUNT(*) as count, 
    SUM(amount) as total_amount,
    SUM(fee) as total_fee
FROM hana_fund_transactions
WHERE customer_ci = 'test_customer_ci_001'
GROUP BY transaction_type;

-- 거래 통계 확인
SELECT 
    COUNT(*) as total_transactions,
    SUM(CASE WHEN transaction_type = 'BUY' THEN amount ELSE 0 END) as total_purchase,
    SUM(CASE WHEN transaction_type = 'SELL' THEN amount ELSE 0 END) as total_redemption,
    SUM(CASE WHEN transaction_type = 'SELL' THEN profit ELSE 0 END) as total_profit,
    SUM(fee) as total_fees
FROM hana_fund_transactions
WHERE customer_ci = 'test_customer_ci_001';
```

---

## 📝 주요 특징

### 거래 내역 관리
- ✅ 매수/매도 시 자동 저장
- ✅ 실시간 IRP 잔액 추적
- ✅ 실현 손익 기록
- ✅ 다양한 조회 옵션 (전체/기간/유형)
- ✅ 통계 정보 제공

### 배치 프로세스
- ✅ 일일 자동 기준가 업데이트 (18:00)
- ✅ 랜덤 변동 (-2% ~ +2%)
- ✅ 자동 평가금액 재계산
- ✅ 수동 배치 실행 지원
- ✅ 상세 로그 출력

---

## 🎯 전체 펀드 기능 현황

| Phase | 기능 | 상태 |
|-------|------|------|
| Phase 1 | 엔티티 구조 (Master, Class, Rules, Fees, NAV) | ✅ |
| Phase 2 | 조회 API (6개 엔드포인트) | ✅ |
| Phase 3 | 매수 기능 | ✅ |
| Phase 3-2 | 매도 기능 | ✅ |
| **Phase 4** | **거래 내역 관리** | **✅ 완료!** |
| **Phase 5** | **배치 프로세스** | **✅ 완료!** |

---

## 🔄 데이터 흐름

### 거래 내역 저장 흐름
```
펀드 매수/매도 실행
    ↓
FundSubscription 저장/업데이트
    ↓
IRP 계좌 출금/입금 처리
    ↓
FundTransaction 자동 생성 ⭐
    ↓
거래 내역 저장 완료
```

### 배치 프로세스 흐름
```
매일 18:00 배치 시작
    ↓
FundClass 조회 (판매중)
    ↓
전일 기준가 조회
    ↓
신규 기준가 계산 & 저장
    ↓
FundSubscription 조회 (활성)
    ↓
평가금액 재계산 & 업데이트 ⭐
    ↓
배치 종료
```

---

## 🎉 다음 단계

이제 완성된 백엔드 API를 프론트엔드에 연동할 수 있습니다:

1. **펀드 상품 목록 화면**
   - 펀드 클래스 목록 표시
   - 필터링 (자산유형, 클래스, 금액)
   - 상세 정보 (수수료, 규칙)

2. **펀드 매수 화면**
   - 클래스 선택
   - IRP 계좌 선택
   - 매수 금액 입력
   - 수수료 계산 표시

3. **펀드 매도 화면**
   - 보유 펀드 목록
   - 전량/일부 매도 선택
   - 예상 수령액 표시
   - 실현 손익 표시

4. **펀드 거래 내역 화면** ⭐
   - 거래 내역 목록
   - 기간별 필터
   - 유형별 필터 (매수/매도)
   - 거래 통계 대시보드

5. **통합 포트폴리오 화면**
   - 정기예금 + 펀드 통합 조회
   - 전체 수익률
   - 상품 비중 차트

---

## ✅ 완료 요약

✅ **거래 내역 관리**: 엔티티 + Repository + Service + Controller + 자동 저장  
✅ **거래 통계**: 매수/매도 통계, 실현 손익, 수수료 합계  
✅ **배치 프로세스**: 일일 기준가 업데이트 + 평가금액 재계산  
✅ **스케줄링**: 매일 18:00 자동 실행  
✅ **수동 실행**: 관리자 API 제공  
✅ **데이터 동기화**: 하나인플랜 백엔드 완전 연동  

**Phase 4 & 5 완료! 🎉**

이제 펀드 기능의 모든 백엔드 구현이 완료되었습니다!

