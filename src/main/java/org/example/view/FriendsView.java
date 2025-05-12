package org.example.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.Friend;
import org.example.model.Member;
import org.example.service.AuthService;
import org.example.service.FriendService;

import java.util.List;
import java.util.Optional;

/**
 * 친구 목록 및 관리 화면
 * MainView의 친구 탭에 표시됨
 */
public class FriendsView extends BorderPane {
    private final Stage stage;
    private final FriendService friendService;
    private final Member currentUser;
    private final ListView<Friend> friendsListView;
    private final FriendRequestsView friendRequestsView;
    private TextField searchField;
    private Timeline autoRefreshTimeline;

    // 새로고침 버튼 (모든 뷰에서 참조할 수 있도록 필드로 선언)
    private Button refreshButton;

    public FriendsView(Stage stage) {
        this.stage = stage;
        this.friendService = new FriendService();
        this.currentUser = AuthService.getCurrentUser();

        setPadding(new Insets(10));

        // 상단 영역 - 검색, 추가 버튼, 새로고침 버튼
        HBox topBox = createTopBox();
        setTop(topBox);

        // 중앙 영역 - 친구 목록
        friendsListView = new ListView<>();
        friendsListView.setCellFactory(param -> new FriendListCell());
        setCenter(friendsListView);

        // 오른쪽 영역 - 친구 요청 목록
        friendRequestsView = new FriendRequestsView();
        // 친구 목록 새로고침 콜백 설정 (더 이상 필요 없음)
        // friendRequestsView.setRefreshFriendsCallback(this::loadFriends);
        setRight(friendRequestsView);

        // 친구 목록 로드
        refreshAll();

        // 자동 새로고침 타이머 시작
        startAutoRefresh();
    }

    /**
     * 상단 영역 생성 (검색 필드, 친구 추가 버튼, 새로고침 버튼)
     */
    private HBox createTopBox() {
        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(0, 0, 10, 0));

        // 검색 필드
        searchField = new TextField();
        searchField.setPromptText("친구 검색...");
        searchField.prefWidthProperty().bind(topBox.widthProperty().multiply(0.5));

        // 친구 검색 기능
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFriendsList(newValue);
        });

        // 친구 추가 버튼
        Button addFriendButton = new Button("친구 요청");
        addFriendButton.setOnAction(e -> showAddFriendDialog());

        // 통합 새로고침 버튼
        refreshButton = new Button("새로고침");
        refreshButton.setOnAction(e -> {
            refreshAll();
            showRefreshToast();
        });

        topBox.getChildren().addAll(searchField, addFriendButton, refreshButton);
        return topBox;
    }

    /**
     * 모든 목록 새로고침 (친구 목록 + 친구 요청 목록)
     */
    public void refreshAll() {
        loadFriends();
        friendRequestsView.loadFriendRequests();
    }

    /**
     * 친구 목록 로드
     */
    public void loadFriends() {
        // 검색어가 있으면 저장
        String searchText = searchField.getText().trim();

        // 친구 목록 전체 로드
        List<Friend> friends = friendService.getFriendsList(currentUser.getMemberId());

        // UI 스레드에서 처리
        Platform.runLater(() -> {
            friendsListView.getItems().clear();

            if (searchText.isEmpty()) {
                // 검색어가 없으면 전체 목록 표시
                friendsListView.getItems().addAll(friends);
            } else {
                // 검색어가 있으면 필터링해서 표시
                friends.stream()
                        .filter(friend ->
                                friend.getFriendInfo().getNickname().toLowerCase().contains(searchText.toLowerCase()) ||
                                        friend.getFriendInfo().getUserId().toLowerCase().contains(searchText.toLowerCase())
                        )
                        .forEach(friend -> friendsListView.getItems().add(friend));
            }
        });
    }

    /**
     * 검색어로 친구 목록 필터링
     */
    private void filterFriendsList(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadFriends(); // 검색어가 없으면 전체 목록 표시
            return;
        }

        List<Friend> allFriends = friendService.getFriendsList(currentUser.getMemberId());

        Platform.runLater(() -> {
            friendsListView.getItems().clear();

            // 검색어와 일치하는 친구만 필터링하여 표시
            allFriends.stream()
                    .filter(friend ->
                            friend.getFriendInfo().getNickname().toLowerCase().contains(searchText.toLowerCase()) ||
                                    friend.getFriendInfo().getUserId().toLowerCase().contains(searchText.toLowerCase())
                    )
                    .forEach(friend -> friendsListView.getItems().add(friend));
        });
    }

    /**
     * 친구 요청 대화상자 표시
     */
    private void showAddFriendDialog() {
        AddFriendDialog dialog = new AddFriendDialog(stage);
        dialog.showAndWait();

        // 대화상자 닫힌 후 모든 목록 새로고침
        refreshAll();
    }

    /**
     * 자동 새로고침 시작
     */
    private void startAutoRefresh() {
        // 기존 타이머가 있으면 중지
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }

        // 20초마다 자동 새로고침
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(20), event -> {
                    refreshAll(); // 모든 목록 새로고침
                })
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * 새로고침 버튼 클릭 시 토스트 메시지 표시
     */
    private void showRefreshToast() {
        // 간단한 알림창 표시
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("새로고침");
        alert.setHeaderText(null);
        alert.setContentText("친구 정보가 새로고침되었습니다.");

        // 1.5초 후 자동으로 닫기
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1.5), event -> alert.close())
        );
        timeline.play();

        alert.show();
    }

    /**
     * 자원 해제
     */
    public void dispose() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }

    /**
     * 새로고침 버튼 강조 표시 (새로운 친구 요청이 있을 때)
     */
    public void highlightRefreshButton(boolean highlight) {
        if (refreshButton != null) {
            if (highlight) {
                refreshButton.setStyle("-fx-base: #ffb6c1;"); // 연한 분홍색으로 강조
            } else {
                refreshButton.setStyle(""); // 기본 스타일로 되돌림
            }
        }
    }

    /**
     * 친구 항목 컨텍스트 메뉴 생성
     */
    private ContextMenu createFriendContextMenu(Friend friend) {
        ContextMenu contextMenu = new ContextMenu();

        // 1:1 채팅 메뉴 항목
        MenuItem chatMenuItem = new MenuItem("1:1 채팅");
        chatMenuItem.setOnAction(e -> startPrivateChat(friend));

        // 친구 삭제 메뉴 항목
        MenuItem deleteFriendMenuItem = new MenuItem("친구 삭제");
        deleteFriendMenuItem.setOnAction(e -> removeFriend(friend));

        contextMenu.getItems().addAll(chatMenuItem, deleteFriendMenuItem);
        return contextMenu;
    }

    /**
     * 1:1 채팅 시작
     */
    private void startPrivateChat(Friend friend) {
        // MainView 인스턴스 찾기
        if (getScene() != null && getScene().getWindow() instanceof Stage) {
            Stage mainStage = (Stage) getScene().getWindow();
            if (mainStage.getUserData() instanceof MainView) {
                MainView mainView = (MainView) mainStage.getUserData();
                mainView.openChatWithFriend(friend);
                return;
            }
        }

        // MainView 참조를 찾지 못한 경우
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("기능 준비 중");
        alert.setHeaderText(null);
        alert.setContentText(friend.getFriendInfo().getNickname() + "님과의 1:1 채팅은 아직 구현되지 않았습니다.");
        alert.showAndWait();
    }

    /**
     * 친구 삭제
     */
    private void removeFriend(Friend friend) {
        // 삭제 확인 대화상자
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("친구 삭제");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(friend.getFriendInfo().getNickname() + "님을 친구 목록에서 삭제하시겠습니까?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 친구 삭제 실행
            FriendService.FriendResult deleteResult = friendService.removeFriend(
                    currentUser.getMemberId(), friend.getFriendId());

            if (deleteResult.isSuccess()) {
                // 모든 목록 새로고침
                Platform.runLater(this::refreshAll);

                // 성공 메시지 표시
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("친구 삭제");
                successAlert.setHeaderText(null);
                successAlert.setContentText(deleteResult.getMessage());
                successAlert.showAndWait();
            } else {
                // 실패 메시지 표시
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("친구 삭제 실패");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText(deleteResult.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    /**
     * 친구 정보를 표시하는 커스텀 리스트 셀
     */
    private class FriendListCell extends ListCell<Friend> {
        @Override
        protected void updateItem(Friend friend, boolean empty) {
            super.updateItem(friend, empty);

            if (empty || friend == null || friend.getFriendInfo() == null) {
                setText(null);
                setGraphic(null);
                setContextMenu(null);
            } else {
                Member friendInfo = friend.getFriendInfo();

                // 친구 정보 표시 레이아웃
                VBox vbox = new VBox(5);
                vbox.setPadding(new Insets(5));

                // 닉네임 및 상태 표시
                Label nicknameLabel = new Label(friendInfo.getNickname());
                nicknameLabel.setStyle("-fx-font-weight: bold;");

//                Label userIdLabel = new Label("ID: " + friendInfo.getUserId());
//                userIdLabel.setStyle("-fx-font-size: 11px;");

                Label statusLabel = new Label(friendInfo.getStatus());
                statusLabel.setStyle("-fx-text-fill: " +
                        (friendInfo.getStatus().equals("ONLINE") ? "green" : "gray") + ";");

                // vbox.getChildren().addAll(nicknameLabel, userIdLabel, statusLabel); // 이 줄 수정
                vbox.getChildren().addAll(nicknameLabel, statusLabel); // ID 라벨 제거

                // 컨텍스트 메뉴 설정
                setContextMenu(createFriendContextMenu(friend));

                setText(null);
                setGraphic(vbox);
            }
        }
    }
}