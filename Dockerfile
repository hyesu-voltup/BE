# 1단계: 빌드 스테이지
FROM amazoncorretto:17-alpine AS build
WORKDIR /app
COPY . .
# 실행 권한 부여
RUN chmod +x gradlew
# 테스트 제외하고 빌드 (현재는 빠른 배포 필요)
RUN ./gradlew clean bootJar -x test

# 2단계: 실행 스테이지
FROM amazoncorretto:17-alpine
WORKDIR /app
# 빌드 단계에서 생성된 jar만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Render 기본 포트
EXPOSE 10000

# 배포 환경(prod) 프로필 적용 및 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Dserver.port=10000", "-jar", "app.jar"]