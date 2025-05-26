package org.example;

import org.example.socket.ChatSocketServer;
import org.example.utils.DatabaseInitializer;

public class App {
    public static void main(String[] args) {
        // 서버 시작 옵션 확인
        boolean startServer = false;
        for (String arg : args) {
            if (arg.equals("--start-server")) {
                startServer = true;
                break;
            }
        }

        // 서버 모드로 시작
        if (startServer) {
            try {
                System.out.println("채팅 서버 모드로 시작합니다...");

                // 데이터베이스 초기화
                boolean initialized = DatabaseInitializer.initialize();
                if (!initialized) {
                    System.err.println("데이터베이스 초기화 실패. 서버를 시작할 수 없습니다.");
                    return;
                }

                // 채팅 소켓 서버 시작
                ChatSocketServer server = ChatSocketServer.getInstance();
                server.start();

                // 종료 시 서버 정리
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("서버 종료 중...");
                    server.stop();
                }));

                System.out.println("채팅 서버가 시작되었습니다. 종료하려면 Ctrl+C를 누르세요.");
            } catch (Exception e) {
                System.err.println("서버 시작 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // 클라이언트 모드로 시작 (JavaFX 애플리케이션)
            JavaFX.main(args);
        }
    }
}