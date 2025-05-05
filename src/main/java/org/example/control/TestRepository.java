package org.example.control;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.example.boundary.DatabaseConnector;

// Repository 예제
public class TestRepository {
    public void test() {
        try {
            Connection connection = DatabaseConnector.getConnection();
            System.out.println("H2 데이터베이스 연결 성공!");

            // 테이블 생성 예제
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, name VARCHAR(255))");

            // 데이터 삽입 예제
            statement.execute("INSERT INTO users VALUES (1, '사용자1')");
            statement.execute("INSERT INTO users VALUES (2, '사용자2')");

            // 데이터 조회 예제
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println("ID: " + id + ", 이름: " + name);
            }

            // 리소스 정리
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
