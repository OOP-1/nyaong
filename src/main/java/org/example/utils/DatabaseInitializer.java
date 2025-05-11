package org.example.utils;

import org.example.boundary.DatabaseConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 데이터베이스 초기화 유틸리티
 * 애플리케이션 시작 시 필요한 테이블을 생성합니다.
 */
public class DatabaseInitializer {

    /**
     * 필요한 데이터베이스 테이블을 생성합니다.
     * @return 초기화 성공 여부
     */
    public static boolean initialize() {
        try (Connection connection = DatabaseConnector.getConnection();
             Statement statement = connection.createStatement()) {

            // Members 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS Members (" +
                    "member_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id VARCHAR(50) NOT NULL," +
                    "password VARCHAR(100) NOT NULL," +
                    "nickname VARCHAR(50) NOT NULL," +
                    "status VARCHAR(20)," +
                    "role VARCHAR(20) NOT NULL," +
                    "UNIQUE KEY unique_user_id (user_id)" +
                    ")");

            // Friends 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS Friends (" +
                    "member_id INT NOT NULL," +
                    "friend_id INT NOT NULL," +
                    "PRIMARY KEY (member_id, friend_id)," +
                    "FOREIGN KEY (member_id) REFERENCES Members(member_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (friend_id) REFERENCES Members(member_id) ON DELETE CASCADE" +
                    ")");

            // FriendRequests 테이블 생성 (소개 메시지 필드 추가)
            statement.execute("CREATE TABLE IF NOT EXISTS FriendRequests (" +
                    "request_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sender_id INT NOT NULL," +
                    "receiver_id INT NOT NULL," +
                    "message TEXT," +  // 소개 메시지 필드 추가
                    "status VARCHAR(20) NOT NULL," + // PENDING, ACCEPTED, REJECTED
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (sender_id) REFERENCES Members(member_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (receiver_id) REFERENCES Members(member_id) ON DELETE CASCADE" +
                    ")");

            // ChatRooms 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS ChatRooms (" +
                    "chatroom_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "chatroom_name VARCHAR(100) NOT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // ChatRoomMembers 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS ChatRoomMembers (" +
                    "chatroom_id INT NOT NULL," +
                    "member_id INT NOT NULL," +
                    "PRIMARY KEY (chatroom_id, member_id)," +
                    "FOREIGN KEY (chatroom_id) REFERENCES ChatRooms(chatroom_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (member_id) REFERENCES Members(member_id) ON DELETE CASCADE" +
                    ")");

            // Messages 테이블 생성
            statement.execute("CREATE TABLE IF NOT EXISTS Messages (" +
                    "message_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "message_content TEXT NOT NULL," +
                    "chatroom_id INT NOT NULL," +
                    "member_id INT NOT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (chatroom_id) REFERENCES ChatRooms(chatroom_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (member_id) REFERENCES Members(member_id) ON DELETE CASCADE" +
                    ")");

            System.out.println("데이터베이스 테이블이 성공적으로 생성되었습니다.");
            return true;

        } catch (SQLException e) {
            System.err.println("데이터베이스 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}