package org.example.boundary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    // 환경변수에서 데이터베이스 접속 정보 가져오기
    private static final String JDBC_URL = System.getenv("DB_URL") != null ?
            System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/nyaong?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = System.getenv("DB_USER") != null ?
            System.getenv("DB_USER") : "user";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ?
            System.getenv("DB_PASSWORD") : "User@12345";

    public static Connection getConnection() throws SQLException {
        try {
            // 드라이버 이름도 환경변수로 설정 가능
            String driverClassName = System.getenv("DB_DRIVER") != null ?
                    System.getenv("DB_DRIVER") : "com.mysql.cj.jdbc.Driver";
            Class.forName(driverClassName);

            return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
            return null;
        }
    }
}