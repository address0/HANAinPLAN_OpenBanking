# HANAinPLAN OCR Service

상담사 회원가입을 위한 문서 OCR 및 민감정보 마스킹 서비스입니다.

## 주요 기능

1. **문서 OCR 및 정보 추출**
   - 직원증: 직원번호, 이름, 지점정보
   - 근로계약서: 지점, 직급, 입사일
   - 신분증: 이름, 주민번호 앞자리, 성별
   - 자격증: 자격증 종류, 번호, 발급일

2. **민감정보 마스킹**
   - 주민번호 뒷자리 마스킹
   - 전화번호, 이메일, 계좌번호 마스킹
   - 이미지 블러 처리

3. **MySQL 데이터 저장**
   - 추출된 정보를 hanainplan DB에 저장
   - User 및 Consultant 테이블에 자동 등록

## 설치 및 실행

### 1. 환경 설정

```bash
# 가상환경 생성 (선택사항)
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 패키지 설치
pip install -r requirements.txt
```

### 2. 환경 변수 설정

`.env` 파일 생성:

```bash
cp .env.example .env
```

`.env` 파일 수정:
- MySQL 접속 정보 설정
- Google Cloud Vision API 키 파일 경로 설정

### 3. Google Cloud Vision API 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
2. Vision API 활성화
3. 서비스 계정 생성 및 키 다운로드 (JSON 파일)
4. 환경 변수에 키 파일 경로 설정:

```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/key.json"
```

또는 `.env` 파일에 추가

### 4. 서버 실행

```bash
python main.py
```

또는

```bash
uvicorn main:app --host 0.0.0.0 --port 8090 --reload
```

서버가 `http://localhost:8090`에서 실행됩니다.

## API 엔드포인트

### 1. 문서 OCR 및 마스킹

**POST** `/api/ocr/extract`

PDF 문서를 업로드하여 OCR 처리 및 민감정보 마스킹

**Request:**
- `file`: PDF 파일 (multipart/form-data)

**Response:**
```json
{
  "success": true,
  "masked_text": "마스킹된 텍스트...",
  "masked_pdf_base64": "base64 인코딩된 마스킹된 PDF",
  "extracted_info": [
    {
      "document_type": "employee_id",
      "name": "홍길동",
      "employee_id": "HANA001234",
      "branch_name": "강남지점",
      "confidence": 0.8
    }
  ],
  "pages": 1
}
```

### 2. 상담사 등록

**POST** `/api/ocr/register-counselor`

추출된 정보를 바탕으로 상담사를 MySQL에 등록

**Request Body:**
```json
{
  "name": "홍길동",
  "social_number": "9001011234567",
  "gender": "M",
  "phone_number": "010-1234-5678",
  "employee_id": "HANA001234",
  "branch_name": "강남지점",
  "department": "영업부",
  "position": "대리",
  "license_type": "재무설계사",
  "license_number": "FP-2023-001",
  "license_issue_date": "2023-01-01",
  "hire_date": "2020-03-01"
}
```

**Response:**
```json
{
  "success": true,
  "user_id": 123,
  "message": "상담사 등록이 완료되었습니다."
}
```

### 3. 문서 일괄 검증

**POST** `/api/ocr/verify-documents`

4개의 문서를 한번에 업로드하여 정보 추출

**Request:**
- `employee_id_doc`: 직원증 PDF
- `employment_contract`: 근로계약서 PDF
- `identity_doc`: 신분증 PDF
- `qualification_cert`: 자격증 PDF (선택)

**Response:**
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

## 프론트엔드 연동 예시

```typescript
// 1. 문서 OCR 처리
async function uploadAndExtractDocument(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch('http://localhost:8090/api/ocr/extract', {
    method: 'POST',
    body: formData
  });
  
  const data = await response.json();
  return data.extracted_info[0];
}

// 2. 4개 문서 일괄 처리
async function verifyAllDocuments(files: {
  employeeId: File,
  contract: File,
  identity: File,
  qualification?: File
}) {
  const formData = new FormData();
  formData.append('employee_id_doc', files.employeeId);
  formData.append('employment_contract', files.contract);
  formData.append('identity_doc', files.identity);
  if (files.qualification) {
    formData.append('qualification_cert', files.qualification);
  }
  
  const response = await fetch('http://localhost:8090/api/ocr/verify-documents', {
    method: 'POST',
    body: formData
  });
  
  return await response.json();
}

// 3. 상담사 등록
async function registerCounselor(data: CounselorData) {
  const response = await fetch('http://localhost:8090/api/ocr/register-counselor', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
  
  return await response.json();
}
```

## 지원 파일 형식

- **PDF**: 여러 페이지 문서
- **이미지**: JPG, JPEG, PNG, WEBP

## 주의사항

1. **Google Cloud Vision API 비용**: API 호출마다 비용이 발생할 수 있습니다.
2. **파일 크기**: 10MB 이하 권장
3. **이미지 품질**: 명확한 스캔 또는 촬영 권장 (300 DPI 이상)
4. **민감정보 처리**: 마스킹된 PDF는 별도로 저장하지 않고 즉시 반환됩니다.
5. **데이터베이스**: MySQL 연결 정보가 정확해야 합니다.

## 문제 해결

### Google Vision API 인증 오류
```bash
# 환경 변수 확인
echo $GOOGLE_APPLICATION_CREDENTIALS

# 키 파일 경로 설정
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/key.json"
```

### MySQL 연결 오류
- `.env` 파일의 데이터베이스 접속 정보 확인
- MySQL 서버가 실행 중인지 확인
- 방화벽 설정 확인

### 문서 인식 정확도 향상
- PDF 해상도를 300 DPI 이상으로 스캔
- 문서가 선명하고 글자가 명확한지 확인
- 문서 방향이 올바른지 확인

## 라이선스

이 프로젝트는 HANAinPLAN 프로젝트의 일부입니다.

