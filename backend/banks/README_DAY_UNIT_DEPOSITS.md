# 일 단위 정기예금 금리 데이터 추가 가이드

## 📋 개요

퇴직이 임박한 고객(6개월 미만)을 위한 **일 단위 정기예금** 상품 금리 데이터를 추가합니다.

### 추가되는 상품
- **90일 정기예금**: 퇴직까지 120일 미만인 경우 추천
- **180일 정기예금**: 퇴직까지 120일 이상 6개월 미만인 경우 추천

### 지원 은행
- 하나은행 (HANA)
- 국민은행 (KOOKMIN)
- 신한은행 (SHINHAN)

---

## 🚀 실행 방법

### 1. 각 은행 서버가 실행 중인지 확인

```bash
# 하나은행 컨테이너 확인
docker ps | grep hana-bank

# 국민은행 컨테이너 확인
docker ps | grep kookmin-bank

# 신한은행 컨테이너 확인
docker ps | grep shinhan-bank
```

### 2. SQL 스크립트 실행

#### 방법 A: 터미널에서 직접 실행 (권장)

```bash
# 루트 디렉토리에서 실행
cd backend/banks

# MySQL 클라이언트로 실행
mysql -h 127.0.0.1 -P 3307 -u root -p < insert_day_unit_interest_rates.sql

# 비밀번호: root1234 (하나은행 기준)
```

**각 은행별 포트:**
- 하나은행: `3307`
- 국민은행: `3308`
- 신한은행: `3309`

#### 방법 B: Docker Exec로 실행

```bash
# 하나은행
docker exec -i hana-mysql mysql -uroot -proot1234 < insert_day_unit_interest_rates.sql

# 국민은행
docker exec -i kookmin-mysql mysql -uroot -proot1234 < insert_day_unit_interest_rates.sql

# 신한은행
docker exec -i shinhan-mysql mysql -uroot -proot1234 < insert_day_unit_interest_rates.sql
```

---

## 📊 추가되는 금리 데이터

### 하나은행 (HANA)
| 기간 | 금리 종류 | IRP 여부 | 금리 |
|------|----------|----------|------|
| 90일 | BASIC | 일반 | 2.80% |
| 90일 | PREFERENTIAL | 일반 | 3.00% |
| 90일 | BASIC | IRP | 2.90% |
| 90일 | PREFERENTIAL | IRP | 3.10% |
| 180일 | BASIC | 일반 | 2.85% |
| 180일 | PREFERENTIAL | 일반 | 3.05% |
| 180일 | BASIC | IRP | 2.95% |
| 180일 | PREFERENTIAL | IRP | 3.15% |

### 국민은행 (KOOKMIN) - 최고 금리 🏆
| 기간 | 금리 종류 | IRP 여부 | 금리 |
|------|----------|----------|------|
| 90일 | BASIC | 일반 | 2.90% |
| 90일 | PREFERENTIAL | 일반 | 3.10% |
| 90일 | BASIC | IRP | **3.00%** ⭐ |
| 90일 | PREFERENTIAL | IRP | **3.20%** ⭐ |
| 180일 | BASIC | 일반 | 2.95% |
| 180일 | PREFERENTIAL | 일반 | 3.15% |
| 180일 | BASIC | IRP | **3.05%** ⭐ |
| 180일 | PREFERENTIAL | IRP | **3.25%** ⭐ |

### 신한은행 (SHINHAN)
| 기간 | 금리 종류 | IRP 여부 | 금리 |
|------|----------|----------|------|
| 90일 | BASIC | 일반 | 2.85% |
| 90일 | PREFERENTIAL | 일반 | 3.05% |
| 90일 | BASIC | IRP | 2.95% |
| 90일 | PREFERENTIAL | IRP | 3.15% |
| 180일 | BASIC | 일반 | 2.90% |
| 180일 | PREFERENTIAL | 일반 | 3.10% |
| 180일 | BASIC | IRP | 3.00% |
| 180일 | PREFERENTIAL | IRP | 3.20% |

---

## ✅ 확인 방법

### 1. 데이터 삽입 확인

```sql
-- 하나은행 확인
USE hana_bank;
SELECT product_code, interest_type, maturity_period, interest_rate, is_irp 
FROM hana_interest_rates 
WHERE maturity_period IN ('90일', '180일');

-- 국민은행 확인
USE kookmin_bank;
SELECT product_code, interest_type, maturity_period, interest_rate, is_irp 
FROM kookmin_interest_rates 
WHERE maturity_period IN ('90일', '180일');

-- 신한은행 확인
USE shinhan_bank;
SELECT product_code, interest_type, maturity_period, interest_rate, is_irp 
FROM shinhan_interest_rates 
WHERE maturity_period IN ('90일', '180일');
```

### 2. 추천 API 테스트

퇴직일이 3개월 후인 경우 (90일 상품 추천):

```bash
curl -X POST http://localhost:8080/api/banking/deposit/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "retirementDate": "2025-04-02",
    "goalAmount": 50000000
  }'
```

**예상 결과:**
```json
{
  "depositCode": "IRP_DEPOSIT_001",
  "depositName": "IRP 정기예금",
  "bankCode": "KOOKMIN",
  "bankName": "국민은행",
  "productType": 2,
  "productTypeName": "일단위 정기예금",
  "contractPeriod": 3,
  "contractPeriodUnit": "일",
  "maturityPeriod": "90일",
  "appliedRate": 0.0300,
  "recommendationReason": "은퇴까지 약 0개월(92일)이 남아, 90일 정기예금을 추천드립니다. 은퇴 시점에 맞춰 만기가 도래하여 자금을 받을 수 있습니다. 국민은행이 연 3.00%로 가장 높은 금리를 제공합니다."
}
```

---

## 🎯 추천 로직

### 은퇴까지 남은 기간에 따른 상품 추천

| 남은 기간 | 추천 상품 | 비고 |
|-----------|----------|------|
| 5년 이상 | 5년 정기예금 | 장기 안정 투자 |
| 3~5년 | 3년 정기예금 | 은퇴 시점 맞춤 |
| 2~3년 | 2년 정기예금 | 은퇴 시점 맞춤 |
| 1~2년 | 1년 정기예금 | 은퇴 시점 맞춤 |
| 6개월~1년 | 6개월 정기예금 | 유연한 자금 운용 |
| 120일~6개월 | **180일 정기예금** 🆕 | 퇴직 임박 대응 |
| 120일 미만 | **90일 정기예금** 🆕 | 퇴직 직전 대응 |

---

## 💡 특징

### 1. 높은 금리
일 단위 상품은 **단기 특판 상품** 성격으로 일반 6개월 상품보다 높은 금리를 제공합니다.

**금리 비교 (IRP 기본금리 기준):**
- 일반 6개월: 2.07%
- **90일**: 2.90~3.00% (+0.83%p~0.93%p) ⬆️
- **180일**: 2.95~3.05% (+0.88%p~0.98%p) ⬆️

### 2. 퇴직 시점 맞춤
- 은퇴일에 맞춰 만기가 도래하여 **조기 해지 없이** 자금 수령 가능
- 중도 해지 시 불이익(금리 감소) 방지

### 3. 은행별 차별화
- 국민은행이 가장 높은 금리 제공 (90일 IRP: 3.00%, 180일 IRP: 3.05%)
- 신한은행, 하나은행 순으로 금리 경쟁

---

## 🔧 트러블슈팅

### 문제 1: "Unknown database" 오류
```
ERROR 1049 (42000): Unknown database 'hana_bank'
```

**해결:** 각 은행 서버의 `init.sql`이 먼저 실행되었는지 확인
```bash
docker exec -it hana-mysql mysql -uroot -proot1234 -e "SHOW DATABASES;"
```

### 문제 2: 중복 키 오류
```
ERROR 1062 (23000): Duplicate entry...
```

**해결:** 이미 데이터가 존재하는 경우, 먼저 삭제 후 재실행
```sql
USE hana_bank;
DELETE FROM hana_interest_rates WHERE maturity_period IN ('90일', '180일');
```

### 문제 3: 추천 시 일 단위 상품이 안 나옴

**확인사항:**
1. 퇴직일이 정말 6개월 미만인지 확인
2. IRP 계좌가 활성화되어 있는지 확인
3. 백엔드 서버 재시작 (캐시 갱신)

```bash
cd backend/hanainplan
./gradlew bootRun
```

---

## 📚 관련 파일

- 추천 로직: `backend/hanainplan/src/main/java/com/hanainplan/domain/banking/service/DepositProductService.java`
- 금리 엔티티: `backend/banks/{hana|kookmin|shinhan}/src/main/java/com/.../product/entity/InterestRate.java`
- SQL 스크립트: `backend/banks/insert_day_unit_interest_rates.sql`

---

## ✨ 개선 효과

### Before (수정 전)
- 퇴직 6개월 미만: 6개월 상품 추천
- 문제: 퇴직 후 중도 해지 필요 → 금리 손실

### After (수정 후)
- 퇴직 120일 미만: **90일 상품 추천** (3.00%)
- 퇴직 120~180일: **180일 상품 추천** (3.05%)
- 개선: 퇴직 시점 정확히 맞춤 + 더 높은 금리

**예시:**
- 퇴직까지 100일 남음 → 90일 상품 가입 → 퇴직 시 만기 도래 ✅
- 기존: 6개월 상품 가입 → 퇴직 후 4개월 남음 → 중도 해지 필요 ❌









