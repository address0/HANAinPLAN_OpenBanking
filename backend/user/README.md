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

### Swagger UI 접속
서버 실행 후 브라우저에서 다음 URL로 접속하여 API를 테스트할 수 있습니다:

- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8084/v3/api-docs

### Health Check
```bash
curl http://localhost:8084/api/user/health
```

### 서버 정보 확인
```bash
curl http://localhost:8084/api/user/info
```

### CI 변환 API
```bash
# CI 변환
curl -X POST http://localhost:8084/api/user/ci/convert \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "birthDate": "19900115",
    "gender": "M",
    "residentNumber": "9001151234567"
  }'

# CI 검증
curl -X POST http://localhost:8084/api/user/ci/verify \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "birthDate": "19900115",
    "gender": "M",
    "residentNumber": "9001151234567"
  }'

# CI 변환 테스트 (GET)
curl "http://localhost:8084/api/user/ci/test?name=홍길동&birthDate=19900115&gender=M&residentNumber=9001151234567"
```

### 사용자 관리 API
```bash
# 사용자 생성 (CI 포함)
curl -X POST http://localhost:8084/api/user/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "phone": "010-1234-5678",
    "birthDate": "19900115",
    "gender": "M",
    "residentNumber": "9001151234567"
  }'

# 응답 예시
# {
#   "id": 1,
#   "name": "홍길동",
#   "phone": "010-1234-5678",
#   "birthDate": "19900115",
#   "gender": "M",
#   "ci": "ABC123DEF456GHI789"
# }

# 사용자 조회 (ID로)
curl http://localhost:8084/api/user/1

# CI로 사용자 조회
curl http://localhost:8084/api/user/ci/생성된_CI_값

# 모든 사용자 조회
curl http://localhost:8084/api/user/all

# 사용자 삭제
curl -X DELETE http://localhost:8084/api/user/1
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
- `id`: 사용자 고유 ID (AUTO_INCREMENT, Primary Key)
- `name`: 사용자 이름
- `phone`: 전화번호
- `ci`: CI 값 (UNIQUE) - 실명인증용
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

## CI 변환 로직

### 1. 입력 데이터 정규화
- 이름: 대소문자 통일 (대문자로 변환)
- 생년월일: YYYYMMDD 형식으로 통일
- 성별: M(남성) 또는 F(여성)으로 통일
- 주민번호: 13자리 숫자로 정규화

### 2. 데이터 결합
정해진 순서로 필드 결합: `이름|생년월일|성별|주민번호`

### 3. 키 기반 일방향 변환
- HMAC-SHA256 알고리즘 사용
- 공인기관의 시크릿 키 적용
- Base64 URL-safe 인코딩

### 4. CI 생성
- 최대 32자리로 제한
- 중복 방지를 위한 UNIQUE 제약

## 주민번호 검증 규칙

### 1. 기본 형식 검증
- 13자리 숫자만 허용
- 숫자가 아닌 문자 입력 시 오류

### 2. 생년월일 일치 검증
- 생년월일(YYYYMMDD)의 뒤 6자리(YYMMDD)와 주민번호 앞 6자리가 일치해야 함
- 예: 생년월일이 19900115이면 주민번호 앞 6자리는 900115여야 함

### 3. 연도대별 성별 코드 검증
- **1900년대 (1900-1999년)**:
  - 남성(M): 주민번호 7번째 자리가 1
  - 여성(F): 주민번호 7번째 자리가 2
- **2000년대 이상 (2000년 이후)**:
  - 남성(M): 주민번호 7번째 자리가 3
  - 여성(F): 주민번호 7번째 자리가 4

### 4. 체크섬 검증
- 주민번호 마지막 자리(13번째)는 앞 12자리의 체크섬
- 가중치: 2,3,4,5,6,7,8,9,2,3,4,5
- 계산식: (11 - (합계 % 11)) % 10

### 5. 성별 입력값 검증
- M(남성) 또는 F(여성)만 허용
- 그 외 입력값은 예외 처리

## 데이터베이스 초기화

JPA의 `ddl-auto: update` 설정으로 테이블이 자동 생성됩니다. 별도의 초기화 스크립트는 필요하지 않습니다.

샘플 데이터가 필요한 경우 API를 통해 사용자를 생성하세요.
