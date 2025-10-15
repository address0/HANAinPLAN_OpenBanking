import io
import re
import base64
import os
from typing import List, Optional, Dict, Any
from datetime import datetime
from enum import Enum

from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from starlette.responses import JSONResponse

from pdf2image import convert_from_bytes
from google.cloud import vision
from google.api_core import client_options as client_options_lib
from PIL import Image, ImageFilter
import pymysql
from dotenv import load_dotenv

load_dotenv()

GOOGLE_VISION_API_KEY = os.getenv('GOOGLE_VISION_API_KEY')
if GOOGLE_VISION_API_KEY:
    print(f"✅ Google Cloud Vision API 키 로드 완료")
else:
    print("⚠️  경고: GOOGLE_VISION_API_KEY 환경 변수가 설정되지 않았습니다.")
    print("   .env 파일에 GOOGLE_VISION_API_KEY=your_api_key 형식으로 설정해주세요.")

app = FastAPI(title="HANAinPLAN OCR + Masking Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class DocumentType(str, Enum):
    EMPLOYEE_ID = "employee_id"
    EMPLOYMENT_CONTRACT = "employment_contract"
    IDENTITY_VERIFICATION = "identity_verification"
    QUALIFICATION_CERT = "qualification_cert"

class ExtractedInfo(BaseModel):
    document_type: DocumentType
    name: Optional[str] = None
    social_number_front: Optional[str] = None
    gender: Optional[str] = None
    employee_id: Optional[str] = None
    branch_code: Optional[str] = None
    branch_name: Optional[str] = None
    department: Optional[str] = None
    position: Optional[str] = None
    license_type: Optional[str] = None
    license_number: Optional[str] = None
    license_issue_date: Optional[str] = None
    hire_date: Optional[str] = None
    phone_number: Optional[str] = None
    address: Optional[str] = None
    issue_date: Optional[str] = None
    raw_text: str = ""

class CounselorRegistrationData(BaseModel):
    name: str
    social_number: str
    gender: str
    phone_number: str
    employee_id: str
    branch_code: Optional[str] = None
    branch_name: Optional[str] = None
    department: Optional[str] = None
    position: Optional[str] = None
    license_type: Optional[str] = None
    license_number: Optional[str] = None
    license_issue_date: Optional[str] = None
    hire_date: Optional[str] = None

def get_db_connection():
    """MySQL 데이터베이스 연결"""
    return pymysql.connect(
        host=os.getenv('DB_HOST', 'localhost'),
        port=int(os.getenv('DB_PORT', 3306)),
        user=os.getenv('MYSQL_USER', 'hanainplan'),
        password=os.getenv('MYSQL_PASSWORD', 'hanainplan123'),
        database=os.getenv('MYSQL_DATABASE', 'hanainplan'),
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )

RE_PATTERNS = {
    "rrn_hyphen": re.compile(r"\b\d{6}-\d{7}\b"),
    "rrn13": re.compile(r"\b\d{13}\b"),
    "rrn_front": re.compile(r"\b(\d{6})[-\s]?[1-4]\d{6}\b"),
    "creditcard": re.compile(r"\b(?:\d[ -]*?){13,19}\b"),
    "phone": re.compile(r"\b01[0|1|6|7|8|9][- ]?\d{3,4}[- ]?\d{4}\b"),
    "email": re.compile(r"[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+"),
    "bank": re.compile(r"\b\d{8,16}\b"),
    "date_yyyymmdd": re.compile(r"\b(19|20)\d{2}[년\-\./ ](0?[1-9]|1[0-2])[월\-\./ ](0?[1-9]|[12]\d|3[01])일?\b"),
    "date_yymmdd": re.compile(r"\b\d{2}[년\-\./ ](0?[1-9]|1[0-2])[월\-\./ ](0?[1-9]|[12]\d|3[01])일?\b"),
    "address": re.compile(r"(서울특?별?시|부산광역시|대구광역시|인천광역시|광주광역시|대전광역시|울산광역시|세종특별자치시|경기도|강원특?별?자?치?도|충청북도|충청남도|전라북도|전북특별자치도|전라남도|경상북도|경상남도|제주특별자치도)\s+[가-힣]+[시군구]\s+[가-힣\s]+[동로가길읍면리번지]\s*\d*[-\s]*\d*"),
    "address_simple": re.compile(r"(서울|부산|대구|인천|광주|대전|울산|세종|경기|강원|충북|충남|전북|전남|경북|경남|제주)[^\n]{10,50}[동로가길읍면리]"),
}

def mask_rrn(match: re.Match) -> str:
    """주민번호 마스킹: 앞 6자리 + 뒷자리 1자까지 보이도록"""
    s = match.group(0)
    if "-" in s:
        return s[:8] + "******"
    else:
        return s[:6] + "-" + s[6] + "******"

def mask_creditcard(match: re.Match) -> str:
    s = re.sub(r"[ -]", "", match.group(0))
    if len(s) <= 8:
        return "*" * len(s)
    return s[:4] + "-" + ("*" * (len(s) - 8)) + "-" + s[-4:]

def mask_phone(match: re.Match) -> str:
    s = match.group(0)
    digits = re.sub(r"[^\d]", "", s)
    if len(digits) >= 7:
        return digits[:-4] + "-" + "****"
    return "****"

def mask_email(match: re.Match) -> str:
    s = match.group(0)
    local, _, domain = s.partition("@")
    if len(local) <= 2:
        return "*" + "@" + domain
    return local[0] + ("*" * (len(local)-2)) + local[-1] + "@" + domain

def mask_bank(match: re.Match) -> str:
    s = match.group(0)
    if len(s) <= 4:
        return "*" * len(s)
    return s[:2] + ("*" * (len(s)-4)) + s[-2:]

def mask_address(match: re.Match) -> str:
    """주소 마스킹: 시/도까지만 표시하고 나머지는 마스킹"""
    s = match.group(0)
    parts = s.split()
    if parts:
        return parts[0] + " " + ("*" * min(15, len(s) - len(parts[0])))
    return "*" * min(15, len(s))

def mask_text_all(text: str) -> str:
    """모든 민감정보를 마스킹"""
    text = RE_PATTERNS["rrn_hyphen"].sub(mask_rrn, text)
    text = RE_PATTERNS["rrn13"].sub(mask_rrn, text)
    text = RE_PATTERNS["creditcard"].sub(mask_creditcard, text)
    text = RE_PATTERNS["phone"].sub(mask_phone, text)
    text = RE_PATTERNS["email"].sub(mask_email, text)
    text = RE_PATTERNS["bank"].sub(mask_bank, text)
    text = RE_PATTERNS["address"].sub(mask_address, text)
    text = RE_PATTERNS["address_simple"].sub(mask_address, text)
    return text

def is_sensitive_token(token: str) -> bool:
    """토큰이 민감정보인지 판별"""
    if RE_PATTERNS["rrn_hyphen"].search(token) or RE_PATTERNS["rrn13"].search(token):
        return True
    if RE_PATTERNS["creditcard"].search(token):
        return True
    if RE_PATTERNS["phone"].search(token):
        return True
    if RE_PATTERNS["email"].search(token):
        return True
    if RE_PATTERNS["bank"].search(token):
        return True
    if RE_PATTERNS["address"].search(token) or RE_PATTERNS["address_simple"].search(token):
        if not re.match(r"^(서울|부산|대구|인천|광주|대전|울산|세종|경기|강원|충북|충남|전북|전남|경북|경남|제주)", token):
            if len(token) > 3:
                return True
    return False

def blur_region(pil_img: Image.Image, box: List[dict], blur_radius=12):
    """특정 영역을 블러 처리"""
    xs = [v["x"] for v in box if "x" in v]
    ys = [v["y"] for v in box if "y" in v]
    if not xs or not ys:
        return
    left, upper, right, lower = int(min(xs)), int(min(ys)), int(max(xs)), int(max(ys))
    left = max(0, left)
    upper = max(0, upper)
    right = min(pil_img.width, right)
    lower = min(pil_img.height, lower)
    if right <= left or lower <= upper:
        return
    region = pil_img.crop((left, upper, right, lower))
    blurred = region.filter(ImageFilter.GaussianBlur(radius=blur_radius))
    pil_img.paste(blurred, (left, upper))

def extract_name(text: str) -> Optional[str]:
    """이름 추출 (한글 2-4자) - 표 형식 및 라벨 없는 경우도 처리"""
    exclude_words = [
        '하나은행', '국민은행', '신한은행', '우리은행', '농협은행', 'KB은행',
        '증명서', '계약서', '재직증명', '재직', '직원증', '사원증',
        '근로계약', '고용계약', '자격증', '본점', '지점', '주민등록증', '주민등록',
        '운전면허', '면허증', '금융', '상담', '대리', '과장', '부장', '팀장',
        '입사', '연월일', '생년월일', '생년', '월일', '일생', '발급', '유효', '기간', 
        '서울특별', '특별시', '광역시', '주소', '전화', '휴대', '연락', '소속', '직위', '직급',
        '중구', '을지로', '강남구', '서초구', '종로구', '올지로'
    ]

    patterns = [
        r"성\s*명[:\s]+([가-힣]{2,4})",
        r"성명[:\s]+([가-힣]{2,4})",
        r"이름[:\s]+([가-힣]{2,4})",
        r"직원명[:\s]+([가-힣]{2,4})",
        r"성\s*명\s+([가-힣]{2,4})",
    ]

    for pattern in patterns:
        match = re.search(pattern, text, re.MULTILINE)
        if match:
            name = match.group(1).strip()
            if name not in exclude_words and 2 <= len(name) <= 4:
                if not any(word in name for word in ['증명', '계약', '재직', '근로', '등록', '면허', '특별']):
                    return name

    rrn_match = re.search(r"\d{6}[-\s]?[1-4]\d{6}", text)
    if rrn_match:
        before_rrn = text[:rrn_match.start()][-100:]

        name_patterns = [
            r"([가-힣]{2,4})\s*\([^)]+\)",
            r"([가-힣]{2,4})\s*\n",
            r"([가-힣]{2,4})$",
        ]

        for pattern in name_patterns:
            matches = re.findall(pattern, before_rrn)
            if matches:
                for name in reversed(matches):
                    name = name.strip()
                    if name not in exclude_words and 2 <= len(name) <= 4:
                        if not any(word in name for word in ['주민', '등록', '번호', '생년', '월일', '서울', '부산', '특별', '광역']):
                            return name

    all_korean = re.findall(r"[가-힣]{2,4}", text)
    for name in all_korean:
        if name not in exclude_words and 2 <= len(name) <= 4:
            if not any(word in name for word in ['증명', '계약', '재직', '근로', '등록', '면허', '특별', '광역', '주민', '은행', '지점']):
                return name

    return None

def extract_social_number(text: str) -> tuple[Optional[str], Optional[str]]:
    """주민번호 앞자리와 성별 추출 (앞 6자리 + 뒷자리 1자 표시)"""
    rrn_pattern = re.search(r"(\d{6})[-\s]?([1-4])\d{6}", text)
    if rrn_pattern:
        front = rrn_pattern.group(1)
        gender_digit = rrn_pattern.group(2)
        gender = "M" if gender_digit in ["1", "3"] else "F"
        display_rrn = f"{front}-{gender_digit}"
        return display_rrn, gender
    return None, None

def extract_employee_id(text: str) -> Optional[str]:
    """직원번호 추출 (사원증 특화)"""
    patterns = [
        r"직원[번]?호[:\s]*([A-Z0-9가-힣]{4,15})",
        r"사원[번]?호[:\s]*([A-Z0-9가-힣]{4,15})",
        r"ID[:\s]*([A-Z0-9]{6,12})",
        r"HANA(\d{6,10})",
        r"Employee\s*ID[:\s]*([A-Z0-9]{6,12})",
        r"NO\.?\s*([A-Z0-9]{6,12})",
    ]
    for pattern in patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            emp_id = match.group(1).strip()
            if not re.search(r'[가-힣]', emp_id):
                return emp_id
    return None

def extract_branch_info(text: str) -> tuple[Optional[str], Optional[str]]:
    """지점 정보 추출 (사원증/근로계약서/재직증명서 특화)"""
    branch_patterns = [
        r"(하나은행|KB국민은행|신한은행)\s*([가-힣]{2,6})\s*지점",
        r"(하나은행|KB국민은행|신한은행)\s*(본점)",
        r"소\s*속[:\s]*([가-힣]{2,6})\s*지점",
        r"소속[:\s]*([가-힣]{2,6})\s*지점",
        r"근무\s*지점[:\s]*([가-힣]{2,6})\s*지점",
        r"발급\s*지점[:\s]*([가-힣]{2,6})\s*지점",
        r"Branch[:\s]*([가-힣]{2,6})",
    ]

    for pattern in branch_patterns:
        match = re.search(pattern, text)
        if match:
            groups = match.groups()
            branch_name = groups[-1] if groups else None
            if branch_name and len(branch_name) >= 2:
                if branch_name == '본점':
                    return None, "본점"
                exclude_words = ['하나은행', '국민은행', '신한은행', '증명서', '계약서']
                if branch_name not in exclude_words:
                    return None, f"{branch_name}지점" if branch_name != '본점' else "본점"
    return None, None

def extract_department_position(text: str) -> tuple[Optional[str], Optional[str]]:
    """부서 및 직급 추출"""
    department = None
    position = None

    dept_patterns = [
        r"부서[:\s]*([가-힣]+)",
        r"소속[:\s]*([가-힣]+)부",
        r"소\s*속[:\s]*([가-힣]+)",
    ]
    for pattern in dept_patterns:
        match = re.search(pattern, text)
        if match:
            dept = match.group(1).strip()
            if len(dept) >= 2 and dept not in ['하나은행', '국민은행', '신한은행']:
                department = dept
                break

    position_patterns = [
        r"직\s*위[:\s]*(사원|주임|대리|과장|차장|부장|팀장|이사|상무|전무|부사장|사장)",
        r"직급[:\s]*(사원|주임|대리|과장|차장|부장|팀장|이사|상무|전무|부사장|사장)",
        r"직책[:\s]*(사원|주임|대리|과장|차장|부장|팀장|이사|상무|전무|부사장|사장)",
    ]
    for pattern in position_patterns:
        match = re.search(pattern, text)
        if match:
            position = match.group(1)
            break

    return department, position

def extract_license_info(text: str) -> tuple[Optional[str], Optional[str], Optional[str]]:
    """자격증 정보 추출"""
    license_type = None
    license_number = None
    issue_date = None

    type_patterns = [
        r"(금융투자분석사|재무설계사|보험계리사|공인중개사|투자자산운용사|펀드투자권유자문인력|파생상품투자권유자문인력)",
        r"자격증[:\s]*([가-힣]+)",
    ]
    for pattern in type_patterns:
        match = re.search(pattern, text)
        if match:
            license_type = match.group(1)
            break

    number_patterns = [
        r"자격[증]?\s*[번]?호[:\s]*([A-Z0-9\-]{6,20})",
        r"등록[번]?호[:\s]*([A-Z0-9\-]{6,20})",
    ]
    for pattern in number_patterns:
        match = re.search(pattern, text)
        if match:
            license_number = match.group(1)
            break

    date_match = re.search(RE_PATTERNS["date_yyyymmdd"], text)
    if date_match:
        issue_date = date_match.group(0)

    return license_type, license_number, issue_date

def extract_hire_date(text: str) -> Optional[str]:
    """입사일 추출 (근로계약서/재직증명서 특화)"""
    patterns = [
        r"입사\s*연\s*월\s*일[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"입사연월일[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"입사[일]?[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"근무\s*시작[일]?[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"채용[일]?[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"계약\s*시작[일]?[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"발령[일]?[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
    ]
    for pattern in patterns:
        match = re.search(pattern, text)
        if match:
            return match.group(1)
    return None

def extract_phone_number(text: str) -> Optional[str]:
    """연락처 추출 (마스킹 전 원본)"""
    patterns = [
        r"연락처[:\s]*(01[0-9][- ]?\d{3,4}[- ]?\d{4})",
        r"전화[번호]?[:\s]*(01[0-9][- ]?\d{3,4}[- ]?\d{4})",
        r"휴대[전화]?[:\s]*(01[0-9][- ]?\d{3,4}[- ]?\d{4})",
        r"Mobile[:\s]*(01[0-9][- ]?\d{3,4}[- ]?\d{4})",
    ]
    for pattern in patterns:
        match = re.search(pattern, text)
        if match:
            return match.group(1)
    return None

def extract_address(text: str) -> Optional[str]:
    """주소 추출 (원본, 마스킹 없음)"""
    address_match = RE_PATTERNS["address"].search(text)
    if address_match:
        return address_match.group(0)

    address_match = RE_PATTERNS["address_simple"].search(text)
    if address_match:
        return address_match.group(0)

    return None

def extract_issue_date(text: str) -> Optional[str]:
    """발급일 추출 (주민등록증 등)"""
    patterns = [
        r"발급[일자]?[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"발급일[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"발행[일자]?[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
        r"발행일[:\s]*((?:19|20)\d{2}[년\-\./ ](?:0?[1-9]|1[0-2])[월\-\./ ](?:0?[1-9]|[12]\d|3[01])일?)",
    ]
    for pattern in patterns:
        match = re.search(pattern, text)
        if match:
            return match.group(1)
    return None

def identify_document_type(text: str) -> DocumentType:
    """문서 타입 식별"""
    text_lower = text.lower()

    if any(keyword in text for keyword in ["직원증", "사원증", "employee id", "id card", "사번"]):
        return DocumentType.EMPLOYEE_ID
    elif any(keyword in text for keyword in ["재직증명서", "재직증명", "근로계약서", "고용계약서", "employment", "입사연월일"]):
        return DocumentType.EMPLOYMENT_CONTRACT
    elif any(keyword in text for keyword in ["자격증", "자격증명", "certificate", "qualification"]) and "재직" not in text:
        return DocumentType.QUALIFICATION_CERT
    elif any(keyword in text for keyword in ["주민등록증", "운전면허증", "신분증", "identity"]):
        return DocumentType.IDENTITY_VERIFICATION
    else:
        return DocumentType.IDENTITY_VERIFICATION

def extract_info_from_text(text: str, doc_type: DocumentType) -> ExtractedInfo:
    """문서 타입에 따라 정보 추출 (타입별 특화 처리)"""
    info = ExtractedInfo(document_type=doc_type, raw_text=text)

    info.name = extract_name(text)
    social_front, gender = extract_social_number(text)
    info.social_number_front = social_front
    info.gender = gender

    if doc_type == DocumentType.EMPLOYEE_ID:
        branch_code, branch_name = extract_branch_info(text)
        info.branch_code = branch_code
        info.branch_name = branch_name
        department, position = extract_department_position(text)
        info.department = department
        info.position = position
        info.phone_number = extract_phone_number(text)

    elif doc_type == DocumentType.EMPLOYMENT_CONTRACT:
        info.hire_date = extract_hire_date(text)
        branch_code, branch_name = extract_branch_info(text)
        info.branch_code = branch_code
        info.branch_name = branch_name
        department, position = extract_department_position(text)
        info.department = department
        info.position = position

    elif doc_type == DocumentType.IDENTITY_VERIFICATION:
        info.address = extract_address(text)
        info.issue_date = extract_issue_date(text)

    elif doc_type == DocumentType.QUALIFICATION_CERT:
        license_type, license_number, issue_date = extract_license_info(text)
        info.license_type = license_type
        info.license_number = license_number
        info.license_issue_date = issue_date

    return info

@app.get("/")
async def root():
    return {"message": "HANAinPLAN OCR Service", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

@app.post("/api/ocr/extract")
async def extract_and_mask(file: UploadFile = File(...)):
    """
    문서(PDF/이미지)를 받아서:
     1. OCR로 텍스트 추출
     2. 민감정보 마스킹
     3. 문서 타입별 정보 추출
     4. 마스킹된 PDF 반환
    """
    allowed_types = [
        "application/pdf",
        "application/octet-stream",
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/webp"
    ]

    if file.content_type not in allowed_types:
        raise HTTPException(
            status_code=400, 
            detail="PDF 또는 이미지 파일(JPG, PNG, WEBP)을 업로드해 주세요."
        )

    contents = await file.read()
    if not contents:
        raise HTTPException(status_code=400, detail="빈 파일입니다.")

    try:
        if file.content_type in ["image/jpeg", "image/jpg", "image/png", "image/webp"]:
            pil_image = Image.open(io.BytesIO(contents))
            pil_pages: List[Image.Image] = [pil_image]
        else:
            try:
                pil_pages: List[Image.Image] = convert_from_bytes(contents, dpi=300)
            except Exception as pdf_error:
                print(f"PDF 변환 오류: {pdf_error}")
                raise HTTPException(
                    status_code=500, 
                    detail=f"PDF 변환 실패. Poppler가 설치되어 있는지 확인해주세요. 오류: {str(pdf_error)}"
                )
    except HTTPException:
        raise
    except Exception as e:
        print(f"파일 변환 오류: {e}")
        raise HTTPException(status_code=500, detail=f"파일 변환 실패: {str(e)}")
    if not GOOGLE_VISION_API_KEY:
        raise HTTPException(status_code=500, detail="Google Vision API 키가 설정되지 않았습니다.")

    client_options = client_options_lib.ClientOptions(api_key=GOOGLE_VISION_API_KEY)
    client = vision.ImageAnnotatorClient(client_options=client_options)

    masked_pages = []
    aggregated_text = []
    extracted_documents = []

    for page_idx, pil_page in enumerate(pil_pages):
        buf = io.BytesIO()
        pil_page_rgb = pil_page.convert("RGB")
        pil_page_rgb.save(buf, format="JPEG")
        image_bytes = buf.getvalue()

        image = vision.Image(content=image_bytes)
        response = client.document_text_detection(image=image)

        if response.error.message:
            raise HTTPException(status_code=500, detail=f"Vision API error: {response.error.message}")

        full_text = response.full_text_annotation.text if response.full_text_annotation else ""
        aggregated_text.append(full_text)

        doc_type = identify_document_type(full_text)
        extracted_info = extract_info_from_text(full_text, doc_type)
        extracted_documents.append(extracted_info.model_dump())

        masked_text = mask_text_all(full_text)

        pil_masked = pil_page_rgb.copy()
        annotations = response.text_annotations
        for ann in annotations[1:]:
            token = ann.description
            if is_sensitive_token(token):
                verts = []
                for v in ann.bounding_poly.vertices:
                    verts.append({"x": getattr(v, "x", 0), "y": getattr(v, "y", 0)})
                blur_region(pil_masked, verts, blur_radius=12)

        masked_pages.append((pil_masked, masked_text))

    masked_text_pages = [p[1] for p in masked_pages]
    combined_masked_text = "\n\n--- PAGE BREAK ---\n\n".join(masked_text_pages)

    pdf_bytes_io = io.BytesIO()
    images_for_pdf = [p[0].convert("RGB") for p in masked_pages]
    if len(images_for_pdf) == 0:
        raise HTTPException(status_code=500, detail="이미지 페이지가 없습니다.")

    try:
        if len(images_for_pdf) == 1:
            images_for_pdf[0].save(pdf_bytes_io, format="PDF")
        else:
            images_for_pdf[0].save(pdf_bytes_io, format="PDF", save_all=True, append_images=images_for_pdf[1:])
        pdf_bytes = pdf_bytes_io.getvalue()
    except Exception as e:
        print(f"PDF 생성 오류: {e}")
        raise HTTPException(status_code=500, detail=f"PDF 생성 실패: {str(e)}")
    pdf_b64 = base64.b64encode(pdf_bytes).decode("utf-8")

    masked_images_base64 = []
    for pil_masked, _ in masked_pages:
        img_buffer = io.BytesIO()
        pil_masked.save(img_buffer, format='PNG')
        img_base64 = base64.b64encode(img_buffer.getvalue()).decode('utf-8')
        masked_images_base64.append(img_base64)

    return JSONResponse({
        "success": True,
        "masked_text": combined_masked_text,
        "masked_pdf_base64": pdf_b64,
        "masked_images": masked_images_base64,
        "extracted_info": extracted_documents,
        "pages": len(masked_pages),
        "message": "문서 처리 완료"
    })

@app.post("/api/ocr/register-counselor")
async def register_counselor(data: CounselorRegistrationData):
    """
    추출된 정보를 바탕으로 상담사 정보를 MySQL에 저장
    """
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        birth_year = int(data.social_number[:2])
        birth_month = int(data.social_number[2:4])
        birth_day = int(data.social_number[4:6])

        gender_digit = int(data.social_number[6])
        if gender_digit in [1, 2]:
            birth_year += 1900
        elif gender_digit in [3, 4]:
            birth_year += 2000

        birth_date = f"{birth_year:04d}-{birth_month:02d}-{birth_day:02d}"

        user_sql = """
            INSERT INTO tb_user 
            (user_type, user_name, social_number, phone_number, birth_date, gender, 
             login_type, is_phone_verified, is_active, created_date, updated_date)
            VALUES 
            ('COUNSELOR', %s, %s, %s, %s, %s, 'PASSWORD', TRUE, TRUE, NOW(), NOW())
        """

        cursor.execute(user_sql, (
            data.name,
            data.social_number,
            data.phone_number,
            birth_date,
            data.gender
        ))

        user_id = cursor.lastrowid

        consultant_sql = """
            INSERT INTO tb_consultant 
            (consultant_id, employee_id, branch_code, branch_name, department, position,
             license_type, license_number, license_issue_date, hire_date,
             work_status, created_date, updated_date)
            VALUES 
            (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 'ACTIVE', NOW(), NOW())
        """

        license_issue_date = None
        if data.license_issue_date:
            try:
                date_str = re.sub(r'[년월일\s]', '-', data.license_issue_date).strip('-')
                license_issue_date = date_str
            except:
                pass

        hire_date = None
        if data.hire_date:
            try:
                date_str = re.sub(r'[년월일\s]', '-', data.hire_date).strip('-')
                hire_date = date_str
            except:
                pass

        cursor.execute(consultant_sql, (
            user_id,
            data.employee_id,
            data.branch_code,
            data.branch_name,
            data.department,
            data.position,
            data.license_type,
            data.license_number,
            license_issue_date,
            hire_date
        ))

        conn.commit()
        cursor.close()
        conn.close()

        return JSONResponse({
            "success": True,
            "user_id": user_id,
            "message": "상담사 등록이 완료되었습니다."
        })

    except pymysql.IntegrityError as e:
        return JSONResponse({
            "success": False,
            "message": f"중복된 정보가 있습니다: {str(e)}"
        }, status_code=400)
    except Exception as e:
        return JSONResponse({
            "success": False,
            "message": f"등록 중 오류가 발생했습니다: {str(e)}"
        }, status_code=500)

@app.post("/api/ocr/verify-documents")
async def verify_documents(
    employee_id_doc: UploadFile = File(...),
    employment_contract: UploadFile = File(...),
    identity_doc: UploadFile = File(...),
    qualification_cert: Optional[UploadFile] = File(None)
):
    """
    4개의 문서를 한번에 검증하고 정보를 추출 (PDF 또는 이미지)
    """
    documents = [
        ("employee_id", employee_id_doc),
        ("employment_contract", employment_contract),
        ("identity_verification", identity_doc),
    ]

    if qualification_cert:
        documents.append(("qualification_cert", qualification_cert))

    all_extracted_info = {}

    for doc_name, doc_file in documents:
        if not doc_file:
            continue

        contents = await doc_file.read()
        if not contents:
            continue

        try:
            if doc_file.content_type in ["image/jpeg", "image/jpg", "image/png", "image/webp"]:
                pil_image = Image.open(io.BytesIO(contents))
                pil_pages = [pil_image]
            else:
                pil_pages = convert_from_bytes(contents, dpi=300)
        except Exception as e:
            continue

        if not GOOGLE_VISION_API_KEY:
            continue

        client_options = client_options_lib.ClientOptions(api_key=GOOGLE_VISION_API_KEY)
        client = vision.ImageAnnotatorClient(client_options=client_options)

        for pil_page in pil_pages:
            buf = io.BytesIO()
            pil_page_rgb = pil_page.convert("RGB")
            pil_page_rgb.save(buf, format="JPEG")
            image_bytes = buf.getvalue()

            image = vision.Image(content=image_bytes)
            response = client.document_text_detection(image=image)

            if response.error.message:
                continue

            full_text = response.full_text_annotation.text if response.full_text_annotation else ""

            doc_type = DocumentType(doc_name) if doc_name in [e.value for e in DocumentType] else identify_document_type(full_text)
            extracted_info = extract_info_from_text(full_text, doc_type)
            all_extracted_info[doc_name] = extracted_info.model_dump()

    merged_info = {
        "name": None,
        "social_number_front": None,
        "gender": None,
        "employee_id": None,
        "branch_name": None,
        "department": None,
        "position": None,
        "license_type": None,
        "license_number": None,
        "hire_date": None,
    }

    for doc_name, info in all_extracted_info.items():
        for key in merged_info.keys():
            if info.get(key) and not merged_info[key]:
                merged_info[key] = info[key]

    return JSONResponse({
        "success": True,
        "merged_info": merged_info,
        "individual_documents": all_extracted_info,
        "message": "문서 검증 완료"
    })

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8090)