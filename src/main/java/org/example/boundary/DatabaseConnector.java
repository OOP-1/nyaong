package org.example.boundary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.example.config.EnvLoader;

public class DatabaseConnector {
    // 환경변수에서 데이터베이스 접속 정보 가져오기
    private static final String JDBC_URL = EnvLoader.get("DB_URL");
    private static final String USER = EnvLoader.get("DB_USER");
    private static final String PASSWORD = EnvLoader.get("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        try {
            // 드라이버 이름도 환경변수로 설정 가능
            String driverClassName = EnvLoader.get("DB_DRIVER");
            Class.forName(driverClassName);

            return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
            return null;
        }
    }
}