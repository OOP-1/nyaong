package org.example.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.FriendRequest;
import org.example.model.Member;
import org.example.service.AuthService;
import org.example.service.FriendService;

import java.util.List;

public class MainView {
    private final Stage stage;
    private final Member currentUser;
    private FriendsView friendsView;
    private final FriendService friendService;
    private Tab friendsTab;
    private Circle notificationBadge;
    private Timeline checkFriendRequestsTimeline;

    public MainView(Stage stage) {
        this.stage = stage;
        this.currentUser = AuthService.getCurrentUser(); // 현재 로그인한 사용자 정보
        this.friendService = new FriendService();
    }

    public void show() {
        stage.setTitle("Nyaong Chat - 메인");

        // 메인 레이아웃
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        // 상단 메뉴바
        MenuBar menuBar = createMenuBar();
        borderPane.setTop(menuBar);

        // 좌측 사이드바 (친구 목록, 채팅방)
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
        Scene scene = new Scene(borderPane, 900, 600);
        stage.setScene(scene);
        stage.show();

        // 윈도우 닫기 이벤트 처리 (로그아웃과 자원 해제)
        stage.setOnCloseRequest(e -> {
            cleanup();
            handleLogout();
        });

        // 친구 요청 알림 확인 타이머 시작
        startFriendRequestChecker();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // 파일 메뉴
        Menu fileMenu = new Menu("파일");
        MenuItem logoutItem = new MenuItem("로그아웃");
        logoutItem.setOnAction(e -> handleLogout());
        MenuItem exitItem = new MenuItem("종료");
        exitItem.setOnAction(e -> {
            cleanup();
            Platform.exit();
        });
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
        friendsTab = new Tab();

        // 탭 제목 영역에 알림 배지를 포함시킴
        StackPane tabHeader = new StackPane();
        Label titleLabel = new Label("친구 목록");

        // 알림 배지 (새로운 친구 요청이 있을 때 표시)
        notificationBadge = new Circle(6, Color.RED);
        notificationBadge.setTranslateX(30);
        notificationBadge.setTranslateY(-5);
        notificationBadge.setVisible(false); // 초기에는 숨김

        tabHeader.getChildren().addAll(titleLabel, notificationBadge);
        tabHeader.setAlignment(Pos.CENTER_LEFT);

        friendsTab.setGraphic(tabHeader);

        // FriendsView 생성 및 설정
        friendsView = new FriendsView(stage);
        friendsTab.setContent(friendsView);

        // 탭 선택 시 이벤트
        friendsTab.setOnSelectionChanged(event -> {
            if (friendsTab.isSelected()) {
                // 탭 선택 시 알림 배지 숨김
                notificationBadge.setVisible(false);
            }
        });

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

    /**
     * 자원 해제
     */
    private void cleanup() {
        if (checkFriendRequestsTimeline != null) {
            checkFriendRequestsTimeline.stop();
        }

        if (friendsView != null) {
            friendsView.dispose();
        }
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

    // FriendsView 새로고침
    public void refreshFriendsView() {
        if (friendsView != null) {
            friendsView.loadFriends();
        }
    }

    /**
     * 친구 요청 알림 확인 타이머 시작
     */
    private void startFriendRequestChecker() {
        // 10초마다 친구 요청 확인
        checkFriendRequestsTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), event -> checkFriendRequests())
        );
        checkFriendRequestsTimeline.setCycleCount(Timeline.INDEFINITE);
        checkFriendRequestsTimeline.play();

        // 초기 확인
        checkFriendRequests();
    }

    /**
     * 친구 요청 확인
     */
    private void checkFriendRequests() {
        Platform.runLater(() -> {
            List<FriendRequest> requests = friendService.getPendingFriendRequests(currentUser.getMemberId());

            // 새로운 친구 요청이 있을 경우 알림 표시
            if (!requests.isEmpty()) {
                // 알림 배지 표시 (친구 탭이 선택되어 있지 않을 때만)
                if (!friendsTab.isSelected()) {
                    notificationBadge.setVisible(true);
                }

                // 첫 실행이 아니고 새로운 요청이 있는 경우에만 토스트 메시지 표시
                if (notificationBadge.isVisible()) {
                    showFriendRequestToast(requests.size());
                }
            }
        });
    }

    /**
     * 친구 요청 알림 토스트 표시
     */
    private void showFriendRequestToast(int count) {
        // 토스트 메시지 표시
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("새 친구 요청");
        alert.setHeaderText(null);
        alert.setContentText(count + "개의 새로운 친구 요청이 있습니다.");

        // 3초 후 자동으로 닫기
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> alert.close())
        );
        timeline.play();

        alert.show();
    }
}