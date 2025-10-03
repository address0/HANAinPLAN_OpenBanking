# 펀드 매수 기능 (Phase 3) 완료 ✅

## 📋 개요

실제 펀드 구조(FundClass 기반)에 맞춘 펀드 매수 기능을 완전히 구현했습니다.

---

## ✅ 완료된 작업

### 1️⃣ 하나은행 서버 (Port 8081)

```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/
├── dto/
│   ├── ✅ FundPurchaseRequestDto.java
│   └── ✅ FundPurchaseResponseDto.java
├── service/
│   └── ✅ FundSubscriptionService.java (매수 로직)
├── controller/
│   └── ✅ FundSubscriptionController.java
└── entity/
    └── ✅ FundSubscription.java (classCode 필드 추가)
```

**API 엔드포인트:**
```
POST  /api/hana/fund-subscription/purchase            - 펀드 매수
GET   /api/hana/fund-subscription/customer/{ci}       - 가입 목록
GET   /api/hana/fund-subscription/customer/{ci}/active - 활성 가입 목록
```

---

### 2️⃣ 하나인플랜 백엔드 (Port 8080)

```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/
├── dto/
│   ├── ✅ FundPurchaseRequestDto.java
│   └── ✅ FundPurchaseResponseDto.java
├── service/
│   └── ✅ FundSubscriptionService.java
└── controller/
    └── ✅ FundSubscriptionController.java
```

**API 엔드포인트:**
```
POST  /api/banking/fund-subscription/purchase  - 펀드 매수
```

**HanaBankClient 업데이트:**
- ✅ 3개 펀드 매수/가입 API 메서드 추가

---

## 🎯 핵심 매수 로직

### 1. 매수 프로세스

```
1. 유효성 검증
   - 필수 파라미터 확인
   
2. 펀드 클래스 조회
   - FundClass + FundRules + FundFees 조회
   - 판매 가능 여부 확인
   
3. IRP 계좌 확인
   - 계좌 존재 여부
   - 잔액 확인
   
4. 거래 규칙 확인
   - 최소 투자금액 확인
   - 컷오프 시간 확인 (추후)
   
5. 기준가 조회
   - 당일 기준가 우선
   - 없으면 최신 기준가 사용
   
6. 수수료 계산
   - bp 단위 -> 금액 변환
   - 선취수수료 or 판매보수
   
7. 좌수 계산
   - 좌수 = (매수금액 - 수수료) / 기준가
   - 소수점 6자리까지
   
8. IRP 계좌 출금
   - 잔액 차감
   
9. FundSubscription 저장
   - 신규 or 추가 매수
   
10. 응답 반환
    - 결제일 계산 (T+N)
```

---

## 💡 주요 계산 로직

### 1. 수수료 계산 (bp → 금액)

```java
// bp (basis point) 단위: 45 bp = 0.45%
if (fees.getFrontLoadPct() != null) {
    // 선취 판매수수료
    purchaseFee = purchaseAmount * frontLoadPct;
} else if (fees.getSalesFeeBps() != null) {
    // 판매보수를 수수료로 적용
    BigDecimal salesFeeRate = salesFeeBps / 10000;
    purchaseFee = purchaseAmount * salesFeeRate;
}
```

**예시:**
```
매수금액: 1,000,000원
판매보수: 25 bp = 0.25%
수수료: 1,000,000 × 0.0025 = 2,500원
```

---

### 2. 매수 좌수 계산

```java
// 실투자금 = 매수금액 - 수수료
BigDecimal netAmount = purchaseAmount - purchaseFee;

// 좌수 = 실투자금 / 기준가 (소수점 6자리)
BigDecimal units = netAmount.divide(nav, 6, RoundingMode.DOWN);
```

**예시:**
```
매수금액: 1,000,000원
수수료: 2,500원
실투자금: 997,500원
기준가: 1,013.2100원
매수 좌수: 997,500 / 1,013.21 = 984.519234 좌
```

---

### 3. 추가 매수 로직

```java
if (existingSubscription.isPresent()) {
    // 기존 가입에 추가 매수
    subscription.addPurchase(amount, nav, fee, units);
    
    // 평균 매수 기준가 재계산
    BigDecimal netAmount = purchaseAmount - purchaseFee;
    BigDecimal avgNav = netAmount / purchaseUnits;
}
```

---

### 4. 결제일 계산 (T+N)

```java
// FundRules에서 결제일 조회
int settleDays = rules.getBuySettleDays(); // 예: 2

// 결제일 = 매수일 + N일
LocalDate settlementDate = today.plusDays(settleDays);
// 예: 2025-10-03 + 2일 = 2025-10-05
```

---

## 📊 요청/응답 예시

### 요청 (Request)

```json
POST /api/banking/fund-subscription/purchase

{
  "userId": 1,
  "customerCi": "ABC123...",
  "childFundCd": "51306P",
  "irpAccountNumber": "110-123-456789",
  "purchaseAmount": 1000000
}
```

---

### 응답 (Response) - 성공

```json
{
  "success": true,
  "message": "펀드 매수가 완료되었습니다",
  "subscriptionId": 1,
  "childFundCd": "51306P",
  "fundName": "미래에셋퇴직플랜20증권자투자신탁1호(채권혼합)",
  "classCode": "P",
  "purchaseAmount": 1000000.00,
  "purchaseNav": 1013.2100,
  "purchaseUnits": 984.519234,
  "purchaseFee": 2500.00,
  "purchaseDate": "2025-10-03",
  "settlementDate": "2025-10-05",
  "irpAccountNumber": "110-123-456789",
  "irpBalanceAfter": 4000000.00
}
```

---

### 응답 (Response) - 실패

```json
{
  "success": false,
  "message": "펀드 매수에 실패했습니다",
  "errorMessage": "IRP 계좌 잔액이 부족합니다"
}
```

---

## 🚀 테스트 방법

### 1. 사전 준비

```sql
-- IRP 계좌 잔액 확인
SELECT account_number, balance FROM irp_accounts;

-- 펀드 클래스 확인
SELECT child_fund_cd, class_code FROM fund_class WHERE sale_status = 'ON';

-- 최신 기준가 확인
SELECT child_fund_cd, nav_date, nav FROM fund_nav 
ORDER BY nav_date DESC LIMIT 5;
```

---

### 2. 하나은행 서버 테스트

```bash
# 펀드 매수 (하나은행 직접)
curl -X POST http://localhost:8081/api/hana/fund-subscription/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "customerCi": "test_customer_ci_001",
    "childFundCd": "51306P",
    "irpAccountNumber": "110-001-000001",
    "purchaseAmount": 100000
  }'
```

---

### 3. 하나인플랜 백엔드 테스트 (통합)

```bash
# 펀드 매수 (하나인플랜 -> 하나은행)
curl -X POST http://localhost:8080/api/banking/fund-subscription/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "customerCi": "test_customer_ci_001",
    "childFundCd": "30810C",
    "irpAccountNumber": "110-001-000001",
    "purchaseAmount": 50000
  }'
```

---

### 4. 가입 내역 조회

```bash
# 고객의 펀드 가입 목록 조회
curl http://localhost:8081/api/hana/fund-subscription/customer/test_customer_ci_001

# 활성 가입만 조회
curl http://localhost:8081/api/hana/fund-subscription/customer/test_customer_ci_001/active
```

---

### 5. Swagger UI 테스트

- **하나은행:** http://localhost:8081/swagger-ui/index.html
- **하나인플랜:** http://localhost:8080/swagger-ui/index.html

"Fund Subscription" 섹션에서 매수 API 테스트

---

## 🔒 유효성 검증

### 1. 필수 파라미터 검증
```
✅ userId (하나인플랜만)
✅ customerCi
✅ childFundCd
✅ irpAccountNumber
✅ purchaseAmount > 0
```

### 2. 비즈니스 검증
```
✅ 펀드 클래스 존재 여부
✅ 판매 가능 여부 (saleStatus = "ON")
✅ IRP 계좌 존재 여부
✅ IRP 계좌 잔액 충분 여부
✅ 최소 투자금액 확인
✅ 기준가 존재 여부
```

---

## 🔄 데이터 흐름

```
프론트엔드
    ↓ POST /api/banking/fund-subscription/purchase
하나인플랜 백엔드 (8080)
    ↓ Feign Client
하나은행 서버 (8081)
    ↓
FundClassRepository (펀드 클래스 조회)
    ↓
FundNavRepository (기준가 조회)
    ↓
IrpAccountRepository (IRP 계좌 출금)
    ↓
FundSubscriptionRepository (가입 정보 저장)
    ↓
응답 반환 (FundPurchaseResponseDto)
```

---

## 📝 주요 특징

### 1. 실무 반영
- ✅ bp 단위 수수료 계산
- ✅ 소수점 6자리 좌수 관리
- ✅ 평균 매수 기준가 계산
- ✅ T+N 결제일 자동 계산

### 2. IRP 계좌 연동
- ✅ 실시간 잔액 확인
- ✅ 자동 출금 처리
- ✅ 출금 후 잔액 반환

### 3. 추가 매수 지원
- ✅ 기존 가입에 추가 매수 가능
- ✅ 평균 매수 기준가 재계산
- ✅ 보유 좌수 누적

### 4. 유연한 수수료
- ✅ 선취수수료 지원
- ✅ bp 기반 보수 지원
- ✅ 자동 수수료 계산

---

## 🎯 다음 단계

### 1. 펀드 매도 기능
- [ ] 환매수수료 계산 (90일 이내 0.7% 등)
- [ ] 보유 좌수 차감
- [ ] IRP 계좌 입금

### 2. 평가금액 업데이트
- [ ] 일일 기준가 업데이트 배치
- [ ] 자동 평가금액 계산
- [ ] 수익률 계산

### 3. 통합 포트폴리오
- [ ] 하나인플랜 FundPortfolio 동기화
- [ ] 정기예금 + 펀드 통합 조회
- [ ] 수익률 비교

### 4. 프론트엔드 연동
- [ ] 펀드 매수 화면
- [ ] 클래스 선택 UI
- [ ] 수수료 안내
- [ ] 가입 내역 조회

---

## ✅ 완료 요약

✅ **하나은행 서버**: 매수 DTO + Service + Controller + IRP 연동  
✅ **하나인플랜 백엔드**: 매수 DTO + Service + Controller  
✅ **HanaBankClient**: 3개 매수 API 추가  
✅ **핵심 로직**: 좌수 계산, 수수료 계산, IRP 출금, 결제일 계산  
✅ **추가 매수**: 평균 기준가 재계산 지원  
✅ **데이터 통신**: 완전 동기화  

**Phase 3: 펀드 매수 기능 완료! 🎉**

이제 실제 펀드 클래스를 IRP 계좌로 매수할 수 있습니다!

