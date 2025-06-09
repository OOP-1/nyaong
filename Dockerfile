# 베이스 이미지로 OpenJDK 21을 사용합니다. 더 낮은 버전이 필요하다면 변경하세요.
FROM openjdk:21-jdk-slim


ARG JAR_NAME=nyaong_dev-1.0-SNAPSHOT-all.jar
ARG JAR_PATH=./build/libs/${JAR_NAME}

WORKDIR /app

COPY ${JAR_PATH} /app/app.jar
COPY .env ./

EXPOSE 9000

# 애플리케이션을 실행합니다.
CMD ["java", "-jar", "/app/app.jar",  "--start-server"]