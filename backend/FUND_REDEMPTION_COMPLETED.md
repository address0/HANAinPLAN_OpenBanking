# 펀드 매도(환매) 기능 완료 ✅

## 📋 개요

펀드 매수에 이어 매도(환매) 기능을 완전히 구현했습니다. 환매수수료 계산, 실현 손익 계산, IRP 계좌 입금 등 실무 로직을 모두 반영했습니다.

---

## ✅ 완료된 작업

### 1️⃣ 하나은행 서버 (Port 8081)

```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/
├── dto/
│   ├── ✅ FundRedemptionRequestDto.java
│   └── ✅ FundRedemptionResponseDto.java
├── service/
│   └── ✅ FundSubscriptionService.java (매도 로직 추가)
└── controller/
    └── ✅ FundSubscriptionController.java (매도 API 추가)
```

**신규 API 엔드포인트:**
```
POST  /api/hana/fund-subscription/redeem  - 펀드 매도 (환매)
```

---

### 2️⃣ 하나인플랜 백엔드 (Port 8080)

```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/
├── dto/
│   ├── ✅ FundRedemptionRequestDto.java
│   └── ✅ FundRedemptionResponseDto.java
├── service/
│   └── ✅ FundSubscriptionService.java (매도 메서드 추가)
└── controller/
    └── ✅ FundSubscriptionController.java (매도 API 추가)
```

**신규 API 엔드포인트:**
```
POST  /api/banking/fund-subscription/redeem  - 펀드 매도 (환매)
```

**HanaBankClient 업데이트:**
- ✅ 펀드 매도 API 메서드 추가

---

## 🎯 핵심 매도 로직

### 13단계 매도 프로세스

```
1. 유효성 검증
   - 필수 파라미터 확인
   
2. 가입 정보 조회
   - FundSubscription 조회
   - 소유권 확인
   
3. 매도 가능 여부 확인
   - 활성 상태 확인 (ACTIVE/PARTIAL_SOLD)
   
4. 매도 좌수 결정
   - 전량 매도 or 일부 매도
   - 보유 좌수 초과 확인
   
5. 펀드 클래스 조회
   - 규칙 정보 조회
   
6. 현재 기준가 조회
   - 당일 기준가 우선
   - 없으면 최신 기준가 사용
   
7. 매도 금액 계산
   - 매도 금액 = 좌수 × 기준가
   
8. 환매수수료 계산 ✨
   - 보유 기간 확인
   - 환매수수료 적용 여부 판단
   
9. 실수령액 계산
   - 실수령액 = 매도 금액 - 환매수수료
   
10. 실현 손익 계산 ✨
    - 평균 매수가 계산
    - 실현 손익 및 수익률 계산
    
11. IRP 계좌 입금 처리 ✨
    - 실수령액 입금
    
12. 보유 좌수 차감 및 상태 업데이트
    - SOLD (전량) or PARTIAL_SOLD (일부)
    
13. 응답 반환
    - 결제일 계산 (T+N)
```

---

## 💡 주요 계산 로직

### 1. 환매수수료 계산

```java
// 보유 기간 계산
long holdingDays = ChronoUnit.DAYS.between(purchaseDate, today);

// 환매수수료 적용 기간 확인 (예: 90일)
if (holdingDays < rules.getRedemptionFeeDays()) {
    // 환매수수료 적용
    redemptionFee = sellAmount × redemptionFeeRate;
} else {
    // 환매수수료 면제
    redemptionFee = 0;
}
```

**예시 1 - 90일 이내 매도 (환매수수료 적용)**
```
매수일: 2025-08-01
매도일: 2025-10-03
보유 기간: 63일
환매수수료 기준: 90일 이내 0.7%

매도 금액: 1,000,000원
환매수수료율: 0.7%
환매수수료: 1,000,000 × 0.007 = 7,000원
실수령액: 993,000원
```

**예시 2 - 90일 이후 매도 (환매수수료 면제)**
```
매수일: 2025-06-01
매도일: 2025-10-03
보유 기간: 124일
환매수수료 기준: 90일 이내 0.7%

매도 금액: 1,000,000원
환매수수료: 0원 (면제)
실수령액: 1,000,000원
```

---

### 2. 실현 손익 계산

```java
// 평균 매수가 = 총 투자금 / 총 좌수
BigDecimal avgPurchasePrice = purchaseAmount / purchaseUnits;

// 투자원금 = 평균 매수가 × 매도 좌수
BigDecimal costBasis = avgPurchasePrice × sellUnits;

// 실현 손익 = 매도 금액 - 투자원금 - 환매수수료
BigDecimal profit = sellAmount - costBasis - redemptionFee;

// 실현 수익률 = (실현 손익 / 투자원금) × 100
BigDecimal profitRate = (profit / costBasis) × 100;
```

**예시 - 수익 실현**
```
총 투자금: 1,000,000원
총 매수 좌수: 987.123456좌
평균 매수가: 1,000,000 / 987.123456 = 1,013.04원/좌

매도 좌수: 500좌
매도 기준가: 1,050.00원/좌
매도 금액: 500 × 1,050 = 525,000원

투자원금: 1,013.04 × 500 = 506,520원
환매수수료: 3,675원 (0.7%)
실수령액: 525,000 - 3,675 = 521,325원

실현 손익: 525,000 - 506,520 - 3,675 = 14,805원
실현 수익률: (14,805 / 506,520) × 100 = 2.92%
```

---

### 3. 매도 유형별 처리

#### A. 전량 매도
```java
if (request.isSellAll()) {
    sellUnits = subscription.getCurrentUnits();
    
    // 좌수 차감
    subscription.sellUnits(sellUnits); // currentUnits = 0
    subscription.setStatus("SOLD");    // 상태: 전량 매도
}
```

#### B. 일부 매도
```java
if (sellUnits < subscription.getCurrentUnits()) {
    // 좌수 차감
    subscription.sellUnits(sellUnits);
    subscription.setStatus("PARTIAL_SOLD"); // 상태: 일부 매도
    
    // 남은 좌수로 평가금액 재계산
    subscription.updateValuation(currentNav);
}
```

---

### 4. 결제일 계산 (T+N)

```java
// FundRules에서 환매 결제일 조회
int settleDays = rules.getRedeemSettleDays(); // 예: 3

// 결제일 = 매도일 + N일
LocalDate settlementDate = today.plusDays(settleDays);
// 예: 2025-10-03 + 3일 = 2025-10-06
```

---

## 📊 요청/응답 예시

### 요청 (Request) - 전량 매도

```json
POST /api/banking/fund-subscription/redeem

{
  "userId": 1,
  "customerCi": "ABC123...",
  "subscriptionId": 1,
  "irpAccountNumber": "110-123-456789",
  "sellAll": true
}
```

---

### 요청 (Request) - 일부 매도

```json
POST /api/banking/fund-subscription/redeem

{
  "userId": 1,
  "customerCi": "ABC123...",
  "subscriptionId": 1,
  "irpAccountNumber": "110-123-456789",
  "sellUnits": 500.0,
  "sellAll": false
}
```

---

### 응답 (Response) - 성공

```json
{
  "success": true,
  "message": "펀드 매도가 완료되었습니다",
  "subscriptionId": 1,
  "childFundCd": "51306P",
  "fundName": "미래에셋퇴직플랜20증권자투자신탁1호(채권혼합)",
  "classCode": "P",
  "sellUnits": 500.0,
  "sellNav": 1050.00,
  "sellAmount": 525000.00,
  "redemptionFee": 3675.00,
  "netAmount": 521325.00,
  "profit": 14805.00,
  "profitRate": 2.92,
  "remainingUnits": 487.123456,
  "status": "PARTIAL_SOLD",
  "redemptionDate": "2025-10-03",
  "settlementDate": "2025-10-06",
  "irpAccountNumber": "110-123-456789",
  "irpBalanceAfter": 5521325.00
}
```

---

### 응답 (Response) - 실패

```json
{
  "success": false,
  "message": "펀드 매도에 실패했습니다",
  "errorMessage": "보유 좌수(487.123456)를 초과하여 매도할 수 없습니다"
}
```

---

## 🚀 테스트 방법

### 1. 사전 준비

```sql
-- 보유 중인 펀드 확인
SELECT subscription_id, fund_name, current_units, status
FROM hana_fund_subscriptions
WHERE customer_ci = 'test_customer_ci_001' AND status IN ('ACTIVE', 'PARTIAL_SOLD');

-- 환매수수료 규칙 확인
SELECT child_fund_cd, redemption_fee_rate, redemption_fee_days
FROM fund_rules
WHERE redemption_fee_rate IS NOT NULL;
```

---

### 2. 하나은행 서버 테스트

```bash
# 전량 매도
curl -X POST http://localhost:8081/api/hana/fund-subscription/redeem \
  -H "Content-Type: application/json" \
  -d '{
    "customerCi": "test_customer_ci_001",
    "subscriptionId": 1,
    "irpAccountNumber": "110-001-000001",
    "sellAll": true
  }'

# 일부 매도
curl -X POST http://localhost:8081/api/hana/fund-subscription/redeem \
  -H "Content-Type: application/json" \
  -d '{
    "customerCi": "test_customer_ci_001",
    "subscriptionId": 1,
    "irpAccountNumber": "110-001-000001",
    "sellUnits": 500.0,
    "sellAll": false
  }'
```

---

### 3. 하나인플랜 백엔드 테스트

```bash
# 펀드 매도 (하나인플랜 -> 하나은행)
curl -X POST http://localhost:8080/api/banking/fund-subscription/redeem \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "customerCi": "test_customer_ci_001",
    "subscriptionId": 1,
    "irpAccountNumber": "110-001-000001",
    "sellUnits": 300.0,
    "sellAll": false
  }'
```

---

### 4. Swagger UI 테스트

- **하나은행:** http://localhost:8081/swagger-ui/index.html
- **하나인플랜:** http://localhost:8080/swagger-ui/index.html

"Fund Subscription" 섹션에서 매도 API 테스트

---

## 🔒 유효성 검증

### 1. 필수 파라미터 검증
```
✅ userId (하나인플랜만)
✅ customerCi
✅ subscriptionId
✅ irpAccountNumber
✅ sellUnits > 0 (일부 매도 시)
✅ sellAll (전량 매도 시)
```

### 2. 비즈니스 검증
```
✅ 가입 정보 존재 여부
✅ 소유권 확인 (customerCi 일치)
✅ 활성 상태 확인 (ACTIVE/PARTIAL_SOLD)
✅ 보유 좌수 초과 확인
✅ IRP 계좌 존재 여부
✅ 기준가 존재 여부
```

---

## 🔄 데이터 흐름

```
프론트엔드
    ↓ POST /api/banking/fund-subscription/redeem
하나인플랜 백엔드 (8080)
    ↓ Feign Client
하나은행 서버 (8081)
    ↓
FundSubscriptionRepository (가입 정보 조회)
    ↓
FundClassRepository (펀드 클래스 & 규칙 조회)
    ↓
FundNavRepository (기준가 조회)
    ↓
환매수수료 계산 (보유 기간 체크)
    ↓
실현 손익 계산 (평균 매수가 기반)
    ↓
IrpAccountRepository (IRP 계좌 입금)
    ↓
FundSubscription 업데이트 (좌수 차감, 상태 변경)
    ↓
응답 반환 (FundRedemptionResponseDto)
```

---

## 📝 주요 특징

### 1. 실무 완전 반영
- ✅ 보유 기간 기반 환매수수료 계산
- ✅ 실현 손익 및 수익률 정확 계산
- ✅ 평균 매수가 기반 손익 계산
- ✅ T+N 결제일 자동 계산

### 2. IRP 계좌 연동
- ✅ 실수령액 자동 입금
- ✅ 잔액 업데이트
- ✅ 트랜잭션 보장

### 3. 전량/일부 매도 지원
- ✅ 전량 매도: 상태 SOLD
- ✅ 일부 매도: 상태 PARTIAL_SOLD
- ✅ 남은 좌수로 평가금액 자동 재계산

### 4. 유연한 환매수수료
- ✅ 보유 기간 자동 체크
- ✅ 90일 이내 수수료 적용
- ✅ 90일 이후 수수료 면제

---

## 🎯 매수 vs 매도 비교

| 항목 | 매수 | 매도 |
|------|------|------|
| **API** | `/purchase` | `/redeem` |
| **수수료** | 선취 판매수수료 (bp) | 환매수수료 (보유 기간) |
| **계좌 처리** | IRP 출금 | IRP 입금 |
| **좌수 변동** | 증가 | 감소 |
| **손익** | 평가 손익 | 실현 손익 |
| **상태** | ACTIVE | SOLD/PARTIAL_SOLD |
| **결제일** | T+2 (매수) | T+3 (환매) |

---

## 📈 전체 펀드 기능 요약

### Phase 1 ✅: 엔티티 구조
- FundMaster, FundClass, FundRules, FundFees, FundNav

### Phase 2 ✅: 조회 API
- 6개 조회 엔드포인트 (목록, 상세, 필터링 등)

### Phase 3 ✅: 매수 기능
- 완전한 매수 로직
- IRP 계좌 연동
- bp 기반 수수료
- 추가 매수 지원

### Phase 3-2 ✅: 매도 기능 (지금 완료!)
- 완전한 매도 로직
- 환매수수료 계산
- 실현 손익 계산
- IRP 계좌 입금
- 전량/일부 매도

---

## 🎯 다음 단계

이제 다음 작업을 진행할 수 있습니다:

1. **통합 포트폴리오 구현**
   - 정기예금 + 펀드 통합 조회
   - 전체 수익률 계산
   - 상품 비중 분석

2. **프론트엔드 연동**
   - 펀드 상품 목록 화면
   - 펀드 매수 화면
   - 펀드 매도 화면
   - 포트폴리오 통합 화면

3. **배치 프로세스**
   - 일일 기준가 업데이트
   - 자동 평가금액 계산
   - 수익률 통계

4. **거래 내역 관리**
   - 펀드 매수/매도 내역 저장
   - 거래 내역 조회
   - 거래 통계

---

## ✅ 완료 요약

✅ **하나은행 서버**: 매도 DTO + Service (환매수수료, 실현손익) + Controller  
✅ **하나인플랜 백엔드**: 매도 DTO + Service + Controller  
✅ **HanaBankClient**: 매도 API 추가  
✅ **핵심 로직**: 환매수수료 계산, 실현 손익 계산, IRP 입금, 상태 업데이트  
✅ **전량/일부 매도**: 유연한 매도 지원  
✅ **데이터 통신**: 완전 동기화  

**Phase 3-2: 펀드 매도(환매) 기능 완료! 🎉**

이제 매수부터 매도까지 완전한 펀드 거래 기능이 갖춰졌습니다!

