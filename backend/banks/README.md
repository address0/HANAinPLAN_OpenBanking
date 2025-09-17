# 은행별 서버 구성

이 디렉토리는 각 은행별로 독립적인 Spring Boot 서버를 구성합니다.

## 구성된 은행 서버

### 1. HANA Bank Server
- **포트**: 8081
- **데이터베이스**: MySQL (포트 3307)
- **디렉토리**: `./hana/`
- **API 엔드포인트**: `http://localhost:8081/api/hana/`

### 2. Shinhan Bank Server
- **포트**: 8082
- **데이터베이스**: MySQL (포트 3308)
- **디렉토리**: `./shinhan/`
- **API 엔드포인트**: `http://localhost:8082/api/shinhan/`

### 3. Kookmin Bank Server
- **포트**: 8083
- **데이터베이스**: MySQL (포트 3309)
- **디렉토리**: `./kookmin/`
- **API 엔드포인트**: `http://localhost:8083/api/kookmin/`

## 실행 방법

### 전체 서버 실행
```bash
# 모든 은행 서버와 데이터베이스를 한번에 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서버 중지
docker-compose down
```

### 개별 서버 실행
```bash
# HANA Bank만 실행
cd hana
docker-compose up -d

# Shinhan Bank만 실행
cd shinhan
docker-compose up -d

# Kookmin Bank만 실행
cd kookmin
docker-compose up -d
```

## API 테스트

### Health Check
```bash
# HANA Bank
curl http://localhost:8081/api/hana/health

# Shinhan Bank
curl http://localhost:8082/api/shinhan/health

# Kookmin Bank
curl http://localhost:8083/api/kookmin/health
```

### 서버 정보 확인
```bash
# HANA Bank
curl http://localhost:8081/api/hana/info

# Shinhan Bank
curl http://localhost:8082/api/shinhan/info

# Kookmin Bank
curl http://localhost:8083/api/kookmin/info
```

## 데이터베이스 접속

### HANA Bank MySQL
```bash
mysql -h localhost -P 3307 -u root -p
# 비밀번호: password
# 데이터베이스: hana_bank
```

### Shinhan Bank MySQL
```bash
mysql -h localhost -P 3308 -u root -p
# 비밀번호: password
# 데이터베이스: shinhan_bank
```

### Kookmin Bank MySQL
```bash
mysql -h localhost -P 3309 -u root -p
# 비밀번호: password
# 데이터베이스: kookmin_bank
```

## 개발 환경 설정

각 은행 서버는 독립적인 Spring Boot 프로젝트로 구성되어 있습니다.

### 로컬 개발 실행
```bash
# HANA Bank
cd hana
./gradlew bootRun

# Shinhan Bank
cd shinhan
./gradlew bootRun

# Kookmin Bank
cd kookmin
./gradlew bootRun
```

### 빌드
```bash
# HANA Bank
cd hana
./gradlew build

# Shinhan Bank
cd shinhan
./gradlew build

# Kookmin Bank
cd kookmin
./gradlew build
```

## 포트 정리

| 서비스 | 포트 | 설명 |
|--------|------|------|
| HANA Bank App | 8081 | HANA Bank API 서버 |
| HANA MySQL | 3307 | HANA Bank 데이터베이스 |
| Shinhan Bank App | 8082 | Shinhan Bank API 서버 |
| Shinhan MySQL | 3308 | Shinhan Bank 데이터베이스 |
| Kookmin Bank App | 8083 | Kookmin Bank API 서버 |
| Kookmin MySQL | 3309 | Kookmin Bank 데이터베이스 |

## 주의사항

1. 각 은행 서버는 독립적인 데이터베이스를 사용합니다.
2. 포트 충돌을 방지하기 위해 각각 다른 포트를 사용합니다.
3. Docker 컨테이너 실행 전에 해당 포트가 사용 중이지 않은지 확인하세요.
4. 데이터베이스 초기화 스크립트(`init.sql`)는 각 은행별로 샘플 데이터를 포함합니다.
