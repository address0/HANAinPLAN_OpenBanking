# 실제 펀드 구조 통합 완료 ✅

## 📋 개요

하나은행 서버와 하나인플랜 백엔드 간 실제 펀드 구조 (FundClass 기반) 데이터 송수신을 완료했습니다.

---

## ✅ 완료된 작업

### 1️⃣ 하나은행 서버 (Port 8081)

```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/
├── dto/
│   └── ✅ FundClassDetailDto.java
├── service/
│   └── ✅ FundClassService.java
└── controller/
    └── ✅ FundClassController.java
```

**API 엔드포인트:**
```
GET  /api/hana/fund-classes                         - 판매중인 펀드 클래스 목록
GET  /api/hana/fund-classes/{childFundCd}           - 클래스 상세 조회
GET  /api/hana/fund-classes/master/{fundCd}         - 모펀드의 클래스 목록
GET  /api/hana/fund-classes/asset-type/{assetType}  - 자산 유형별 조회
GET  /api/hana/fund-classes/class-code/{classCode}  - 클래스 코드별 조회 (A/C/P)
GET  /api/hana/fund-classes/max-amount/{maxAmount}  - 최소 투자금액 이하
```

---

### 2️⃣ 하나인플랜 백엔드 (Port 8080)

```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/
├── dto/
│   └── ✅ FundClassDetailDto.java (동일 구조)
├── service/
│   └── ✅ FundClassService.java
└── controller/
    └── ✅ FundClassController.java
```

**API 엔드포인트:**
```
GET  /api/banking/fund-classes                         - 판매중인 펀드 클래스 목록
GET  /api/banking/fund-classes/{childFundCd}           - 클래스 상세 조회
GET  /api/banking/fund-classes/master/{fundCd}         - 모펀드의 클래스 목록
GET  /api/banking/fund-classes/asset-type/{assetType}  - 자산 유형별 조회
GET  /api/banking/fund-classes/class-code/{classCode}  - 클래스 코드별 조회 (A/C/P)
GET  /api/banking/fund-classes/max-amount/{maxAmount}  - 최소 투자금액 이하
```

**HanaBankClient 업데이트:**
- ✅ 6개 펀드 클래스 API 메서드 추가
- ✅ Feign Client 방식으로 하나은행 서버 호출

---

## 📊 DTO 구조

### FundClassDetailDto

```json
{
  "childFundCd": "51306P",
  "classCode": "P",
  "loadType": "UNKNOWN",
  "saleStatus": "ON",
  "fundMaster": {
    "fundCd": "513061",
    "fundName": "미래에셋퇴직플랜20증권자투자신탁1호(채권혼합)",
    "fundGb": 2,
    "assetType": "채권혼합",
    "currency": "KRW",
    "isActive": true
  },
  "rules": {
    "cutoffTime": "15:30:00",
    "navPublishTime": "10:00:00",
    "buySettleDays": 2,
    "redeemSettleDays": 3,
    "minInitialAmount": 10000.00,
    "minAdditional": 1000.00,
    "allowSip": true,
    "allowSwitch": true,
    "redemptionFeeRate": null,
    "redemptionFeeDays": null
  },
  "fees": {
    "mgmtFeeBps": 45,
    "salesFeeBps": 25,
    "trusteeFeeBps": 3,
    "adminFeeBps": 2,
    "totalFeeBps": 75,
    "mgmtFeePercent": 0.0045,
    "salesFeePercent": 0.0025,
    "trusteeFeePercent": 0.0003,
    "adminFeePercent": 0.0002,
    "totalFeePercent": 0.0075
  },
  "latestNav": 1013.2100,
  "latestNavDate": "2025-10-02"
}
```

---

## 🔄 데이터 흐름

```
프론트엔드
    ↓ HTTP Request
하나인플랜 백엔드 (Port 8080)
  /api/banking/fund-classes
    ↓ Feign Client
하나은행 서버 (Port 8081)
  /api/hana/fund-classes
    ↓ JPA Query
데이터베이스 (MySQL)
  fund_master + fund_class + fund_rules + fund_fees + fund_nav
    ↓ Entity -> DTO
FundClassDetailDto
    ↑ Response
하나인플랜 백엔드
    ↑ Response
프론트엔드
```

---

## 🚀 테스트 방법

### 1. 하나은행 서버 테스트

```bash
# 1. 판매중인 펀드 클래스 전체 조회
curl http://localhost:8081/api/hana/fund-classes

# 2. 클래스 상세 조회
curl http://localhost:8081/api/hana/fund-classes/51306P

# 3. 모펀드의 클래스 목록
curl http://localhost:8081/api/hana/fund-classes/master/513061

# 4. 채권혼합 펀드 조회
curl http://localhost:8081/api/hana/fund-classes/asset-type/채권혼합

# 5. P클래스 펀드 조회
curl http://localhost:8081/api/hana/fund-classes/class-code/P

# 6. 50,000원 이하로 투자 가능한 펀드
curl http://localhost:8081/api/hana/fund-classes/max-amount/50000
```

### 2. 하나인플랜 백엔드 테스트 (통합)

```bash
# 1. 판매중인 펀드 클래스 전체 조회 (하나인플랜 -> 하나은행)
curl http://localhost:8080/api/banking/fund-classes

# 2. 클래스 상세 조회
curl http://localhost:8080/api/banking/fund-classes/30810C

# 3. 모펀드의 클래스 목록
curl http://localhost:8080/api/banking/fund-classes/master/308100

# 4. 채권혼합 펀드 조회
curl http://localhost:8080/api/banking/fund-classes/asset-type/채권혼합

# 5. C클래스 펀드 조회
curl http://localhost:8080/api/banking/fund-classes/class-code/C

# 6. 20,000원 이하로 투자 가능한 펀드
curl http://localhost:8080/api/banking/fund-classes/max-amount/20000
```

### 3. Swagger UI 테스트

- **하나은행:** http://localhost:8081/swagger-ui/index.html
- **하나인플랜:** http://localhost:8080/swagger-ui/index.html

"Fund Classes" 섹션에서 모든 API 테스트 가능

---

## 📝 주요 특징

### 1. 완전한 데이터 동기화
- ✅ 하나은행 서버와 하나인플랜 백엔드의 DTO 구조 100% 일치
- ✅ Feign Client를 통한 자동 직렬화/역직렬화
- ✅ 데이터 무결성 보장

### 2. 상세한 펀드 정보 제공
- ✅ 모펀드 + 클래스 정보 통합
- ✅ 거래 규칙 (컷오프 시간, 결제일 등)
- ✅ 세부 수수료 (bp 단위 + % 변환)
- ✅ 최신 기준가 자동 조회

### 3. 다양한 조회 옵션
- ✅ 모펀드 기준 조회
- ✅ 자산 유형별 필터링
- ✅ 클래스 코드별 필터링 (A/C/P)
- ✅ 투자 금액 기준 필터링

---

## 🆚 기존 구조와 비교

### 기존 (Phase 2) - FundProduct

```java
// 단순 구조
FundProduct
- fundCode: "HANA_STOCK_001"
- fundName: "하나 코스피200"
- salesFeeRate: 0.015  // %
- return1year: 54.0
```

**장점:** 간단, 빠른 프로토타이핑  
**단점:** 실무 부족, 클래스 구분 불가

---

### 신규 (실제 구조) - FundClass

```java
// 실무 구조
FundMaster (모펀드)
└─ FundClass (클래스)
   ├─ FundRules (거래 규칙)
   ├─ FundFees (수수료 - bp 단위)
   └─ FundNav (기준가)
```

**장점:** 실무 반영, 정확한 수수료, 상세 규칙  
**단점:** 구조 복잡

---

## 🔄 병행 운영 전략

현재 **두 가지 API 구조를 모두 제공**하고 있습니다:

### 1. 기존 API (샘플/데모용)
```
GET /api/banking/fund-products          - 10개 샘플 펀드
GET /api/banking/fund-products/{code}   - 단순 구조
```

### 2. 실제 API (프로덕션용)
```
GET /api/banking/fund-classes           - 2개 실제 펀드
GET /api/banking/fund-classes/{code}    - 상세 구조
```

**권장 사용:**
- **프론트엔드 초기 개발**: 기존 API 사용 (간단)
- **실제 서비스**: 실제 API 사용 (정확)
- **향후**: 실제 API로 통합

---

## 🎯 다음 단계

### 1. 펀드 매수 기능 (Phase 3)
- [ ] 클래스 선택 기능
- [ ] bp 기반 수수료 계산
- [ ] 컷오프 시간 검증
- [ ] IRP 계좌 연동

### 2. 프론트엔드 통합
- [ ] 펀드 클래스 목록 컴포넌트
- [ ] 모펀드-클래스 계층 구조 UI
- [ ] 수수료 상세 정보 표시
- [ ] 거래 규칙 안내

### 3. 데이터 확장
- [ ] 더 많은 실제 펀드 추가
- [ ] 일일 기준가 업데이트 배치
- [ ] 수익률 계산 로직

---

## ✅ 완료 요약

✅ **하나은행 서버**: FundClassDetailDto + Service + Controller  
✅ **하나인플랜 백엔드**: 동일 DTO + Service + Controller  
✅ **HanaBankClient**: 6개 API 메서드 추가  
✅ **데이터 통신**: Feign Client로 완벽 동기화  
✅ **API**: 6개 조회 엔드포인트 (양쪽 모두)  

**실제 펀드 구조 통합 완료! 🎉**

이제 하나은행과 하나인플랜 간 실제 펀드 데이터를 완벽하게 주고받을 수 있습니다.

