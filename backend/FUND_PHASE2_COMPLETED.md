# 펀드 기능 Phase 2 완료 ✅

## 📋 완료된 작업

### 1️⃣ 하나은행 서버 - Service & Controller

#### 📁 Service
```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/service/
└── ✅ FundProductService.java
```

**주요 기능:**
- `getAllActiveFundProducts()` - 모든 활성 펀드 상품 조회
- `getFundProductByCode()` - 펀드 코드로 상세 조회
- `getFundProductsByType()` - 유형별 조회 (주식형, 채권형, 혼합형, MMF)
- `getFundProductsByRiskLevel()` - 위험등급별 조회 (1~5)
- `getFundProductsByInvestmentRegion()` - 투자 지역별 조회
- `getIrpEligibleFunds()` - IRP 편입 가능 펀드 조회
- `getTopPerformingFunds()` - 수익률 상위 펀드 조회
- `searchFunds()` - 복합 필터링 조회

#### 📁 Controller
```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/controller/
└── ✅ FundProductController.java
```

**API 엔드포인트:**
```
GET  /api/hana/fund-products                    - 전체 목록
GET  /api/hana/fund-products/{fundCode}         - 상세 조회
GET  /api/hana/fund-products/type/{fundType}    - 유형별 조회
GET  /api/hana/fund-products/risk/{riskLevel}   - 위험등급별 조회
GET  /api/hana/fund-products/region/{region}    - 지역별 조회
GET  /api/hana/fund-products/irp-eligible       - IRP 가능 펀드
GET  /api/hana/fund-products/top-performing     - 수익률 상위
GET  /api/hana/fund-products/search?keyword=    - 펀드명 검색
GET  /api/hana/fund-products/filter?...         - 복합 필터링
```

---

### 2️⃣ 샘플 데이터

#### 📁 SQL 스크립트
```
backend/banks/hana/init_fund_products.sql  ✅
```

**샘플 펀드 상품 10개:**

| 펀드 코드 | 펀드명 | 유형 | 위험등급 | 1년 수익률 |
|-----------|--------|------|---------|-----------|
| HANA_MMF_001 | 하나 MMF 파킹통장 | MMF | 5 (매우 낮음) | 3.0% |
| HANA_BOND_001 | 하나 국공채 펀드 | 채권형 | 4 (낮음) | 9.6% |
| HANA_BOND_002 | 하나 회사채 혼합 펀드 | 채권형 | 4 (낮음) | 14.4% |
| HANA_MIXED_001 | 하나 밸런스 30 | 혼합형 | 3 (보통) | 30.0% |
| HANA_MIXED_002 | 하나 다이나믹 50 | 혼합형 | 3 (보통) | 38.4% |
| HANA_STOCK_001 | 하나 코스피200 인덱스 | 주식형 | 2 (높음) | 54.0% |
| HANA_STOCK_002 | 하나 IT 성장주 펀드 | 주식형 | 2 (높음) | 72.0% |
| HANA_GLOBAL_001 | 하나 미국 S&P500 | 주식형 | 2 (높음) | 66.0% |
| HANA_GLOBAL_002 | 하나 글로벌 테크 펀드 | 주식형 | 1 (매우 높음) | 96.0% |
| HANA_GLOBAL_003 | 하나 신흥국 주식 | 주식형 | 1 (매우 높음) | 84.0% |

**초기 기준가 데이터:**
- 각 펀드별 오늘 기준가 설정
- MMF: 1,002원대 (안정적)
- 채권형: 10,000원대
- 혼합형: 12,000~14,000원대
- 주식형: 15,000~25,000원대 (변동성 높음)

---

### 3️⃣ 하나인플랜 백엔드 - Integration

#### 📁 Client 업데이트
```
backend/hanainplan/src/main/java/com/hanainplan/domain/banking/client/
└── ✅ HanaBankClient.java (펀드 메서드 추가)
```

**추가된 메서드:**
- `getAllFundProducts()` - 하나은행 펀드 목록 조회
- `getFundProduct()` - 하나은행 펀드 상세 조회
- `getFundProductsByType()` - 유형별 조회
- `getFundProductsByRiskLevel()` - 위험등급별 조회
- `getIrpEligibleFunds()` - IRP 가능 펀드 조회
- `getTopPerformingFunds()` - 수익률 상위 조회
- `filterFunds()` - 복합 필터링 조회

#### 📁 Service
```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/service/
└── ✅ FundProductService.java
```

**주요 기능:**
- 하나은행 API 호출 및 데이터 변환
- Map → FundProductDto 변환
- 에러 핸들링 및 로깅

#### 📁 Controller
```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/controller/
└── ✅ FundProductController.java
```

**API 엔드포인트 (하나인플랜):**
```
GET  /api/banking/fund-products                    - 전체 목록
GET  /api/banking/fund-products/{fundCode}         - 상세 조회
GET  /api/banking/fund-products/type/{fundType}    - 유형별 조회
GET  /api/banking/fund-products/risk/{riskLevel}   - 위험등급별 조회
GET  /api/banking/fund-products/irp-eligible       - IRP 가능 펀드
GET  /api/banking/fund-products/top-performing     - 수익률 상위
GET  /api/banking/fund-products/filter?...         - 복합 필터링
```

---

## 🚀 테스트 방법

### 1️⃣ 데이터베이스 초기화

하나은행 MySQL 컨테이너에 접속하여 샘플 데이터 삽입:

```bash
# 1. MySQL 컨테이너 접속
docker exec -it hana-mysql mysql -uroot -proot1234

# 2. 데이터베이스 선택
USE hana_bank;

# 3. SQL 파일 실행 (또는 복사 붙여넣기)
SOURCE /path/to/init_fund_products.sql;

# 또는 호스트에서 직접 실행
docker exec -i hana-mysql mysql -uroot -proot1234 hana_bank < backend/banks/hana/init_fund_products.sql
```

### 2️⃣ API 테스트 (하나은행 서버)

**서버 실행:** `http://localhost:8081`

```bash
# 1. 전체 펀드 목록 조회
curl http://localhost:8081/api/hana/fund-products

# 2. 펀드 상세 조회
curl http://localhost:8081/api/hana/fund-products/HANA_STOCK_001

# 3. 주식형 펀드 조회
curl http://localhost:8081/api/hana/fund-products/type/주식형

# 4. 위험등급 3 (보통) 펀드 조회
curl http://localhost:8081/api/hana/fund-products/risk/3

# 5. IRP 편입 가능 펀드 조회
curl http://localhost:8081/api/hana/fund-products/irp-eligible

# 6. 수익률 상위 펀드 조회
curl http://localhost:8081/api/hana/fund-products/top-performing

# 7. 복합 필터링 (주식형 + IRP 가능)
curl "http://localhost:8081/api/hana/fund-products/filter?fundType=주식형&isIrpEligible=true"
```

### 3️⃣ API 테스트 (하나인플랜 백엔드)

**서버 실행:** `http://localhost:8080`

```bash
# 1. 전체 펀드 목록 조회
curl http://localhost:8080/api/banking/fund-products

# 2. 펀드 상세 조회
curl http://localhost:8080/api/banking/fund-products/HANA_GLOBAL_002

# 3. 채권형 펀드 조회
curl http://localhost:8080/api/banking/fund-products/type/채권형

# 4. 위험등급 2 (높음) 펀드 조회
curl http://localhost:8080/api/banking/fund-products/risk/2

# 5. 복합 필터링 (혼합형 + 위험등급 3 + IRP 가능)
curl "http://localhost:8080/api/banking/fund-products/filter?fundType=혼합형&riskLevel=3&isIrpEligible=true"
```

### 4️⃣ Swagger UI 테스트

브라우저에서 Swagger UI 접속:

- **하나은행:** http://localhost:8081/swagger-ui/index.html
- **하나인플랜:** http://localhost:8080/swagger-ui/index.html

"Fund Products" 섹션에서 API 테스트 가능

---

## 📊 응답 예시

### 펀드 목록 조회 응답
```json
[
  {
    "fundCode": "HANA_STOCK_001",
    "fundName": "하나 코스피200 인덱스",
    "fundType": "주식형",
    "investmentRegion": "국내",
    "riskLevel": "2",
    "salesFeeRate": 0.0150,
    "managementFeeRate": 0.0400,
    "totalExpenseRatio": 0.0430,
    "return1month": 4.5000,
    "return3month": 13.5000,
    "return6month": 27.0000,
    "return1year": 54.0000,
    "return3year": 45.0000,
    "managementCompany": "하나자산운용",
    "minInvestmentAmount": 10000.00,
    "isIrpEligible": true,
    "description": "KOSPI200 지수를 추종하는 인덱스 펀드입니다.",
    "isActive": true
  }
]
```

---

## 🎯 다음 단계 (Phase 3)

### 펀드 매수 기능 구현

- [ ] `FundNavHistoryService` 구현 (기준가 조회)
- [ ] `FundSubscriptionService` 구현 (펀드 매수 로직)
- [ ] IRP 계좌 연동 및 출금 처리
- [ ] 좌수 계산 로직 구현
- [ ] 펀드 포트폴리오 생성
- [ ] 거래 내역 저장
- [ ] `FundSubscriptionController` 구현

**핵심 로직:**
```java
// 매수 좌수 계산
매수좌수 = (매수금액 - 판매수수료) / 당일기준가

// 예시:
// 매수금액: 1,000,000원
// 판매수수료: 1% (10,000원)
// 당일기준가: 10,500원
// 매수좌수: 990,000 / 10,500 = 94.285714좌
```

---

## ✨ Phase 2 완료 요약

✅ **하나은행 서버**: Service + Controller + 샘플 데이터  
✅ **하나인플랜 백엔드**: Client 업데이트 + Service + Controller  
✅ **샘플 데이터**: 10개 펀드 + 초기 기준가  
✅ **API 엔드포인트**: 9개 조회 API 구현  

**Phase 2 완료! 🎉**

이제 펀드 상품 조회 API가 완전히 동작하며, 다음 단계인 펀드 매수 기능 구현을 진행할 수 있습니다.

