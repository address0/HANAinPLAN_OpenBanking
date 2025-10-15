# 펀드 기준가 크롤링 시스템

미래에셋 펀드 기준가를 자동으로 크롤링하여 데이터베이스에 저장하는 시스템입니다.

## 📋 목차

- [시스템 구성](#시스템-구성)
- [설치 및 설정](#설치-및-설정)
- [사용 방법](#사용-방법)
- [스케줄 설정](#스케줄-설정)
- [API 문서](#api-문서)
- [문제 해결](#문제-해결)

## 🏗️ 시스템 구성

### 1. Python 크롤러 (`fund_crawler.py`)
- 미래에셋 펀드 페이지에서 기준가를 크롤링
- CLI 인터페이스 지원
- 독립 실행 및 Java 연동 가능

### 2. Spring Boot 서비스
- **FundNavCrawlerService**: 크롤링 로직 관리
- **FundCrawlerController**: 수동 크롤링 API
- **FundNavCrawlerScheduler**: 자동 스케줄링

### 3. 데이터베이스
- **fund_master**: 모펀드 정보
- **fund_class**: 자펀드(클래스) 정보
- **fund_nav**: 펀드 기준가 이력

## 🔧 설치 및 설정

### 1. Python 환경 설정

```bash
cd backend/hanainplan/crawler

# 가상환경 생성 (선택사항)
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt
```

### 2. Spring Boot 설정

`application.yml`에 다음 설정이 추가되어 있습니다:

```yaml
fund:
  crawler:
    python-path: python3  # Python 실행 경로
    script-path: backend/hanainplan/crawler/fund_crawler.py
    timeout-seconds: 10  # 크롤링 타임아웃 (초)
```

### 3. Python 경로 확인

```bash
which python3
# 또는
which python
```

경로가 다르면 `application.yml`의 `python-path` 수정

## 🚀 사용 방법

### 1. Python 크롤러 단독 테스트

```bash
cd backend/hanainplan/crawler

# 테스트 실행 (하드코딩된 펀드)
python3 fund_crawler.py

# 특정 펀드 크롤링
python3 fund_crawler.py 513031 51303C
# 출력: 10523.45
```

### 2. Spring Boot 애플리케이션 실행

**⚠️ 중요: 하나은행 서버를 먼저 실행하세요!**

```bash
# 터미널 1: 하나은행 서버 실행
cd backend/banks/hana
./gradlew bootRun
# 포트 8081에서 실행됨

# 터미널 2: HanaInPlan 서버 실행
cd backend/hanainplan
./gradlew bootRun
# 포트 8080에서 실행됨
# 서버 시작 10초 후 자동으로 초기 크롤링이 실행됩니다 (개발용)
```

### 3. API를 통한 수동 크롤링

**모든 펀드 크롤링:**
```bash
curl -X POST http://localhost:8080/api/funds/crawler/crawl-all
```

**특정 펀드 크롤링:**
```bash
curl -X POST http://localhost:8080/api/funds/crawler/crawl/51303C
```

### 4. 하나은행 서버에서 기준가 확인

**최신 기준가 조회:**
```bash
curl http://localhost:8081/api/hana/fund-nav/51303C/latest
```

**특정 날짜 기준가 조회:**
```bash
curl http://localhost:8081/api/hana/fund-nav/51303C/2025-10-15
```

## ⏰ 스케줄 설정

### 자동 스케줄

**매일 오후 6시 (운영용)**
```java
@Scheduled(cron = "0 0 18 * * ?")
```

**서버 시작 10초 후 (개발용)**
```java
@Scheduled(initialDelay = 10000)
```

### 스케줄 비활성화

운영 환경에서 개발용 스케줄을 비활성화하려면:

`FundNavCrawlerScheduler.java`에서 `initialCrawlForDevelopment()` 메서드를 주석 처리:

```java
// @Scheduled(initialDelay = 10000)
// public void initialCrawlForDevelopment() {
//     ...
// }
```

또는 프로파일 설정으로 제어:

```java
@Profile("local")
@Scheduled(initialDelay = 10000)
public void initialCrawlForDevelopment() {
    ...
}
```

## 📡 API 문서

### POST /api/funds/crawler/crawl-all

모든 펀드의 기준가를 크롤링합니다.

**Response:**
```json
{
  "totalCount": 28,
  "successCount": 27,
  "failureCount": 1,
  "failedFunds": [
    {
      "fundCd": "513031",
      "childFundCd": "51303X",
      "fundName": "미래에셋테스트펀드",
      "errorMessage": "기준가를 찾을 수 없음"
    }
  ],
  "startTime": "2025-10-15 18:00:00",
  "endTime": "2025-10-15 18:02:35",
  "durationSeconds": 155,
  "successRate": 96.42
}
```

### POST /api/funds/crawler/crawl/{childFundCd}

특정 펀드의 기준가만 크롤링합니다.

**Request:**
- Path Variable: `childFundCd` (자펀드 코드)

**Response:**
```json
{
  "childFundCd": "51303C",
  "nav": 10523.45,
  "message": "크롤링 성공"
}
```

## 🔍 크롤링 동작 방식

1. **데이터베이스 조회**: `fund_class` 테이블에서 모든 펀드 클래스 목록 조회
2. **URL 생성**: 각 펀드별로 크롤링 URL 동적 생성
   ```
   https://investments.miraeasset.com/magi/fund/view.do?
   fundGb=2&fundCd={모펀드코드}&childFundCd={자펀드코드}&childFundGb=2
   ```
3. **Python 실행**: Java에서 Python 스크립트를 subprocess로 실행
4. **기준가 추출**: HTML 파싱하여 기준가 추출
5. **DB 저장 (hanainplan 서버)**: 
   - 오늘 날짜의 기준가가 이미 있으면 **UPDATE**
   - 없으면 새로 **INSERT**
6. **하나은행 서버 동기화**: 
   - `POST /api/hana/fund-nav` API 호출
   - 하나은행 DB에도 동일한 기준가 저장
   - 동기화 실패 시에도 크롤링은 계속 진행
7. **이력 관리**: 날짜별로 1개씩 레코드가 쌓여 자동으로 이력 관리

## ⚠️ 주의사항

### 1. 서버 실행 순서
**중요: 반드시 하나은행 서버를 먼저 실행해야 합니다!**

```bash
# 1. 하나은행 서버 실행 (포트 8081)
cd backend/banks/hana
./gradlew bootRun

# 2. HanaInPlan 서버 실행 (포트 8080)
cd backend/hanainplan
./gradlew bootRun
```

하나은행 서버가 실행되지 않은 상태에서 크롤링이 실행되면:
- 크롤링 자체는 성공
- 하나은행 동기화만 실패 (로그에 경고 기록)
- 전체 크롤링은 정상 진행

### 2. 크롤링 딜레이
- 서버 부하 방지를 위해 각 펀드 크롤링 사이 0.5초 딜레이
- 28개 펀드 기준 약 2~3분 소요

### 3. 타임아웃
- 각 펀드당 10초 타임아웃
- 타임아웃 발생 시 해당 펀드는 건너뛰고 다음 펀드 진행

### 4. 트랜잭션
- 각 펀드별로 개별 트랜잭션 처리
- 일부 펀드 실패해도 전체 크롤링은 계속 진행

### 5. 에러 처리
- 크롤링 실패 시 상세 로그 기록
- 실패한 펀드 목록 응답에 포함
- 하나은행 동기화 실패는 경고로 기록하되 크롤링은 계속 진행

## 🐛 문제 해결

### Python을 찾을 수 없다는 오류

```
Cannot run program "python3": error=2, No such file or directory
```

**해결:**
1. Python 설치 확인: `which python3`
2. `application.yml`의 `python-path` 수정
3. 절대 경로 사용: `/usr/bin/python3`

### 크롤링 스크립트를 찾을 수 없다는 오류

```
FileNotFoundException: backend/hanainplan/crawler/fund_crawler.py
```

**해결:**
1. 프로젝트 루트에서 실행하는지 확인
2. `application.yml`의 `script-path`를 절대 경로로 변경

### 기준가를 찾을 수 없다는 오류

```
ERROR: 기준가를 찾을 수 없습니다.
```

**원인:**
- 펀드 페이지 구조 변경
- 잘못된 펀드 코드
- 펀드가 판매 중단됨

**해결:**
1. 펀드 페이지 직접 접속하여 확인
2. HTML 구조 변경 여부 확인
3. `fund_crawler.py`의 셀렉터 업데이트

### 크롤링이 너무 느림

**해결:**
1. 딜레이 조정: `FundNavCrawlerService.java`의 `Thread.sleep(500)` 값 감소
2. 병렬 처리 도입 (추후 개선 과제)

## 📊 로그 확인

크롤링 진행 상황은 로그에서 확인:

```
[INFO] ====================================================
[INFO] 펀드 기준가 크롤링 시작: 2025-10-15 18:00:00
[INFO] ====================================================
[INFO] 총 28개의 펀드 클래스를 크롤링합니다.
[INFO] ✓ 크롤링 성공: 미래에셋글로벌그레이트컨슈머증권자투자신탁(주식) C클래스 - 기준가: 10523.45
[WARN] ✗ 크롤링 실패: 테스트펀드 - 기준가를 찾을 수 없음
[INFO] ====================================================
[INFO] 펀드 기준가 크롤링 완료: 2025-10-15 18:02:35
[INFO] 총 28건 / 성공 27건 / 실패 1건 / 성공률 96.42%
[INFO] 소요 시간: 155초
[INFO] ====================================================
```

## 🔮 향후 개선 사항

- [ ] 병렬 크롤링으로 속도 개선
- [ ] 재시도 로직 추가
- [ ] 크롤링 실패 알림 기능
- [ ] 다른 자산운용사 지원 확장
- [ ] 기준가 변동률 계산 및 저장
- [ ] 크롤링 통계 대시보드

## 🏗️ 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                    HanaInPlan 서버 (8080)                        │
│                                                                  │
│  ┌────────────────────┐    ┌──────────────────────┐            │
│  │  FundNavCrawler    │───→│  Python 크롤러        │            │
│  │  Scheduler         │    │  (fund_crawler.py)   │            │
│  │  (매일 18:00)       │    └──────────────────────┘            │
│  └────────────────────┘              │                          │
│           │                          │                          │
│           ↓                          ↓                          │
│  ┌────────────────────┐    ┌──────────────────────┐            │
│  │  FundNavCrawler    │───→│  미래에셋 웹사이트     │            │
│  │  Service           │    │  (크롤링 대상)        │            │
│  └────────────────────┘    └──────────────────────┘            │
│           │                                                     │
│           ↓                                                     │
│  ┌────────────────────┐                                        │
│  │  FundNav           │                                        │
│  │  Repository        │                                        │
│  │  (hanainplan DB)   │                                        │
│  └────────────────────┘                                        │
│           │                                                     │
│           │ 동기화 (REST API)                                   │
│           ↓                                                     │
└───────────│─────────────────────────────────────────────────────┘
            │
            │ POST /api/hana/fund-nav
            │
┌───────────↓─────────────────────────────────────────────────────┐
│                    하나은행 서버 (8081)                          │
│                                                                  │
│  ┌────────────────────┐                                        │
│  │  FundNavController │                                        │
│  └────────────────────┘                                        │
│           │                                                     │
│           ↓                                                     │
│  ┌────────────────────┐                                        │
│  │  FundNavService    │                                        │
│  └────────────────────┘                                        │
│           │                                                     │
│           ↓                                                     │
│  ┌────────────────────┐                                        │
│  │  FundNav           │                                        │
│  │  Repository        │                                        │
│  │  (hana DB)         │                                        │
│  └────────────────────┘                                        │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## 📝 관련 파일

### HanaInPlan 서버
```
backend/hanainplan/
├── crawler/
│   ├── fund_crawler.py          # Python 크롤러
│   ├── requirements.txt         # Python 의존성
│   └── README.md               # 이 문서
├── src/main/java/com/hanainplan/
│   ├── config/
│   │   └── RestTemplateConfig.java  # RestTemplate 설정
│   └── domain/fund/
│       ├── controller/
│       │   └── FundCrawlerController.java
│       ├── service/
│       │   └── FundNavCrawlerService.java  # 크롤링 + 하나은행 동기화
│       ├── scheduler/
│       │   └── FundNavCrawlerScheduler.java
│       ├── dto/
│       │   └── FundNavCrawlResult.java
│       ├── entity/
│       │   ├── FundMaster.java
│       │   ├── FundClass.java
│       │   └── FundNav.java
│       └── repository/
│           ├── FundClassRepository.java
│           └── FundNavRepository.java
└── src/main/resources/
    └── application.yml          # 설정 (크롤러, 하나은행 URL)
```

### 하나은행 서버
```
backend/banks/hana/
└── src/main/java/com/hanainplan/hana/fund/
    ├── controller/
    │   └── FundNavController.java    # 기준가 업데이트 API
    ├── service/
    │   └── FundNavService.java      # 기준가 저장/업데이트 로직
    ├── dto/
    │   └── FundNavUpdateRequest.java
    ├── entity/
    │   └── FundNav.java
    └── repository/
        └── FundNavRepository.java
```

## 📞 문의

시스템 관련 문의사항이 있으시면 개발팀에 연락주세요.

