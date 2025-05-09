package org.example.boundary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    // MySQL 설정
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/nyaong?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "user";
    private static final String PASSWORD = "User@12345";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
            return null;
        }
    }
}