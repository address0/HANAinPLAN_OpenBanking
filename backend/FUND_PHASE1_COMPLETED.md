# 펀드 기능 Phase 1 완료 ✅

## 📋 완료된 작업

### 1️⃣ 하나은행 서버 - 펀드 도메인 생성

#### 📁 Entity (엔티티)
```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/entity/
├── FundProduct.java           ✅ 펀드 상품 엔티티
├── FundNavHistory.java        ✅ 펀드 기준가 이력 엔티티
└── FundSubscription.java      ✅ 펀드 가입 정보 엔티티
```

**주요 기능:**
- `FundProduct`: 펀드 상품 정보 (상품코드, 유형, 위험등급, 수수료, 수익률 등)
- `FundNavHistory`: 일별 기준가 이력 관리 (NAV, 변동률, 순자산 등)
- `FundSubscription`: 고객의 펀드 가입/보유 내역 (매수정보, 평가금액, 수익률 등)

#### 📁 Repository (레포지토리)
```
backend/banks/hana/src/main/java/com/hanainplan/hana/fund/repository/
├── FundProductRepository.java         ✅ 펀드 상품 조회
├── FundNavHistoryRepository.java      ✅ 기준가 이력 조회
└── FundSubscriptionRepository.java    ✅ 펀드 가입 내역 조회
```

**주요 쿼리 메서드:**
- 활성 펀드 상품 조회
- 펀드 유형/위험등급별 조회
- 최신 기준가 조회
- 기간별 기준가 이력 조회
- 고객별 펀드 가입 내역 조회

---

### 2️⃣ 하나인플랜 백엔드 - 펀드 도메인 생성

#### 📁 Entity (엔티티)
```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/entity/
├── FundPortfolio.java         ✅ 펀드 포트폴리오 엔티티 (통합 관리)
└── FundTransaction.java       ✅ 펀드 거래 내역 엔티티
```

**주요 기능:**
- `FundPortfolio`: 여러 은행의 펀드를 하나의 포트폴리오로 통합 관리
- `FundTransaction`: 펀드 매수/매도 거래 내역 추적

#### 📁 Repository (레포지토리)
```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/repository/
├── FundPortfolioRepository.java      ✅ 펀드 포트폴리오 조회
└── FundTransactionRepository.java    ✅ 펀드 거래 내역 조회
```

**주요 쿼리 메서드:**
- 사용자별 포트폴리오 조회
- 활성 포트폴리오만 조회
- 총 투자금액/평가금액/수익 합계 조회
- 거래 내역 조회 (기간별, 유형별)

#### 📁 DTO (데이터 전송 객체)
```
backend/hanainplan/src/main/java/com/hanainplan/domain/fund/dto/
├── FundProductDto.java            ✅ 펀드 상품 DTO
├── FundPortfolioDto.java          ✅ 펀드 포트폴리오 DTO
├── FundTransactionDto.java        ✅ 펀드 거래 내역 DTO
├── FundPurchaseRequest.java       ✅ 펀드 매수 요청 DTO
├── FundPurchaseResponse.java      ✅ 펀드 매수 응답 DTO
└── IntegratedPortfolioDto.java    ✅ 통합 포트폴리오 DTO
```

**통합 포트폴리오 기능:**
- 정기예금 + 펀드 + 보험 + 채권을 한눈에 조회
- 상품별 집계 (총 투자금, 평가액, 수익률)
- 상품 비중 계산 (예금 vs 펀드 비율)

---

## 🗄️ 데이터베이스 스키마

### 하나은행 서버

#### 1. `hana_fund_products` (펀드 상품)
```sql
CREATE TABLE hana_fund_products (
    fund_code VARCHAR(20) PRIMARY KEY,
    fund_name VARCHAR(100) NOT NULL,
    fund_type VARCHAR(50) NOT NULL,
    investment_region VARCHAR(50),
    risk_level VARCHAR(20) NOT NULL,
    
    -- 수수료 정보
    sales_fee_rate DECIMAL(5,4),
    management_fee_rate DECIMAL(5,4),
    trust_fee_rate DECIMAL(5,4),
    total_expense_ratio DECIMAL(5,4),
    redemption_fee_rate DECIMAL(5,4),
    
    -- 수익률 정보
    return_1month DECIMAL(10,4),
    return_3month DECIMAL(10,4),
    return_6month DECIMAL(10,4),
    return_1year DECIMAL(10,4),
    return_3year DECIMAL(10,4),
    
    -- 기타
    management_company VARCHAR(100),
    min_investment_amount DECIMAL(15,2),
    is_irp_eligible BOOLEAN,
    description TEXT,
    is_active BOOLEAN,
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

#### 2. `hana_fund_nav_history` (펀드 기준가 이력)
```sql
CREATE TABLE hana_fund_nav_history (
    nav_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL,
    base_date DATE NOT NULL,
    nav DECIMAL(15,4) NOT NULL,
    previous_nav DECIMAL(15,4),
    change_amount DECIMAL(15,4),
    change_rate DECIMAL(10,4),
    total_net_assets DECIMAL(20,2),
    
    created_at TIMESTAMP,
    UNIQUE KEY (fund_code, base_date)
);
```

#### 3. `hana_fund_subscriptions` (펀드 가입 정보)
```sql
CREATE TABLE hana_fund_subscriptions (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_ci VARCHAR(64) NOT NULL,
    irp_account_number VARCHAR(50),
    
    fund_code VARCHAR(20) NOT NULL,
    fund_name VARCHAR(100) NOT NULL,
    
    -- 매수 정보
    purchase_date DATE NOT NULL,
    purchase_nav DECIMAL(15,4) NOT NULL,
    purchase_amount DECIMAL(15,2) NOT NULL,
    purchase_fee DECIMAL(15,2),
    purchase_units DECIMAL(15,6) NOT NULL,
    
    -- 현재 보유 정보
    current_units DECIMAL(15,6) NOT NULL,
    current_nav DECIMAL(15,4),
    current_value DECIMAL(15,2),
    
    -- 수익 정보
    total_return DECIMAL(15,2),
    return_rate DECIMAL(10,4),
    
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### 하나인플랜 백엔드

#### 4. `fund_portfolio` (펀드 포트폴리오)
```sql
CREATE TABLE fund_portfolio (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_ci VARCHAR(64) NOT NULL,
    
    bank_code VARCHAR(20) NOT NULL,
    fund_code VARCHAR(20) NOT NULL,
    fund_name VARCHAR(100) NOT NULL,
    
    -- 매수/보유/수익 정보 (동일)
    purchase_date DATE NOT NULL,
    purchase_amount DECIMAL(15,2) NOT NULL,
    current_units DECIMAL(15,6) NOT NULL,
    current_value DECIMAL(15,2),
    total_return DECIMAL(15,2),
    return_rate DECIMAL(10,4),
    
    irp_account_number VARCHAR(50),
    subscription_id BIGINT,  -- 은행사의 가입 ID 참조
    status VARCHAR(20),
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

#### 5. `fund_transactions` (펀드 거래 내역)
```sql
CREATE TABLE fund_transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    fund_code VARCHAR(20) NOT NULL,
    
    transaction_type VARCHAR(20) NOT NULL,  -- BUY, SELL
    transaction_date TIMESTAMP NOT NULL,
    settlement_date DATE,
    
    nav DECIMAL(15,4) NOT NULL,
    units DECIMAL(15,6) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    fee DECIMAL(15,2),
    
    balance_units DECIMAL(15,6) NOT NULL,
    description TEXT,
    
    created_at TIMESTAMP
);
```

---

## 🎯 다음 단계 (Phase 2)

### 우선순위 1: 펀드 상품 조회 기능
- [ ] `FundProductService` 구현
- [ ] `FundProductController` 구현
- [ ] 펀드 상품 목록/상세 조회 API
- [ ] 유형별/위험등급별 필터링

### 우선순위 2: 샘플 데이터 생성
- [ ] 펀드 상품 샘플 데이터 (10개)
- [ ] 초기 기준가 데이터
- [ ] SQL 삽입 스크립트 작성

### 우선순위 3: 펀드 매수 기능
- [ ] `FundSubscriptionService` 구현 (설계 문서 참고)
- [ ] IRP 계좌 연동
- [ ] 좌수 계산 로직
- [ ] 거래 내역 저장

---

## 💡 핵심 설계 포인트

### 1. **통합 포트폴리오 개념**
- 정기예금, 펀드, 보험, 채권 등 모든 금융상품을 하나의 포트폴리오로 조회
- `IntegratedPortfolioDto`로 통합 통계 제공
- 상품별 비중, 수익률 비교 가능

### 2. **은행 서버 vs 하나인플랜 백엔드 분리**
- **하나은행 서버**: 실제 펀드 상품, 기준가, 가입 정보 관리
- **하나인플랜 백엔드**: 여러 은행의 펀드를 통합 관리 (포트폴리오)
- `subscription_id`로 은행사 데이터 참조

### 3. **펀드 vs 정기예금 차이**
| 항목 | 정기예금 | 펀드 |
|------|---------|------|
| 거래 단위 | 금액 | 좌수 (Units) |
| 수익 구조 | 고정 이자율 | 변동 기준가 (NAV) |
| 일일 업데이트 | ❌ | ✅ (기준가 + 평가금액) |
| 배치 필요 | ❌ | ✅ (기준가 시뮬레이션) |

### 4. **주요 계산 로직**
```java
// 매수 좌수 계산
매수좌수 = (매수금액 - 판매수수료) / 기준가

// 평가금액 계산
평가금액 = 보유좌수 × 현재기준가

// 수익률 계산
수익률 = ((평가금액 - 원금) / 원금) × 100
```

---

## 📝 참고 문서
- `FUND_SERVICE_DESIGN.md`: 전체 설계 문서
- `backend/banks/hana/src/main/java/com/hanainplan/hana/fund/`: 하나은행 서버 펀드 도메인
- `backend/hanainplan/src/main/java/com/hanainplan/domain/fund/`: 하나인플랜 펀드 도메인

---

## ✨ 완료 요약

✅ **하나은행 서버**: 3개 엔티티 + 3개 Repository  
✅ **하나인플랜 백엔드**: 2개 엔티티 + 2개 Repository + 6개 DTO  
✅ **통합 포트폴리오**: IntegratedPortfolioDto로 여러 상품 통합 조회 가능  
✅ **데이터베이스 스키마**: 5개 테이블 설계 완료  

**Phase 1 완료! 🎉**

