FROM openjdk:17-jdk-slim

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
RUN ./gradlew build --no-daemon -x test

# JAR 파일 실행
EXPOSE 8080
CMD ["java", "-jar", "build/libs/hanainplan-0.0.1-SNAPSHOT.jar"]