# User Server

사용자 관리 서버입니다. 사용자 정보, 프로필, 세션 관리를 담당합니다.

## 서버 정보

- **포트**: 8084
- **데이터베이스**: MySQL (포트 3310)
- **API 엔드포인트**: `http://localhost:8084/api/user/`

## 실행 방법

### Docker로 실행
```bash
# User 서버와 데이터베이스를 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서버 중지
docker-compose down
```

### 로컬 개발 실행
```bash
# Gradle로 실행
./gradlew bootRun

# 빌드
./gradlew build
```

## API 테스트

### Health Check
```bash
curl http://localhost:8084/api/user/health
```

### 서버 정보 확인
```bash
curl http://localhost:8084/api/user/info
```

## 데이터베이스 접속

### MySQL 접속
```bash
mysql -h localhost -P 3310 -u root -p
# 비밀번호: password
# 데이터베이스: user_db
```

## 데이터베이스 스키마

### users 테이블
- `id`: 사용자 고유 ID (AUTO_INCREMENT)
- `user_id`: 사용자 식별자 (UNIQUE)
- `username`: 사용자명
- `email`: 이메일 (UNIQUE)
- `phone`: 전화번호
- `status`: 사용자 상태 (ACTIVE, INACTIVE, SUSPENDED)
- `created_at`: 생성일시
- `updated_at`: 수정일시

### user_profiles 테이블
- `id`: 프로필 고유 ID (AUTO_INCREMENT)
- `user_id`: 사용자 식별자 (외래키)
- `first_name`: 이름
- `last_name`: 성
- `birth_date`: 생년월일
- `address`: 주소
- `created_at`: 생성일시
- `updated_at`: 수정일시

### user_sessions 테이블
- `id`: 세션 고유 ID (AUTO_INCREMENT)
- `user_id`: 사용자 식별자 (외래키)
- `session_token`: 세션 토큰 (UNIQUE)
- `expires_at`: 만료일시
- `created_at`: 생성일시

## 환경변수

다음 환경변수들을 설정할 수 있습니다:

- `MYSQL_ROOT_PASSWORD`: MySQL root 비밀번호
- `USER_DATABASE`: 사용자 데이터베이스명
- `USER_USER`: 사용자 데이터베이스 사용자명
- `USER_PASSWORD`: 사용자 데이터베이스 비밀번호

## 샘플 데이터

초기화 시 다음 샘플 데이터가 생성됩니다:

- **user001**: john_doe (john@example.com)
- **user002**: jane_smith (jane@example.com)
- **user003**: bob_wilson (bob@example.com)

각 사용자에 대한 프로필 정보도 함께 생성됩니다.
