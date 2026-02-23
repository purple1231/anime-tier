# 1단계: 빌드 스테이지 (Gradle 빌드 수행)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Gradle 래퍼와 설정 파일들을 먼저 복사 (캐싱 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 의존성 먼저 다운로드
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 JAR 빌드
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

# 2단계: 실행 스테이지 (실제 컨테이너 동작)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 가져옴
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 사용할 포트 설정
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]