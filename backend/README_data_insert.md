# HANAinPLAN 오픈뱅킹 통합 데이터 삽입 가이드

## 개요
이 스크립트는 HANAinPLAN 오픈뱅킹 프로젝트의 모든 백엔드 서버에 초기 데이터를 한 번에 삽입합니다.

## 삽입되는 데이터

### 1. HANAinPLAN 백엔드 서버 (포트 3306)
- ✅ 예금 상품 코드 3개 (하나, 신한, 국민)
- ✅ 이율보증형 보험 상품 코드 3개 (하나생명, 한화생명, 삼성생명)

### 2. 하나은행 백엔드 서버 (포트 3307)
- ✅ 고객 데이터 3명
- ✅ 계좌 데이터 4개
- ✅ 예금 가입 정보 2개

### 3. 신한은행 백엔드 서버 (포트 3308)
- ✅ 고객 데이터 2명
- ✅ 계좌 데이터 2개

### 4. 국민은행 백엔드 서버 (포트 3309)
- ✅ 고객 데이터 2명
- ✅ 계좌 데이터 2개

## 사용 방법

### 1. 필수 요구사항
```bash
pip install mysql-connector-python
```

### 2. 모든 MySQL 서버 실행 확인
```bash
# 각 은행의 Docker 컨테이너 확인
docker ps | grep mysql

# 또는 로컬 MySQL 서비스 확인
systemctl status mysql  # Linux
brew services list | grep mysql  # macOS
```

### 3. 스크립트 실행
```bash
cd /Users/jusoyeong/Desktop/HANAinPLAN_OpenBanking/backend
python3 insert_all_data.py
```

## 환경 변수 설정 (선택사항)

기본값을 사용하지 않을 경우 환경 변수로 DB 접속 정보를 설정할 수 있습니다:

```bash
# HANAinPLAN 백엔드
export HANAINPLAN_DB_HOST=localhost
export HANAINPLAN_DB_PORT=3306

# 하나은행
export HANA_DB_HOST=localhost
export HANA_DB_PORT=3307

# 신한은행
export SHINHAN_DB_HOST=localhost
export SHINHAN_DB_PORT=3308

# 국민은행
export KOOKMIN_DB_HOST=localhost
export KOOKMIN_DB_PORT=3309

# 공통 인증 정보
export MYSQL_USER=root
export MYSQL_PASSWORD=root1234

# 스크립트 실행
python3 insert_all_data.py
```

## 실행 결과 예시

```
======================================================================
  HANAinPLAN 오픈뱅킹 통합 데이터 삽입 스크립트
======================================================================
실행 시간: 2025-10-01 16:00:00

----------------------------------------------------------------------
📋 1. HANAinPLAN 백엔드 - 상품 코드 삽입
----------------------------------------------------------------------
✅ HANAINPLAN DB 연결 성공: localhost:3306/hanainplan

  📊 예금 상품 코드 삽입...
    ✓ 하나 정기예금 (HANA-DEP-001)
    ✓ 신한 정기예금 (SHINHAN-DEP-001)
    ✓ 국민 정기예금 (KOOKMIN-DEP-001)

  🏥 이율보증형 보험 상품 코드 삽입...
    ✓ 하나생명 이율보증형보험 (HANA-INS-001)
    ✓ 한화생명 이율보증형보험 (HANHWA-INS-001)
    ✓ 삼성생명 이율보증형보험 (SAMSUNG-INS-001)

  ✅ HANAinPLAN 상품 코드 삽입 완료

----------------------------------------------------------------------
📋 2. 하나은행 백엔드 - 샘플 데이터 삽입
----------------------------------------------------------------------
✅ HANA DB 연결 성공: localhost:3307/hana_bank

  👥 고객 데이터 삽입...
    ✓ 홍길동 (M, 1990년생)
    ✓ 김영희 (F, 1992년생)
    ✓ 이철수 (M, 1988년생)

  🏦 계좌 데이터 삽입...
    ✓ 110-1234-567890 (잔액: 5,000,000원)
    ✓ 110-2345-678901 (잔액: 10,000,000원)
    ✓ 110-3456-789012 (잔액: 3,000,000원)
    ✓ 110-4567-890123 (잔액: 8,000,000원)

  💰 예금 가입 정보 삽입...
    ✓ 110-2345-678901 (가입일: 2025-09-01, 금리: 3.50%)
    ✓ 110-3456-789012 (가입일: 2025-08-02, 금리: 3.40%)

  ✅ 하나은행 데이터 삽입 완료

----------------------------------------------------------------------
📋 3. 신한은행 백엔드 - 샘플 데이터 삽입
----------------------------------------------------------------------
✅ SHINHAN DB 연결 성공: localhost:3308/shinhan_bank

  👥 고객 데이터 삽입...
    ✓ 홍길동
    ✓ 김영희

  🏦 계좌 데이터 삽입...
    ✓ 140-5678-901234 (잔액: 4,000,000원)
    ✓ 140-6789-012345 (잔액: 6,000,000원)

  ✅ 신한은행 데이터 삽입 완료

----------------------------------------------------------------------
📋 4. 국민은행 백엔드 - 샘플 데이터 삽입
----------------------------------------------------------------------
✅ KOOKMIN DB 연결 성공: localhost:3309/kookmin_bank

  👥 고객 데이터 삽입...
    ✓ 홍길동
    ✓ 이철수

  🏦 계좌 데이터 삽입...
    ✓ 004-7890-123456 (잔액: 7,000,000원)
    ✓ 004-8901-234567 (잔액: 9,000,000원)

  ✅ 국민은행 데이터 삽입 완료

======================================================================
📊 데이터 삽입 결과 요약
======================================================================

✅ 성공: 4개
  ✓ HANAinPLAN
  ✓ 하나은행
  ✓ 신한은행
  ✓ 국민은행

======================================================================
🎉 모든 데이터 삽입이 성공적으로 완료되었습니다!
======================================================================
```

## Docker Compose 환경에서 실행

```bash
# Docker 컨테이너가 실행 중인 경우
docker-compose exec hanainplan-app python3 /app/insert_all_data.py
```

## 주요 기능

✅ **다중 DB 처리**: 4개의 서로 다른 MySQL 데이터베이스에 동시 삽입  
✅ **중복 방지**: ON DUPLICATE KEY UPDATE로 안전하게 재실행 가능  
✅ **에러 격리**: 한 DB 실패 시에도 다른 DB는 계속 처리  
✅ **상세 로그**: 각 단계별 진행 상황 실시간 출력  
✅ **결과 요약**: 성공/실패 항목 명확히 표시

## 데이터베이스 포트 구성

| 서버 | 데이터베이스 | 포트 |
|------|-------------|------|
| HANAinPLAN | hanainplan | 3306 |
| 하나은행 | hana_bank | 3307 |
| 신한은행 | shinhan_bank | 3308 |
| 국민은행 | kookmin_bank | 3309 |

## 문제 해결

### 연결 실패
```bash
# 각 MySQL 서버 상태 확인
docker ps | grep mysql

# 포트 확인
netstat -an | grep 3306
netstat -an | grep 3307
netstat -an | grep 3308
netstat -an | grep 3309
```

### 권한 오류
```sql
-- 각 데이터베이스에서 권한 확인 및 부여
GRANT ALL PRIVILEGES ON hanainplan.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON hana_bank.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON shinhan_bank.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON kookmin_bank.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

### 테이블 없음 오류
먼저 각 백엔드 서버의 init.sql이 실행되어 테이블이 생성되어 있는지 확인하세요.

## 데이터 초기화 후 재실행

스크립트는 재실행해도 안전합니다. 이미 존재하는 데이터는 업데이트되고, 새로운 데이터만 삽입됩니다.

```bash
# 데이터를 완전히 초기화하고 싶다면
mysql -u root -p hanainplan < backend/hanainplan/docker/mysql/init.sql
mysql -u root -p hana_bank < backend/banks/hana/init.sql
# ... 다른 DB도 동일하게

# 그 후 스크립트 재실행
python3 insert_all_data.py
```

## 추가 데이터 커스터마이징

스크립트 내의 데이터 배열을 수정하여 원하는 샘플 데이터를 추가할 수 있습니다:
- `DEPOSIT_PRODUCTS`: 예금 상품
- `INSURANCE_PRODUCTS`: 보험 상품
- 각 `insert_*_bank_data` 함수 내의 customers, accounts, subscriptions

## 주의사항

1. 모든 MySQL 서버가 실행 중이어야 합니다
2. 각 서버의 테이블이 미리 생성되어 있어야 합니다 (init.sql)
3. 네트워크 연결이 안정적이어야 합니다
4. 충분한 디스크 공간이 있어야 합니다


