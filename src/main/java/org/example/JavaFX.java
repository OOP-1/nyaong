package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.example.model.Member;
import org.example.service.AuthService;
import org.example.socket.ChatSocketClient;
import org.example.utils.DatabaseInitializer;
import org.example.view.LoginView;

public class JavaFX extends Application {

    // 두 번째 창인지 확인하는 플래그
    private static boolean isSecondWindow = false;

    @Override
    public void start(Stage stage) {
        try {
            // 데이터베이스 초기화
            boolean dbInitialized = DatabaseInitializer.initialize();
            if (!dbInitialized) {
                System.err.println("데이터베이스 초기화에 실패했습니다. 기본 화면을 표시합니다.");
                showDefaultView(stage);
                return;
            }

            // 두 번째 창이면 창 위치와 제목 조정
            if (isSecondWindow) {
                stage.setX(800); // 오른쪽으로 이동
                stage.setTitle("Nyaong Chat - 두 번째 창");
            }

            // 로그인 화면 표시
            LoginView loginView = new LoginView(stage);
            loginView.show();

            // 애플리케이션 종료 시 소켓 연결 종료
            stage.setOnCloseRequest(event -> {
                cleanupResources();
            });

        } catch (Exception e) {
            System.err.println("애플리케이션 시작 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 기본 화면 표시
            showDefaultView(stage);
        }
    }

    // 오류 발생시 기본 화면 표시 (기존 코드와 유사한 화면)
    private void showDefaultView(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        javafx.scene.control.Label label = new javafx.scene.control.Label(
                "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".\n" +
                        "데이터베이스 연결에 실패했습니다. .env 파일을 확인해주세요.");
        Scene scene = new Scene(new javafx.scene.layout.StackPane(label), 640, 480);
        stage.setTitle("Nyaong Chat - 오류");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 애플리케이션 종료 시 자원 정리
     */
    private void cleanupResources() {
        try {
            // 로그인 상태인 경우 로그아웃
            if (AuthService.isLoggedIn()) {
                Member currentUser = AuthService.getCurrentUser();
                System.out.println("사용자 " + currentUser.getNickname() + " 로그아웃 중...");

                // 소켓 연결 종료
                ChatSocketClient socketClient = ChatSocketClient.getInstance();
                if (socketClient.isConnected()) {
                    socketClient.disconnect();
                }

                // 로그아웃 처리
                AuthService authService = new AuthService();
                authService.logout();
            }
        } catch (Exception e) {
            System.err.println("애플리케이션 종료 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 애플리케이션 종료
     */
    @Override
    public void stop() {
        cleanupResources();
        Platform.exit();
    }

    public static void main(String[] args) {
        // 커맨드 라인 인자 확인
        for (String arg : args) {
            if (arg.equals("--second-window")) {
                isSecondWindow = true;
                break;
            }
        }

        launch(args);
    }
}