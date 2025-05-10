package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.service.AuthService;

public class RegisterView {
    private final Stage stage;
    private final AuthService authService;

    private TextField userIdField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField nicknameField;
    private Label messageLabel;

    public RegisterView(Stage stage) {
        this.stage = stage;
        this.authService = new AuthService();
    }

    public void show() {
        stage.setTitle("Nyaong Chat - 회원가입");

        // 그리드 레이아웃 설정
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // 제목
        Label titleLabel = new Label("Nyaong Chat 회원가입");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // 사용자 ID 입력
        Label userIdLabel = new Label("아이디:");
        grid.add(userIdLabel, 0, 1);

        userIdField = new TextField();
        userIdField.setPromptText("사용할 아이디 입력");
        grid.add(userIdField, 1, 1);

        // 비밀번호 입력
        Label passwordLabel = new Label("비밀번호:");
        grid.add(passwordLabel, 0, 2);

        passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호 입력");
        grid.add(passwordField, 1, 2);

        // 비밀번호 확인
        Label confirmPasswordLabel = new Label("비밀번호 확인:");
        grid.add(confirmPasswordLabel, 0, 3);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("비밀번호 다시 입력");
        grid.add(confirmPasswordField, 1, 3);

        // 닉네임 입력
        Label nicknameLabel = new Label("닉네임:");
        grid.add(nicknameLabel, 0, 4);

        nicknameField = new TextField();
        nicknameField.setPromptText("사용할 닉네임 입력");
        grid.add(nicknameField, 1, 4);

        // 메시지 라벨
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");
        grid.add(messageLabel, 0, 5, 2, 1);

        // 회원가입 버튼
        Button registerButton = new Button("회원가입");
        registerButton.setOnAction(e -> handleRegister());

        // 취소 버튼
        Button cancelButton = new Button("취소");
        cancelButton.setOnAction(e -> showLoginView());

        // 버튼을 담을 HBox
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, registerButton);
        grid.add(buttonBox, 1, 6);

        // 장면 생성 및 표시
        Scene scene = new Scene(grid, 400, 350);
        stage.setScene(scene);
        stage.show();
    }

    private void handleRegister() {
        String userId = userIdField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String nickname = nicknameField.getText();

        // 입력 검증
        if (userId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nickname.isEmpty()) {
            messageLabel.setText("모든 필드를 입력해주세요.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("비밀번호가 일치하지 않습니다.");
            return;
        }

        // 회원가입 처리
        boolean success = authService.register(userId, password, nickname);

        if (success) {
            // 회원가입 성공 시 로그인 화면으로 이동
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("회원가입 성공");
            alert.setHeaderText(null);
            alert.setContentText("회원가입이 완료되었습니다. 로그인해주세요.");
            alert.showAndWait();

            showLoginView();
        } else {
            messageLabel.setText("이미 존재하는 아이디입니다.");
        }
    }

    private void showLoginView() {
        // 로그인 화면으로 돌아가기
        LoginView loginView = new LoginView(stage);
        loginView.show();
    }
}