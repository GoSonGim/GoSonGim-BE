# 빌드 스테이지 (Linux x86_64 플랫폼 지정)
FROM --platform=linux/amd64 gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 빌드 설정
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.caching=true -Dorg.gradle.parallel=true"

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (재시도 포함)
RUN for i in 1 2 3; do \
        echo "Attempt $i: Downloading dependencies..." && \
        gradle dependencies --no-daemon && break || \
        (echo "Attempt $i failed, retrying in 5 seconds..." && sleep 5); \
    done || exit 1

COPY src ./src

# 빌드 실행
RUN gradle bootJar --no-daemon -x test

# 실행 스테이지 (Linux x86_64 플랫폼 지정)
FROM --platform=linux/amd64 eclipse-temurin:17-jre
WORKDIR /app

# 타임존 설정
RUN apt-get update && apt-get install -y curl tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# non-root 사용자
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

