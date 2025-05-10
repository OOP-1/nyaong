package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.utils.DatabaseInitializer;
import org.example.view.LoginView;

public class JavaFX extends Application {

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

            // 로그인 화면 표시
            LoginView loginView = new LoginView(stage);
            loginView.show();

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

    public static void main(String[] args) {
        launch();
    }
}