FROM openjdk:17-jdk-slim

# Python 설치 (크롤러용)
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle gradle

# 의존성 파일 복사 및 다운로드
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src

# Python 크롤러 복사 및 의존성 설치
COPY crawler crawler
RUN pip3 install --no-cache-dir -r crawler/requirements.txt

RUN ./gradlew build --no-daemon -x test

# JAR 파일 실행
EXPOSE 8080
CMD ["java", "-jar", "build/libs/hanainplan-0.0.1-SNAPSHOT.jar"]