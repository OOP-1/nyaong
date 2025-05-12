package org.example.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.ChatRoom;
import org.example.model.Friend;
import org.example.model.FriendRequest;
import org.example.model.Member;
import org.example.service.AuthService;
import org.example.service.ChatService;
import org.example.service.FriendService;

import java.util.List;
import java.util.Optional;

public class MainView {
    private final Stage stage;
    private final Member currentUser;
    private FriendsView friendsView;
    private final FriendService friendService;
    private Tab friendsTab;
    private Tab chatTab;
    private TabPane sidebarTabs;
    private Circle notificationBadge;
    private Timeline checkFriendRequestsTimeline;
    private BorderPane mainBorderPane;
    private ChatRoomsView chatRoomsView;

    public MainView(Stage stage) {
        this.stage = stage;
        this.currentUser = AuthService.getCurrentUser(); // 현재 로그인한 사용자 정보
        this.friendService = new FriendService();
    }

    public void show() {
        stage.setTitle("Nyaong Chat - 메인");

        // 메인 레이아웃
        mainBorderPane = new BorderPane();
        mainBorderPane.setPadding(new Insets(10));

        // 상단 메뉴바
        MenuBar menuBar = createMenuBar();
        mainBorderPane.setTop(menuBar);

        // 좌측 사이드바 (친구 목록, 채팅방)
        sidebarTabs = new TabPane();
        sidebarTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 친구 탭 생성
        friendsTab = createFriendsTab();

        // 채팅 탭 생성
        chatTab = createChatRoomsTab();

        // 탭 추가
        sidebarTabs.getTabs().addAll(friendsTab, chatTab);
        mainBorderPane.setLeft(sidebarTabs);

        // 중앙 컨텐츠 영역 (환영 메시지)
        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(20));
        Label welcomeLabel = new Label("안녕하세요, " + currentUser.getNickname() + "님!");
        welcomeLabel.setStyle("-fx-font-size: 24px;");
        Label infoLabel = new Label("왼쪽 탭에서 친구 목록이나 채팅방을 선택해주세요.");
        centerContent.getChildren().addAll(welcomeLabel, infoLabel);
        mainBorderPane.setCenter(centerContent);

        // 장면 생성 및 표시
        Scene scene = new Scene(mainBorderPane, 900, 600);
        stage.setScene(scene);
        stage.setUserData(this); // MainView 인스턴스를 Stage의 userData에 저장
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
        Tab tab = new Tab();

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

        tab.setGraphic(tabHeader);

        // FriendsView 생성 및 설정
        friendsView = new FriendsView(stage);
        tab.setContent(friendsView);

        // 탭 선택 시 이벤트
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                // 탭 선택 시 알림 배지 숨김
                notificationBadge.setVisible(false);
            }
        });

        return tab;
    }

    private Tab createChatRoomsTab() {
        Tab tab = new Tab("채팅");

        // 채팅 관련 컴포넌트 생성
        BorderPane chatPane = new BorderPane();

        // 채팅방 목록 뷰 생성 및 저장
        chatRoomsView = new ChatRoomsView(stage);

        // 처음에는 채팅방 목록만 표시
        chatPane.setCenter(chatRoomsView);

        // 채팅방 선택 시 채팅 화면으로 전환 (선택 콜백 설정)
        chatRoomsView.setOnChatRoomSelectedCallback(chatRoom -> {
            // 기존 ChatView가 있다면 정리
            if (chatPane.getCenter() instanceof ChatView) {
                ((ChatView) chatPane.getCenter()).dispose();
            }

            // 새로운 ChatView 생성
            ChatView chatView = new ChatView();
            chatView.setChatRoom(chatRoom);

            // 뒤로가기 버튼 추가
            Button backButton = new Button("← 채팅방 목록");
            backButton.setOnAction(e -> {
                chatView.setChatRoom(null);
                chatView.dispose();
                chatRoomsView.loadChatRooms();
                chatPane.setCenter(chatRoomsView);
                chatPane.setTop(null);
            });

            HBox topBox = new HBox(backButton);
            topBox.setPadding(new Insets(5));

            chatPane.setCenter(chatView);
            chatPane.setTop(topBox);
        });

        tab.setContent(chatPane);

        return tab;
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

    /**
     * 친구와의 1:1 채팅 시작
     * FriendsView에서 호출됨
     */
    public void openChatWithFriend(Friend friend) {
        try {
            // 채팅 탭으로 전환
            sidebarTabs.getSelectionModel().select(chatTab);

            // 채팅 탭의 내용 가져오기
            BorderPane chatPane = (BorderPane) chatTab.getContent();

            // 채팅 서비스 생성
            ChatService chatService = new ChatService();

            // 1:1 채팅방 생성 또는 조회
            ChatService.ChatResult result = chatService.createPrivateChatRoom(
                    currentUser.getMemberId(), friend.getFriendId());

            if (result.isSuccess()) {
                // 채팅방 정보 조회
                Optional<ChatRoom> chatRoomOpt = chatService.getChatRoomById(result.getChatRoomId());

                if (chatRoomOpt.isPresent()) {
                    // 채팅 뷰 생성 및 설정
                    ChatView chatView = new ChatView();
                    chatView.setChatRoom(chatRoomOpt.get());

                    // 뒤로가기 버튼 추가
                    Button backButton = new Button("← 채팅방 목록");
                    backButton.setOnAction(e -> {
                        // 채팅방 목록으로 돌아가기
                        chatRoomsView.loadChatRooms(); // 목록 새로고침
                        chatPane.setCenter(chatRoomsView);
                        chatPane.setTop(null);
                    });

                    HBox topBox = new HBox(backButton);
                    topBox.setPadding(new Insets(5));

                    // 채팅 뷰 표시
                    chatPane.setCenter(chatView);
                    chatPane.setTop(topBox);

                    // 성공 메시지 표시
                    showAlert(Alert.AlertType.INFORMATION, "1:1 채팅",
                            friend.getFriendInfo().getNickname() + "님과의 1:1 채팅방이 열렸습니다.");
                } else {
                    // 채팅방 정보 조회 실패
                    showAlert(Alert.AlertType.ERROR, "1:1 채팅 실패",
                            "채팅방 정보를 가져오는데 실패했습니다.");
                }
            } else {
                // 채팅방 생성 실패
                showAlert(Alert.AlertType.ERROR, "1:1 채팅 실패", result.getMessage());
            }
        } catch (Exception e) {
            // 예외 발생 시 로그 출력
            System.err.println("1:1 채팅 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();

            // 오류 메시지 표시
            showAlert(Alert.AlertType.ERROR, "오류 발생",
                    "1:1 채팅을 열 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 알림창 표시
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}