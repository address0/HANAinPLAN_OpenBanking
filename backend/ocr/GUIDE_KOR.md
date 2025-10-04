# HANAinPLAN OCR 서비스 - 한국어 가이드

## 📋 개요

상담사 회원가입 시 필요한 문서(직원증, 근로계약서, 신분증, 자격증)를 OCR로 처리하여 정보를 자동으로 추출하고, 민감정보를 마스킹하는 서비스입니다.

## 🎯 주요 기능

### 1. 문서별 정보 자동 추출

- **직원증**: 직원번호, 이름, 지점정보, 부서, 직급
- **근로계약서**: 지점명, 직급, 입사일, 부서
- **신분증**: 이름, 주민번호 앞자리, 성별
- **자격증**: 자격증 종류, 번호, 발급일

### 2. 민감정보 자동 마스킹

- 주민번호 뒷자리 → `*******`
- 전화번호 뒷자리 → `****`
- 이메일 중간 부분 → `a***b@domain.com`
- 계좌번호 중간 부분 마스킹
- 이미지에도 블러 처리 적용

### 3. 데이터베이스 자동 저장

- 추출된 정보를 hanainplan MySQL에 자동 저장
- `tb_user` 및 `tb_consultant` 테이블에 등록

## 🚀 빠른 시작

### 1단계: 환경 설정

```bash
cd backend/ocr

# 설정 스크립트 실행 (자동 설정)
chmod +x setup.sh
./setup.sh
```

또는 수동 설정:

```bash
# 가상환경 생성
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 패키지 설치
pip install -r requirements.txt

# .env 파일 생성
cp .env.example .env  # 또는 직접 생성
```

### 2단계: Google Cloud Vision API 설정

#### 2-1. Google Cloud Console 접속
[https://console.cloud.google.com/](https://console.cloud.google.com/)

#### 2-2. 프로젝트 생성
1. 상단 프로젝트 선택 → "새 프로젝트"
2. 프로젝트 이름: `hanainplan-ocr` (원하는 이름)
3. 생성 클릭

#### 2-3. Vision API 활성화
1. 왼쪽 메뉴 → "API 및 서비스" → "라이브러리"
2. "Cloud Vision API" 검색
3. "사용" 버튼 클릭

#### 2-4. API 키 생성
1. 왼쪽 메뉴 → "API 및 서비스" → "사용자 인증 정보"
2. "사용자 인증 정보 만들기" → "API 키" 선택
3. API 키가 생성됨 (예: `AIzaSyD...`)
4. (선택) "키 제한" 버튼을 눌러 보안 설정
   - API 제한사항 → "키 제한" → "Cloud Vision API" 선택

#### 2-5. 환경 변수 설정

`.env` 파일 수정:
```bash
GOOGLE_VISION_API_KEY=your_actual_api_key_here
```

예시:
```bash
GOOGLE_VISION_API_KEY=AIzaSyDxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 3단계: MySQL 데이터베이스 확인

```bash
# MySQL 접속 확인
mysql -u hanainplan -p

# 데이터베이스 확인
USE hanainplan;
SHOW TABLES;

# tb_user, tb_consultant 테이블이 존재해야 함
```

`.env` 파일에서 DB 정보 확인:
```env
DB_HOST=localhost
DB_PORT=3306
MYSQL_USER=hanainplan
MYSQL_PASSWORD=hanainplan123
MYSQL_DATABASE=hanainplan
```

### 4단계: 서버 실행

```bash
# 가상환경 활성화 (아직 안 했다면)
source venv/bin/activate

# 서버 실행
python main.py

# 또는 개발 모드로 실행 (자동 재시작)
uvicorn main:app --host 0.0.0.0 --port 8090 --reload
```

서버가 정상적으로 실행되면:
```
INFO:     Started server process
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8090
```

### 5단계: 테스트

브라우저에서 접속:
```
http://localhost:8090
```

응답:
```json
{
  "message": "HANAinPLAN OCR Service",
  "version": "1.0.0"
}
```

헬스체크:
```
http://localhost:8090/health
```

## 📡 API 사용법

### 지원 파일 형식
- **PDF**: 여러 페이지 문서 지원
- **이미지**: JPG, JPEG, PNG, WEBP

### 1. 단일 문서 OCR 처리

**엔드포인트**: `POST /api/ocr/extract`

**요청 (cURL):**
```bash
# PDF 파일
curl -X POST "http://localhost:8090/api/ocr/extract" \
  -F "file=@employee_id.pdf"

# 이미지 파일
curl -X POST "http://localhost:8090/api/ocr/extract" \
  -F "file=@employee_id.jpg"
```

**요청 (JavaScript/TypeScript):**
```typescript
const formData = new FormData();
formData.append('file', pdfFile);

const response = await fetch('http://localhost:8090/api/ocr/extract', {
  method: 'POST',
  body: formData
});

const data = await response.json();
console.log(data.extracted_info);
```

**응답:**
```json
{
  "success": true,
  "masked_text": "이름: 홍길동\n직원번호: HANA001234\n...",
  "masked_pdf_base64": "JVBERi0xLjQKJeLjz9...",
  "extracted_info": [
    {
      "document_type": "employee_id",
      "name": "홍길동",
      "employee_id": "HANA001234",
      "branch_name": "강남지점",
      "confidence": 0.85
    }
  ],
  "pages": 1
}
```

### 2. 전체 문서 일괄 검증

**엔드포인트**: `POST /api/ocr/verify-documents`

**요청:**
```bash
# PDF 또는 이미지 파일 모두 가능
curl -X POST "http://localhost:8090/api/ocr/verify-documents" \
  -F "employee_id_doc=@직원증.pdf" \
  -F "employment_contract=@근로계약서.jpg" \
  -F "identity_doc=@신분증.png" \
  -F "qualification_cert=@자격증.pdf"
```

**응답:**
```json
{
  "success": true,
  "merged_info": {
    "name": "홍길동",
    "social_number_front": "900101",
    "gender": "M",
    "employee_id": "HANA001234",
    "branch_name": "강남지점",
    "department": "영업부",
    "position": "대리"
  },
  "individual_documents": {
    "employee_id": {...},
    "employment_contract": {...},
    "identity_verification": {...}
  }
}
```

### 3. 상담사 등록

**엔드포인트**: `POST /api/ocr/register-counselor`

**요청:**
```json
{
  "name": "홍길동",
  "social_number": "9001011234567",
  "gender": "M",
  "phone_number": "010-1234-5678",
  "employee_id": "HANA001234",
  "branch_name": "강남지점",
  "department": "영업부",
  "position": "대리"
}
```

**응답:**
```json
{
  "success": true,
  "user_id": 123,
  "message": "상담사 등록이 완료되었습니다."
}
```

## 🎨 프론트엔드 연동

프론트엔드에서는 이미 `ocrService.ts` 파일이 생성되어 있습니다.

### 사용 예시

```typescript
import { 
  extractDocumentInfo, 
  verifyAllDocuments, 
  registerCounselor 
} from '@/services/ocrService';

// 1. 단일 문서 처리
const handleFileUpload = async (file: File) => {
  try {
    const result = await extractDocumentInfo(file);
    console.log('추출된 정보:', result.extracted_info);
    
    // 마스킹된 PDF 다운로드
    downloadMaskedPDF(result.masked_pdf_base64, 'masked_document.pdf');
  } catch (error) {
    console.error('OCR 오류:', error);
  }
};

// 2. 전체 문서 검증
const handleVerifyAll = async () => {
  try {
    const result = await verifyAllDocuments({
      employeeId: files.employeeId,
      employmentContract: files.contract,
      identityVerification: files.identity,
      qualificationCert: files.qualification
    });
    
    console.log('통합 정보:', result.merged_info);
  } catch (error) {
    console.error('검증 오류:', error);
  }
};

// 3. 상담사 등록
const handleRegister = async (data: CounselorData) => {
  try {
    const result = await registerCounselor(data);
    console.log('등록 완료:', result.user_id);
  } catch (error) {
    console.error('등록 오류:', error);
  }
};
```

### Step5CounselorVerification 컴포넌트

이미 OCR 기능이 통합되어 있습니다:

1. **파일 업로드 시 자동 OCR 처리**
2. **추출된 정보 실시간 표시**
3. **전체 문서 일괄 검증 버튼**
4. **처리 중 로딩 표시**

## 🐳 Docker로 실행 (선택사항)

### Docker Compose 사용

```bash
# 이미지 빌드 및 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f ocr-service

# 중지
docker-compose down
```

### 주의사항

- `google-credentials.json` 파일을 OCR 디렉토리에 위치시켜야 합니다
- Docker에서 호스트의 MySQL에 접근하려면 `host.docker.internal` 사용

## 🔧 문제 해결

### 1. Google Vision API 인증 오류

**오류:**
```
Google Vision API 키가 설정되지 않았습니다.
```

**해결:**
```bash
# .env 파일 확인
cat .env | grep GOOGLE_VISION_API_KEY

# API 키가 올바르게 설정되었는지 확인
# .env 파일에 다음과 같이 입력되어 있어야 함:
GOOGLE_VISION_API_KEY=AIzaSyDxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**API 키 권한 오류:**
```
Permission denied: Cloud Vision API has not been used in project...
```

**해결:**
- Google Cloud Console에서 Vision API가 활성화되었는지 확인
- API 키에 Vision API 사용 권한이 있는지 확인

### 2. MySQL 연결 오류

**오류:**
```
pymysql.err.OperationalError: (2003, "Can't connect to MySQL server...")
```

**해결:**
```bash
# MySQL 서버 상태 확인
mysql.server status  # Mac
sudo service mysql status  # Linux

# MySQL 시작
mysql.server start  # Mac
sudo service mysql start  # Linux

# DB 접속 정보 확인
mysql -u hanainplan -p
```

### 3. PDF 변환 오류

**오류:**
```
PDFInfoNotInstalledError: Unable to get page count. Is poppler installed and in PATH?
```

**해결:**
```bash
# Mac
brew install poppler

# Ubuntu/Debian
sudo apt-get install poppler-utils

# Windows
# https://github.com/oschwartz10612/poppler-windows/releases/ 에서 다운로드
# PATH에 bin 폴더 추가
```

### 4. 문서 인식 정확도가 낮을 때

**해결 방법:**
- **PDF**: 스캔 해상도를 300 DPI 이상으로 설정
- **이미지**: 고해상도로 촬영 (최소 1920x1080 권장)
- 문서가 선명하고 글자가 명확한지 확인
- 문서 방향이 올바른지 확인
- 조명이 균일하게 비춘 상태에서 스캔/촬영
- 그림자나 반사가 없는지 확인

### 5. CORS 오류

**오류:**
```
Access to fetch at 'http://localhost:8090/api/ocr/extract' from origin 'http://localhost:5173' has been blocked by CORS policy
```

**해결:**
`main.py`의 CORS 설정 확인:
```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],  # 프론트엔드 URL 추가
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

## 📊 테스트 데이터

테스트용 샘플 PDF를 만들려면:

```python
# test_create_sample.py
from reportlab.pdfgen import canvas

def create_employee_id():
    c = canvas.Canvas("test_employee_id.pdf")
    c.drawString(100, 750, "하나은행 직원증")
    c.drawString(100, 700, "성명: 홍길동")
    c.drawString(100, 670, "직원번호: HANA001234")
    c.drawString(100, 640, "소속: 강남지점")
    c.drawString(100, 610, "직급: 대리")
    c.save()

create_employee_id()
```

## 💰 비용 안내

### Google Cloud Vision API 요금

- **무료 할당량**: 월 1,000건까지 무료
- **초과 시**: 1,000건당 $1.50

**예상 비용:**
- 상담사 1명당 4개 문서 = 4 API 호출
- 월 250명 가입 시 = 1,000 API 호출 (무료 범위)
- 월 1,000명 가입 시 = 4,000 API 호출 = 약 $4.50

자세한 요금: [https://cloud.google.com/vision/pricing](https://cloud.google.com/vision/pricing)

## 🔒 보안 주의사항

1. **Google API 키 보호**
   - `.gitignore`에 `google-credentials.json` 추가
   - 키 파일을 절대 Git에 커밋하지 않기

2. **민감정보 처리**
   - 마스킹된 PDF는 클라이언트로 즉시 반환만 하고 서버에 저장하지 않음
   - 원본 PDF는 처리 후 즉시 메모리에서 삭제

3. **데이터베이스 접근**
   - `.env` 파일의 DB 비밀번호 보호
   - 프로덕션 환경에서는 강력한 비밀번호 사용

## 📝 라이선스

이 프로젝트는 HANAinPLAN 프로젝트의 일부입니다.

## 🤝 지원

문제가 발생하면 이슈를 등록하거나 팀에 문의하세요.

