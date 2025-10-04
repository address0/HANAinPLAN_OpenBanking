#!/bin/bash

echo "============================================"
echo "HANAinPLAN OCR 서비스 설정 스크립트"
echo "============================================"
echo ""

# 1. .env 파일 생성
if [ ! -f .env ]; then
    echo "📝 .env 파일 생성 중..."
    cat > .env << 'EOF'
# MySQL 데이터베이스 설정
DB_HOST=localhost
DB_PORT=3306
MYSQL_USER=hanainplan
MYSQL_PASSWORD=hanainplan123
MYSQL_DATABASE=hanainplan

# Google Cloud Vision API 설정
# Google Cloud 서비스 계정 키 파일 경로 설정
GOOGLE_APPLICATION_CREDENTIALS=./google-credentials.json

# 서버 설정
PORT=8090
EOF
    echo "✅ .env 파일이 생성되었습니다."
else
    echo "⚠️  .env 파일이 이미 존재합니다."
fi

echo ""

# 2. 가상환경 생성
if [ ! -d "venv" ]; then
    echo "🐍 Python 가상환경 생성 중..."
    python3 -m venv venv
    echo "✅ 가상환경이 생성되었습니다."
else
    echo "⚠️  가상환경이 이미 존재합니다."
fi

echo ""

# 3. 가상환경 활성화 및 패키지 설치
echo "📦 패키지 설치 중..."
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt

echo ""
echo "============================================"
echo "✅ 설정이 완료되었습니다!"
echo "============================================"
echo ""
echo "⚠️  다음 단계를 진행해주세요:"
echo ""
echo "1. Google Cloud Vision API 설정"
echo "   - Google Cloud Console에서 프로젝트 생성"
echo "   - Vision API 활성화"
echo "   - 서비스 계정 생성 및 키 다운로드"
echo "   - 키 파일을 'google-credentials.json'으로 저장"
echo ""
echo "2. MySQL 데이터베이스 확인"
echo "   - MySQL이 실행 중인지 확인"
echo "   - hanainplan 데이터베이스가 존재하는지 확인"
echo "   - .env 파일의 DB 접속 정보 확인"
echo ""
echo "3. 서버 실행"
echo "   $ source venv/bin/activate"
echo "   $ python main.py"
echo ""
echo "   또는"
echo ""
echo "   $ uvicorn main:app --host 0.0.0.0 --port 8090 --reload"
echo ""
echo "============================================"

