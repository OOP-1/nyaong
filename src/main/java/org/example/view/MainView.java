package org.example.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Member;
import org.example.service.AuthService;

public class MainView {
    private final Stage stage;
    private final Member currentUser;

    public MainView(Stage stage) {
        this.stage = stage;
        this.currentUser = AuthService.getCurrentUser(); // 현재 로그인한 사용자 정보
    }

    public void show() {
        stage.setTitle("Nyaong Chat - 메인");

        // 메인 레이아웃
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // 상단 메뉴바
        MenuBar menuBar = createMenuBar();
        borderPane.setTop(menuBar);

        // 좌측 사이드바 (친구 목록)
        TabPane sidebarTabs = new TabPane();
        sidebarTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        sidebarTabs.getTabs().addAll(createFriendsTab(), createChatRoomsTab());
        borderPane.setLeft(sidebarTabs);

        // 중앙 컨텐츠 영역 (환영 메시지)
        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(20));
        Label welcomeLabel = new Label("안녕하세요, " + currentUser.getNickname() + "님!");
        welcomeLabel.setStyle("-fx-font-size: 24px;");
        Label infoLabel = new Label("왼쪽 탭에서 친구 목록이나 채팅방을 선택해주세요.");
        centerContent.getChildren().addAll(welcomeLabel, infoLabel);
        borderPane.setCenter(centerContent);

        // 장면 생성 및 표시
        Scene scene = new Scene(borderPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // 파일 메뉴
        Menu fileMenu = new Menu("파일");
        MenuItem logoutItem = new MenuItem("로그아웃");
        logoutItem.setOnAction(e -> handleLogout());
        MenuItem exitItem = new MenuItem("종료");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);

        // 설정 메뉴
        Menu settingsMenu = new Menu("설정");
        MenuItem profileItem = new MenuItem("프로필 설정");
        profileItem.setOnAction(e -> handleProfileSettings());
        settingsMenu.getItems().add(profileItem);

        // 도움말 메뉴
        Menu helpMenu = new Menu("도움말");
        MenuItem aboutItem = new MenuItem("정보");
        aboutItem.setOnAction(e -> handleAbout());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, settingsMenu, helpMenu);
        return menuBar;
    }

    private Tab createFriendsTab() {
        Tab friendsTab = new Tab("친구 목록");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // 친구 검색
        TextField searchField = new TextField();
        searchField.setPromptText("친구 검색...");

        // 친구 목록 (임시 데이터)
        ListView<String> friendsList = new ListView<>();
        friendsList.getItems().addAll("친구 1", "친구 2", "친구 3");

        // 친구 추가 버튼
        Button addFriendButton = new Button("친구 추가");
        addFriendButton.setMaxWidth(Double.MAX_VALUE);
        addFriendButton.setOnAction(e -> handleAddFriend());

        content.getChildren().addAll(searchField, friendsList, addFriendButton);
        friendsTab.setContent(content);
        return friendsTab;
    }

    private Tab createChatRoomsTab() {
        Tab chatRoomsTab = new Tab("채팅방");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // 채팅방 목록 (임시 데이터)
        ListView<String> chatRoomsList = new ListView<>();
        chatRoomsList.getItems().addAll("채팅방 1", "채팅방 2");

        // 채팅방 생성 버튼
        Button createChatRoomButton = new Button("채팅방 생성");
        createChatRoomButton.setMaxWidth(Double.MAX_VALUE);
        createChatRoomButton.setOnAction(e -> handleCreateChatRoom());

        content.getChildren().addAll(chatRoomsList, createChatRoomButton);
        chatRoomsTab.setContent(content);
        return chatRoomsTab;
    }

    private void handleLogout() {
        // 로그아웃 처리
        AuthService authService = new AuthService();
        authService.logout();

        // 로그인 화면으로 이동
        LoginView loginView = new LoginView(stage);
        loginView.show();
    }

    private void handleProfileSettings() {
        // 프로필 설정 다이얼로그 표시
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("프로필 설정");
        alert.setHeaderText(null);
        alert.setContentText("프로필 설정 기능은 아직 구현되지 않았습니다.");
        alert.showAndWait();
    }

    private void handleAbout() {
        // 앱 정보 다이얼로그 표시
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("앱 정보");
        alert.setHeaderText("Nyaong Chat");
        alert.setContentText("버전: 1.0.0\n제작자: Nyaong Team");
        alert.showAndWait();
    }

    private void handleAddFriend() {
        // 친구 추가 다이얼로그 표시
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("친구 추가");
        dialog.setHeaderText(null);
        dialog.setContentText("추가할 친구의 아이디를 입력하세요:");

        dialog.showAndWait().ifPresent(userId -> {
            // TODO: 친구 추가 기능 구현
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("친구 추가");
            alert.setHeaderText(null);
            alert.setContentText("친구 추가 기능은 아직 구현되지 않았습니다.");
            alert.showAndWait();
        });
    }

    private void handleCreateChatRoom() {
        // 채팅방 생성 다이얼로그 표시
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("채팅방 생성");
        dialog.setHeaderText(null);
        dialog.setContentText("생성할 채팅방 이름을 입력하세요:");

        dialog.showAndWait().ifPresent(roomName -> {
            // TODO: 채팅방 생성 기능 구현
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("채팅방 생성");
            alert.setHeaderText(null);
            alert.setContentText("채팅방 생성 기능은 아직 구현되지 않았습니다.");
            alert.showAndWait();
        });
    }
}