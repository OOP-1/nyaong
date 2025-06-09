# 베이스 이미지로 OpenJDK 21을 사용합니다. 더 낮은 버전이 필요하다면 변경하세요.
FROM openjdk:21-jdk-slim

# 작업 디렉토리를 설정합니다.
WORKDIR /app

# Gradle Wrapper 및 프로젝트 파일을 복사합니다.
# 빌드 캐싱을 위해 필요한 파일만 먼저 복사하는 것이 좋습니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .

# 소스 코드를 복사합니다.
COPY src src

# 애플리케이션 빌드
# 여기서는 'run' 태스크를 사용하므로 별도의 'build' 태스크는 필요 없을 수 있습니다.
# 하지만 의존성 다운로드 및 초기 설정을 위해 한 번 실행하는 것이 좋습니다.
RUN ./gradlew dependencies

# 9000번 포트를 외부에 노출합니다.
EXPOSE 9000

# 애플리케이션을 실행합니다.
# ./gradlew run --args="--start-server" 명령어를 사용합니다.
CMD ["./gradlew", "run", "--args=--start-server"]