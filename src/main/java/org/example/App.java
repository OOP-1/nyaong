package org.example;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
    public static void main(String[] args) {
        // .env 파일에서 환경변수 로딩
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()  // .env 파일이 없어도 오류 발생하지 않음
                .load();

        // 시스템 환경변수로 설정
        if (dotenv.get("DB_URL") != null) {
            System.setProperty("DB_URL", dotenv.get("DB_URL"));
        }
        if (dotenv.get("DB_USER") != null) {
            System.setProperty("DB_USER", dotenv.get("DB_USER"));
        }
        if (dotenv.get("DB_PASSWORD") != null) {
            System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        }
        if (dotenv.get("DB_DRIVER") != null) {
            System.setProperty("DB_DRIVER", dotenv.get("DB_DRIVER"));
        }

        System.out.println("MySQL 데이터베이스에 연결하는 애플리케이션을 시작합니다.");
        JavaFX.main(args);
    }
}