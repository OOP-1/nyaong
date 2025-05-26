package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.service.AuthService;

public class LoginView {
    private final Stage stage;
    private final AuthService authService;

    private TextField userIdField;
    private PasswordField passwordField;
    private Label messageLabel;

    public LoginView(Stage stage) {
        this.stage = stage;
        this.authService = new AuthService();
    }

    public void show() {
        stage.setTitle("Nyaong Chat - 로그인");

        // 그리드 레이아웃 설정
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // 제목
        Label titleLabel = new Label("Nyaong Chat 로그인");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // 사용자 ID 입력
        Label userIdLabel = new Label("아이디:");
        grid.add(userIdLabel, 0, 1);

        userIdField = new TextField();
        userIdField.setPromptText("사용자 아이디 입력");
        grid.add(userIdField, 1, 1);

        // 비밀번호 입력
        Label passwordLabel = new Label("비밀번호:");
        grid.add(passwordLabel, 0, 2);

        passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호 입력");
        grid.add(passwordField, 1, 2);

        // 메시지 라벨
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");
        grid.add(messageLabel, 0, 3, 2, 1);

        // 로그인 버튼
        Button loginButton = new Button("로그인");
        loginButton.setOnAction(e -> handleLogin());

        // 회원가입 버튼
        Button registerButton = new Button("회원가입");
        registerButton.setOnAction(e -> showRegisterView());

        // 버튼을 담을 HBox
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(registerButton, loginButton);
        grid.add(buttonBox, 1, 4);

        // 장면 생성 및 표시
        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    private void handleLogin() {
        String userId = userIdField.getText();
        String password = passwordField.getText();

        if (userId.isEmpty() || password.isEmpty()) {
            messageLabel.setText("아이디와 비밀번호를 모두 입력해주세요.");
            return;
        }

        boolean success = authService.login(userId, password);

        if (success) {
            // 로그인 성공 시 메인 화면으로 이동
            if (authService.getCurrentUser().getRole().equals("USER")) {
                showMainView();
            } else {
                new VerifyView(stage).show();
            }
        } else {
            messageLabel.setText("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    private void showRegisterView() {
        // 회원가입 화면 표시
        RegisterView registerView = new RegisterView(stage);
        registerView.show();
    }

    private void showMainView() {
        // 메인 화면으로 전환
        MainView mainView = new MainView(stage);
        mainView.show();
    }
}