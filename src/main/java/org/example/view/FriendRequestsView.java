package org.example.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.model.FriendRequest;
import org.example.model.Member;
import org.example.service.AuthService;
import org.example.service.FriendService;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 친구 요청 목록 및 처리 화면
 */
public class FriendRequestsView extends VBox {
    private final FriendService friendService;
    private final Member currentUser;
    private final ListView<FriendRequest> requestsListView;
    private final Label noRequestsLabel;

    public FriendRequestsView() {
        this.friendService = new FriendService();
        this.currentUser = AuthService.getCurrentUser();

        setPadding(new Insets(10));
        setSpacing(10);
        setMinWidth(300); // 최소 너비 설정

        // 제목
        Label titleLabel = new Label("친구 요청");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 요청이 없을 때 표시할 라벨
        noRequestsLabel = new Label("받은 친구 요청이 없습니다.");
        noRequestsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        noRequestsLabel.setAlignment(Pos.CENTER);
        noRequestsLabel.setMaxWidth(Double.MAX_VALUE);
        noRequestsLabel.setPadding(new Insets(20, 0, 0, 0));

        // 친구 요청 목록
        requestsListView = new ListView<>();
        requestsListView.setCellFactory(param -> new FriendRequestCell(this::handleAccept, this::handleReject));
        VBox.setVgrow(requestsListView, Priority.ALWAYS);

        // 새로고침 버튼 제거 (FriendsView의 통합 버튼 사용)

        getChildren().addAll(titleLabel, requestsListView);

        // 초기 데이터 로드
        loadFriendRequests();
    }

    /**
     * 친구 요청 목록 로드
     */
    public void loadFriendRequests() {
        List<FriendRequest> requests = friendService.getPendingFriendRequests(currentUser.getMemberId());

        Platform.runLater(() -> {
            requestsListView.getItems().clear();

            if (requests.isEmpty()) {
                getChildren().remove(requestsListView);
                if (!getChildren().contains(noRequestsLabel)) {
                    getChildren().add(1, noRequestsLabel);
                }
            } else {
                getChildren().remove(noRequestsLabel);
                if (!getChildren().contains(requestsListView)) {
                    getChildren().add(1, requestsListView);
                }
                requestsListView.getItems().addAll(requests);
            }
        });
    }

    /**
     * 친구 요청 수락 처리
     * @param request 처리할 친구 요청
     */
    private void handleAccept(FriendRequest request) {
        FriendService.FriendResult result = friendService.acceptFriendRequest(request.getRequestId());

        if (result.isSuccess()) {
            // 요청 목록에서 제거
            Platform.runLater(() -> {
                requestsListView.getItems().remove(request);
                if (requestsListView.getItems().isEmpty()) {
                    getChildren().remove(requestsListView);
                    if (!getChildren().contains(noRequestsLabel)) {
                        getChildren().add(1, noRequestsLabel);
                    }
                }

                // 성공 메시지 표시
                showAlert(Alert.AlertType.INFORMATION, "친구 요청 수락",
                        request.getSenderInfo().getNickname() + "님의 친구 요청을 수락했습니다.");

                // 부모 뷰에 알려 친구 목록 새로고침 (FriendsView에서 처리)
                // 통합 새로고침 버튼을 사용하므로 별도 처리 필요 없음
            });
        } else {
            // 실패 메시지 표시
            showAlert(Alert.AlertType.ERROR, "친구 요청 수락 실패", result.getMessage());
        }
    }

    /**
     * 친구 요청 거절 처리
     * @param request 처리할 친구 요청
     */
    private void handleReject(FriendRequest request) {
        FriendService.FriendResult result = friendService.rejectFriendRequest(request.getRequestId());

        if (result.isSuccess()) {
            // 요청 목록에서 제거
            Platform.runLater(() -> {
                requestsListView.getItems().remove(request);
                if (requestsListView.getItems().isEmpty()) {
                    getChildren().remove(requestsListView);
                    if (!getChildren().contains(noRequestsLabel)) {
                        getChildren().add(1, noRequestsLabel);
                    }
                }

                // 성공 메시지 표시
                showAlert(Alert.AlertType.INFORMATION, "친구 요청 거절",
                        request.getSenderInfo().getNickname() + "님의 친구 요청을 거절했습니다.");
            });
        } else {
            // 실패 메시지 표시
            showAlert(Alert.AlertType.ERROR, "친구 요청 거절 실패", result.getMessage());
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

    /**
     * 친구 요청 항목을 표시하는 커스텀 리스트 셀
     */
    private static class FriendRequestCell extends ListCell<FriendRequest> {
        private final RequestHandler acceptHandler;
        private final RequestHandler rejectHandler;

        public FriendRequestCell(RequestHandler acceptHandler, RequestHandler rejectHandler) {
            this.acceptHandler = acceptHandler;
            this.rejectHandler = rejectHandler;
        }

        @Override
        protected void updateItem(FriendRequest request, boolean empty) {
            super.updateItem(request, empty);

            if (empty || request == null || request.getSenderInfo() == null) {
                setText(null);
                setGraphic(null);
            } else {
                // 컨테이너 생성
                VBox container = new VBox(5);
                container.setPadding(new Insets(8));

                // 사용자 정보 - 닉네임만 표시 (ID 제거)
                Member sender = request.getSenderInfo();
                // Label nameLabel = new Label(sender.getNickname() + " (" + sender.getUserId() + ")"); // 이 줄 수정
                Label nameLabel = new Label(sender.getNickname()); // ID 제거
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                // 요청 시간
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String requestTime = dateFormat.format(request.getCreatedAt());
                Label timeLabel = new Label("요청 시간: " + requestTime);
                timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                // 소개 메시지 표시
                String message = request.getMessage();
                VBox messageBox = new VBox(3);
                messageBox.setPadding(new Insets(5));
                messageBox.setStyle("-fx-background-color: #f2f2f2; -fx-background-radius: 5;");

                if (message != null && !message.trim().isEmpty()) {
                    Label messageLabel = new Label(message);
                    messageLabel.setWrapText(true);
                    messageBox.getChildren().add(messageLabel);
                } else {
                    Label noMessageLabel = new Label("소개 메시지가 없습니다.");
                    noMessageLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
                    messageBox.getChildren().add(noMessageLabel);
                }

                // 버튼 컨테이너
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setPadding(new Insets(5, 0, 0, 0));

                // 수락 버튼
                Button acceptButton = new Button("수락");
                acceptButton.setStyle("-fx-base: lightgreen;");
                acceptButton.setOnAction(e -> acceptHandler.handle(request));

                // 거절 버튼
                Button rejectButton = new Button("거절");
                rejectButton.setStyle("-fx-base: lightpink;");
                rejectButton.setOnAction(e -> rejectHandler.handle(request));

                buttonBox.getChildren().addAll(rejectButton, acceptButton);

                container.getChildren().addAll(nameLabel, timeLabel, messageBox, buttonBox);

                setText(null);
                setGraphic(container);
            }
        }
    }

    /**
     * 친구 요청 처리를 위한 함수형 인터페이스
     */
    @FunctionalInterface
    private interface RequestHandler {
        void handle(FriendRequest request);
    }
}