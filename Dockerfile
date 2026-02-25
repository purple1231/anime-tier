# ===== 1) Build stage: Gradle로 jar 빌드 =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle 캐시 효율 위해 먼저 설정/래퍼만 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
# (있으면) gradle.properties도 복사


# 의존성 다운로드 (소스 복사 전에 실행하면 캐시 잘 먹음)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 이제 소스 전체 복사 후 jar 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# ===== 2) Runtime stage: 실행만 하는 가벼운 이미지 =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드 결과 jar 복사
COPY --from=build /workspace/build/libs/*.jar app.jar

# 스프링부트 기본 포트
EXPOSE 8080

# 컨테이너 실행 시 JVM 옵션 주입 가능하게
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]