package org.example.socket;

import org.example.boundary.DatabaseConnector;
import org.example.utils.DatabaseInitializer;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 채팅 서버 시작 클래스
 * 서버 애플리케이션의 진입점입니다.
 */
public class ChatServerLauncher {

    public static void main(String[] args) {
        System.out.println("채팅 서버 시작 중...");

        // 데이터베이스 초기화 확인
        if (!initializeDatabase()) {
            System.err.println("데이터베이스 초기화 실패. 서버를 시작할 수 없습니다.");
            return;
        }

        // 채팅 서버 시작
        ChatSocketServer server = ChatSocketServer.getInstance();
        server.start();

        // 종료 시 서버 정리
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("서버 종료 중...");
            server.stop();
        }));

        System.out.println("채팅 서버가 시작되었습니다. 종료하려면 Ctrl+C를 누르세요.");
    }

    /**
     * 데이터베이스 연결 및 초기화 확인
     */
    private static boolean initializeDatabase() {
        try {
            // 데이터베이스 연결 테스트
            Connection connection = DatabaseConnector.getConnection();
            if (connection == null) {
                System.err.println("데이터베이스 연결 실패");
                return false;
            }
            connection.close();

            // 데이터베이스 스키마 초기화
            boolean initialized = DatabaseInitializer.initialize();
            if (!initialized) {
                System.err.println("데이터베이스 초기화 실패");
                return false;
            }

            System.out.println("데이터베이스 연결 및 초기화 성공");
            return true;
        } catch (SQLException e) {
            System.err.println("데이터베이스 오류: " + e.getMessage());
            return false;
        }
    }
}