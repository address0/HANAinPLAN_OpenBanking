# 펀드 프론트엔드 연동 완료 ✅

## 📋 개요

펀드 백엔드 API와 프론트엔드를 완전히 연동했습니다. React + TypeScript 기반으로 5개의 주요 페이지를 구현했습니다.

---

## ✅ 구현된 파일 목록

### 1️⃣ 타입 정의 & API 서비스

```
frontend/hanainplan/src/
├── types/
│   └── ✅ fund.types.ts (모든 펀드 관련 타입 정의)
├── api/
│   └── ✅ fundApi.ts (API 서비스 함수들)
└── App.tsx (라우팅 추가 ✅)
```

---

### 2️⃣ 페이지 컴포넌트

```
frontend/hanainplan/src/pages/
├── ✅ FundList.tsx (펀드 목록)
├── ✅ FundDetail.tsx (펀드 상세)
├── ✅ FundPurchase.tsx (펀드 매수)
├── ✅ FundPortfolio.tsx (펀드 포트폴리오)
└── ✅ FundTransactions.tsx (거래 내역)
```

---

## 🎯 주요 기능

### 1. 펀드 목록 페이지 (`/funds`)

**기능:**
- 판매 중인 펀드 클래스 목록 조회
- 자산 유형별 필터 (혼합형, 채권형, 주식형)
- 클래스 유형별 필터 (P, C, A)
- 투자 가능 금액 필터
- 기준가, 수수료 정보 표시
- 펀드 상세 페이지로 이동

**URL:** `http://localhost:5173/funds`

**주요 코드:**
```typescript
// 데이터 로드
const data = await fundProductApi.getAllFundClasses();

// 필터링
filtered = filtered.filter(
  (fund) => fund.fundMaster.assetType === selectedAssetType
);
```

---

### 2. 펀드 상세 페이지 (`/fund/:fundCode`)

**기능:**
- 펀드 클래스 상세 정보 조회
- 기준가 정보
- 기본 정보 (운용사, 설정일 등)
- 투자 규칙 (최소 금액, 결제일 등)
- 수수료 정보 (판매보수, 운용보수, TER)
- 투자 유의사항
- 매수하기 버튼

**URL:** `http://localhost:5173/fund/51306P`

**주요 코드:**
```typescript
const data = await fundProductApi.getFundClass(fundCode);
setFund(data);
```

---

### 3. 펀드 매수 페이지 (`/fund/:fundCode/purchase`)

**기능:**
- 매수 금액 입력
- 빠른 금액 선택 (1만원 ~ 100만원)
- 예상 수수료 계산
- 예상 매수 좌수 계산
- 결제일 표시 (T+N)
- 투자 유의사항 동의
- 매수 실행

**URL:** `http://localhost:5173/fund/51306P/purchase`

**주요 코드:**
```typescript
const request: FundPurchaseRequest = {
  userId: mockUserId,
  customerCi: mockCustomerCi,
  childFundCd: fund.childFundCd,
  irpAccountNumber: mockIrpAccountNumber,
  purchaseAmount: amount,
};

const response = await fundSubscriptionApi.purchaseFund(request);
```

**계산 로직:**
```typescript
// 수수료 계산
const feeRate = fund.fundFees?.salesFeeBps / 10000;
const fee = amount * feeRate;

// 매수 좌수 계산
const netAmount = amount - fee;
const units = netAmount / fund.latestNav.nav;
```

---

### 4. 펀드 포트폴리오 페이지 (`/fund/portfolio`)

**기능:**
- 보유 중인 펀드 목록 조회
- 전체 포트폴리오 요약
  - 투자 원금
  - 평가 금액
  - 평가 손익
  - 수익률
- 개별 펀드 상세 정보
  - 보유 좌수
  - 현재 기준가
  - 평가 금액
  - 수익률
- 매도하기 버튼

**URL:** `http://localhost:5173/fund/portfolio`

**주요 코드:**
```typescript
const data = await hanaBankFundApi.getActiveSubscriptions(mockCustomerCi);
setSubscriptions(data);

// 포트폴리오 통계 계산
const totalInvestment = subscriptions.reduce((sum, sub) => sum + sub.purchaseAmount, 0);
const totalValue = subscriptions.reduce((sum, sub) => sum + sub.currentValue, 0);
const totalReturn = totalValue - totalInvestment;
const totalReturnRate = (totalReturn / totalInvestment) * 100;
```

---

### 5. 거래 내역 페이지 (`/fund/transactions`)

**기능:**
- 펀드 거래 내역 조회
- 거래 통계
  - 매수: 건수, 금액, 수수료
  - 매도: 건수, 금액, 수수료
  - 손익: 실현 손익, 총 수수료, 순현금흐름
- 거래 유형별 필터 (전체, 매수, 매도)
- 거래 상세 정보
  - 거래일, 결제일
  - 거래 좌수, 기준가
  - 실현 손익 (매도 시)
  - IRP 잔액 변동

**URL:** `http://localhost:5173/fund/transactions`

**주요 코드:**
```typescript
const [transactionsData, statsData] = await Promise.all([
  fundTransactionApi.getCustomerTransactions(mockCustomerCi),
  fundTransactionApi.getTransactionStats(mockCustomerCi),
]);
```

---

## 🎨 UI/UX 특징

### 1. 하나은행 브랜드 컬러
```css
Primary Color: #00857D (하나 그린)
Hover Color: #006D66 (다크 그린)
```

### 2. 반응형 디자인
- 모바일, 태블릿, 데스크톱 대응
- Tailwind CSS Grid 활용

### 3. 인터랙티브 요소
- 로딩 스피너
- 호버 효과
- 트랜지션 애니메이션

### 4. 색상 코딩
```typescript
// 수익률 색상
수익: text-red-600 (빨간색)
손실: text-blue-600 (파란색)
평가액: text-gray-900 (검은색)

// 위험등급 색상
1-2등급: text-blue-600 (낮은 위험)
3-4등급: text-green-600 (중간 위험)
5-6등급: text-red-600 (높은 위험)

// 거래 유형 색상
매수: bg-blue-100 text-blue-800
매도: bg-red-100 text-red-800
분배금: bg-green-100 text-green-800
```

---

## 📊 데이터 플로우

### 페이지별 API 호출

```
FundList
    ↓
fundProductApi.getAllFundClasses()
    ↓
GET http://localhost:8080/api/banking/fund-classes


FundDetail
    ↓
fundProductApi.getFundClass(fundCode)
    ↓
GET http://localhost:8080/api/banking/fund-classes/{fundCode}


FundPurchase
    ↓
fundSubscriptionApi.purchaseFund(request)
    ↓
POST http://localhost:8080/api/banking/fund-subscription/purchase


FundPortfolio
    ↓
hanaBankFundApi.getActiveSubscriptions(customerCi)
    ↓
GET http://localhost:8081/api/hana/fund-subscription/customer/{ci}/active


FundTransactions
    ↓
fundTransactionApi.getCustomerTransactions(customerCi)
fundTransactionApi.getTransactionStats(customerCi)
    ↓
GET http://localhost:8080/api/banking/fund-transactions/customer/{ci}
GET http://localhost:8080/api/banking/fund-transactions/customer/{ci}/stats
```

---

## 🚀 실행 방법

### 1. 백엔드 서버 실행

```bash
# 하나은행 서버 (포트 8081)
cd backend/banks/hana
./gradlew bootRun

# 하나인플랜 백엔드 (포트 8080)
cd backend/hanainplan
./gradlew bootRun
```

---

### 2. 프론트엔드 실행

```bash
cd frontend/hanainplan
npm install
npm run dev
```

프론트엔드: `http://localhost:5173`

---

### 3. 페이지 접속

```bash
# 펀드 목록
http://localhost:5173/funds

# 펀드 상세 (예: 51306P)
http://localhost:5173/fund/51306P

# 펀드 매수
http://localhost:5173/fund/51306P/purchase

# 펀드 포트폴리오
http://localhost:5173/fund/portfolio

# 거래 내역
http://localhost:5173/fund/transactions
```

---

## 🔧 설정 정보

### API 엔드포인트

```typescript
// fundApi.ts
const API_BASE_URL = 'http://localhost:8080/api/banking';
const HANA_BANK_URL = 'http://localhost:8081/api/hana';
```

### 임시 사용자 정보

```typescript
// 각 페이지에서 사용 (실제로는 로그인 정보에서 가져와야 함)
const mockUserId = 1;
const mockCustomerCi = 'test_customer_ci_001';
const mockIrpAccountNumber = '110-001-000001';
```

---

## 📱 화면 흐름

### 사용자 시나리오

```
1. 펀드 목록 페이지 접속
   ↓
2. 필터로 원하는 펀드 검색
   ↓
3. 펀드 클릭 → 상세 페이지
   ↓
4. "매수하기" 버튼 클릭
   ↓
5. 금액 입력 및 동의
   ↓
6. 매수 완료
   ↓
7. 포트폴리오에서 확인
   ↓
8. 거래 내역에서 확인
```

---

## 🎯 주요 컴포넌트

### 공통 기능

```typescript
// 숫자 포맷팅
const formatNumber = (num: number | null | undefined) => {
  if (num === null || num === undefined) return '-';
  return num.toLocaleString('ko-KR');
};

// 퍼센트 포맷팅 (bp → %)
const formatPercent = (bps: number | null | undefined) => {
  if (bps === null || bps === undefined) return '-';
  return `${(bps / 100).toFixed(2)}%`;
};

// 수익률 색상
const getReturnColor = (returnRate: number) => {
  if (returnRate > 0) return 'text-red-600';
  if (returnRate < 0) return 'text-blue-600';
  return 'text-gray-600';
};
```

---

## 📝 타입 정의

```typescript
// fund.types.ts에 정의된 주요 타입들

FundClassDetail      - 펀드 클래스 상세 정보
FundMaster           - 모펀드 정보
FundRules            - 펀드 규칙
FundFees             - 펀드 수수료
FundNav              - 펀드 기준가
FundPurchaseRequest  - 매수 요청
FundPurchaseResponse - 매수 응답
FundRedemptionRequest  - 매도 요청
FundRedemptionResponse - 매도 응답
FundSubscription     - 펀드 가입 정보
FundTransaction      - 거래 내역
FundTransactionStats - 거래 통계
```

---

## 🎨 스타일링

### Tailwind CSS 주요 클래스

```css
/* 버튼 */
bg-[#00857D] text-white rounded-lg hover:bg-[#006D66]

/* 카드 */
bg-white rounded-lg shadow-md p-6 hover:shadow-lg

/* 필터 */
border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#00857D]

/* 배지 */
px-2 py-1 text-xs bg-[#00857D] text-white rounded

/* 통계 카드 */
bg-blue-50 rounded-lg border border-blue-200
```

---

## ⚠️ 주의사항

### 1. 임시 사용자 정보

현재는 하드코딩된 임시 정보를 사용합니다. 실제 환경에서는:
- 로그인 상태 관리 (Redux, Zustand 등)
- 사용자 정보 Context
- 인증 토큰 관리

---

### 2. 에러 처리

현재는 기본적인 try-catch만 구현되어 있습니다. 추가 필요:
- 토스트 알림 (react-toastify)
- 에러 바운더리
- 재시도 로직

---

### 3. 로딩 상태

각 페이지에 로딩 스피너가 구현되어 있지만, 추가 개선 가능:
- Skeleton UI
- Progressive Loading
- Optimistic UI Updates

---

## 🎉 완료 요약

✅ **타입 정의**: 모든 API 응답 타입 정의 완료  
✅ **API 서비스**: fundApi.ts로 모든 API 함수 추상화  
✅ **5개 페이지**: 목록, 상세, 매수, 포트폴리오, 거래내역  
✅ **라우팅**: React Router 설정 완료  
✅ **스타일링**: Tailwind CSS + 하나은행 브랜드 컬러  
✅ **반응형**: 모바일, 태블릿, 데스크톱 대응  
✅ **데이터 플로우**: 백엔드 API 완전 연동  

**프론트엔드 연동 완료! 🎊**

이제 펀드 서비스의 전체 기능을 브라우저에서 사용할 수 있습니다!

---

## 📚 다음 단계 (선택사항)

1. **매도 페이지 추가** (`/fund/:subscriptionId/sell`)
2. **차트 추가** (기준가 추이, 포트폴리오 구성)
3. **상태 관리** (Redux, Zustand)
4. **실시간 업데이트** (WebSocket)
5. **알림 기능** (목표 수익률 달성 시)
6. **PDF 리포트** (포트폴리오 리포트 다운로드)

