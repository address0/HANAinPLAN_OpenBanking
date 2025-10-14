# HANAinPLAN 통합 테스트 데이터 삽입 가이드

## 개요

이 스크립트는 HANAinPLAN 프로젝트의 모든 백엔드 서버에 테스트 데이터를 자동으로 삽입합니다.

### 삽입되는 데이터

1. **User 서버 (8084)**: 10명의 테스트 사용자 및 CI 발급
2. **은행 서버 (Hana 8081, Shinhan 8082, Kookmin 8083)**:
   - 각 사용자별 1-3개 은행에 고객 등록 (랜덤)
   - 각 은행당 1-3개 계좌 생성 (랜덤)
3. **hanainplan 서버 (8080)**: 업종코드 57개, 질병코드 38개

## 사전 준비

### 1. 모든 서버 실행

다음 서버들이 모두 실행 중이어야 합니다:

```bash
# User 서버
cd backend/user
docker-compose up -d

# Hana 은행 서버
cd backend/banks/hana
docker-compose up -d

# Shinhan 은행 서버
cd backend/banks/shinhan
docker-compose up -d

# Kookmin 은행 서버
cd backend/banks/kookmin
docker-compose up -d

# hanainplan 메인 서버
cd backend/hanainplan
docker-compose up -d
```

### 2. Python 라이브러리 설치

#### 방법 1: requirements.txt 사용 (권장)

```bash
cd backend
pip3 install -r requirements.txt
```

#### 방법 2: 개별 설치

```bash
pip3 install requests pymysql cryptography
```

또는

```bash
python3 -m pip install requests pymysql cryptography
```

> **Note**: `cryptography` 패키지는 MySQL 8.0의 `caching_sha2_password` 인증을 위해 필요합니다.

## 실행 방법

### 1. 스크립트 실행

```bash
cd backend
python3 insert_test_data.py
```

### 2. 실행 권한 부여 후 실행 (선택사항)

```bash
chmod +x insert_test_data.py
./insert_test_data.py
```

## 실행 순서

스크립트는 다음 순서로 데이터를 삽입합니다:

1. **hanainplan 데이터코드 삽입**
   - MySQL에 직접 연결하여 `datacode.sql` 파일 실행
   - 업종코드 (industry_code) 57개 삽입
   - 질병코드 (disease_code) 38개 삽입

2. **사용자별 처리 (10명)**
   
   각 사용자마다:
   - User 서버에 등록 → CI 발급
   - 1-3개 은행을 랜덤 선택
   - 선택된 각 은행에:
     - 고객 등록 (CI 사용)
     - 1-3개 계좌 생성 (다양한 타입과 잔액)

3. **통계 출력**
   - 생성/실패 건수 요약
   - 데이터 확인 방법 안내

## 생성되는 테스트 데이터

### 사용자 정보 (10명)

| 이름 | 생년월일 | 성별 | 전화번호 |
|------|----------|------|----------|
| 김민준 | 1990.03.15 | 남 | 010-1234-5001 |
| 이서연 | 1995.08.20 | 여 | 010-1234-5002 |
| 박지훈 | 1988.07.05 | 남 | 010-1234-5003 |
| 최수민 | 1992.12.01 | 여 | 010-1234-5004 |
| 정현우 | 1985.04.10 | 남 | 010-1234-5005 |
| 강지은 | 1998.06.25 | 여 | 010-1234-5006 |
| 조성훈 | 1993.11.18 | 남 | 010-1234-5007 |
| 윤아영 | 1987.02.22 | 여 | 010-1234-5008 |
| 임동혁 | 1996.09.08 | 남 | 010-1234-5009 |
| 한예린 | 1994.05.14 | 여 | 010-1234-5010 |

### 계좌 유형 및 잔액 범위

| 계좌 타입 | 이름 | 최소 잔액 | 최대 잔액 |
|----------|------|-----------|-----------|
| 0 | 통합계좌 | 500만원 | 2억원 |
| 1 | 수시입출금 | 10만원 | 500만원 |
| 2 | 예적금 | 100만원 | 1억원 |
| 6 | 수익증권 | 50만원 | 5천만원 |

### 은행별 특징

- **하나은행 (8081)**: 계좌번호 100으로 시작
- **신한은행 (8082)**: 계좌번호 456-459로 시작
- **국민은행 (8083)**: 계좌번호 123-129로 시작

## 데이터 확인

### API로 확인

```bash
# User 서버 - 모든 사용자 조회
curl http://localhost:8084/api/user/all

# 하나은행 - 모든 고객 조회
curl http://localhost:8081/api/hana/customers

# 하나은행 - 모든 계좌 조회
curl http://localhost:8081/api/hana/accounts

# 신한은행 - 모든 고객 조회
curl http://localhost:8082/api/shinhan/customers

# 국민은행 - 모든 고객 조회
curl http://localhost:8083/api/kookmin/customers

# hanainplan - 업종코드 조회
curl http://localhost:8080/api/industries

# hanainplan - 질병코드 조회
curl http://localhost:8080/api/diseases
```

### 데이터베이스에서 확인

```bash
# User 서버 DB
mysql -h localhost -P 3310 -u root -p
# password: password
# database: user_db

# Hana 은행 DB
mysql -h localhost -P 3307 -u root -p
# password: password
# database: hana_db

# Shinhan 은행 DB
mysql -h localhost -P 3308 -u root -p
# password: password
# database: shinhan_db

# Kookmin 은행 DB
mysql -h localhost -P 3309 -u root -p
# password: password
# database: kookmin_db

# hanainplan DB
mysql -h localhost -P 3306 -u root -p
# password: password
# database: hanainplan_db
```

## 문제 해결

### 1. 서버 연결 오류

```
❌ User 생성 오류: Connection refused
```

**해결 방법**: 해당 서버가 실행 중인지 확인

```bash
# 실행 중인 컨테이너 확인
docker ps

# 특정 서버 재시작
cd backend/user
docker-compose restart
```

### 2. CI 중복 오류

```
❌ User 생성 실패: 이미 존재하는 CI입니다
```

**해결 방법**: 기존 데이터 삭제 후 재실행

```bash
# User DB 초기화
mysql -h localhost -P 3310 -u root -p
# password 입력
USE user_db;
TRUNCATE TABLE users;
```

### 3. pymysql 설치 오류

```
❌ pymysql 라이브러리가 필요합니다
```

**해결 방법**:

```bash
pip3 install pymysql
```

### 4. MySQL 연결 오류

```
❌ MySQL 연결 실패: Access denied
```

**해결 방법**: 
- docker-compose.yml의 MySQL 비밀번호 확인
- 스크립트의 비밀번호 수정 (기본값: `password`)

### 5. hanainplan 데이터코드 삽입 실패

**해결 방법**: SQL 파일을 직접 실행

```bash
mysql -h localhost -P 3306 -u root -p hanainplan_db < backend/hanainplan/datacode.sql
```

## 스크립트 커스터마이징

### 사용자 수 변경

`TEST_USERS` 리스트에 사용자 추가/삭제:

```python
TEST_USERS = [
    {
        "name": "홍길동",
        "birthDate": "19900101",
        "gender": "M",
        "residentNumber": "9001011234567",  # 유효한 주민번호 필요
        "phone": "010-0000-0000"
    },
    # ... 추가 사용자
]
```

### 은행 선택 확률 조정

`select_random_banks()` 함수 수정:

```python
def select_random_banks():
    # 모든 사용자가 3개 은행 모두 가입
    return list(BANK_CONFIGS.keys())
    
    # 또는 특정 은행만 선택
    return ["hana", "shinhan"]
```

### 계좌 수 조정

`generate_random_accounts()` 함수 수정:

```python
def generate_random_accounts():
    # 모든 사용자가 4개 계좌 모두 생성
    return list(ACCOUNT_TYPES.keys())
    
    # 또는 고정된 개수
    num_accounts = 2  # 항상 2개
    # ...
```

## 참고사항

- 스크립트는 멱등성(idempotency)을 보장하지 않습니다
- 중복 실행 시 동일한 CI로 인해 오류 발생 가능
- 재실행 전 기존 데이터 삭제 권장
- 서버 부하를 고려하여 적절한 지연(delay) 추가됨
- hanainplan 데이터코드는 `INSERT IGNORE`로 중복 방지

## 라이선스

이 스크립트는 HANAinPLAN 프로젝트의 일부입니다.

