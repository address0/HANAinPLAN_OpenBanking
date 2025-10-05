FROM python:3.11-slim

# 시스템 패키지 설치 (poppler-utils는 pdf2image에 필요)
RUN apt-get update && apt-get install -y \
    poppler-utils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Python 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY main.py .

# 포트 노출
EXPOSE 8090

# 환경 변수 설정 (기본값)
ENV PORT=8090
ENV DB_HOST=localhost
ENV DB_PORT=3306

# 서버 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8090"]


