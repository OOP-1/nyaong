// src/main/java/org/example/view/PrivateChatDialog.java
package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Friend;
import org.example.model.Member;
import org.example.service.AuthService;
import org.example.service.ChatService;
import org.example.service.FriendService;

import java.util.List;
import java.util.Optional;

/**
 * 1:1 채팅 생성을 위한 대화상자
 */

public class PrivateChatDialog extends Dialog<Integer> {
    private final FriendService friendService;
    private final ChatService chatService;
    private final Member currentUser;
    private final ListView<Friend> friendsListView;

    public PrivateChatDialog(Stage owner) {
        this.friendService = new FriendService();
        this.chatService = new ChatService();
        this.currentUser = AuthService.getCurrentUser();

        // 대화상자 설정
        setTitle("1:1 채팅");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setHeaderText("채팅할 친구를 선택하세요");

        // 친구 목록
        friendsListView = new ListView<>();
        friendsListView.setCellFactory(param -> new FriendListCell());
        friendsListView.setPrefHeight(300);

        // 채팅 시작 버튼
        Button startChatButton = new Button("채팅 시작");
        startChatButton.setOnAction(e -> startChat());
        startChatButton.setDisable(true);

        // 친구 선택 시 버튼 활성화
        friendsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> startChatButton.setDisable(newValue == null));

        // 취소 버튼
        Button cancelButton = new Button("취소");
        cancelButton.setOnAction(e -> {
            setResult(-1);
            close();
        });

        // 버튼 레이아웃
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, startChatButton);

        // 메인 레이아웃
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.getChildren().addAll(
                new Label("친구 목록:"),
                friendsListView,
                buttonBox
        );

        getDialogPane().setContent(contentBox);

        // 확인 및 취소 버튼 추가
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // OK, Cancel 버튼 숨기기 (커스텀 버튼 사용)
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        okButton.setVisible(false);
        cancelBtn.setVisible(false);

        // 친구 목록 로드
        loadFriends();
    }

    /**
     * 친구 목록 로드
     */
    private void loadFriends() {
        List<Friend> friends = friendService.getFriendsList(currentUser.getMemberId());
        friendsListView.getItems().clear();
        friendsListView.getItems().addAll(friends);
    }

    /**
     * 채팅 시작
     */
    private void startChat() {
        Friend selectedFriend = friendsListView.getSelectionModel().getSelectedItem();
        if (selectedFriend == null) {
            return;
        }

        // 1:1 채팅방 생성 또는 조회
        ChatService.ChatResult result = chatService.createPrivateChatRoom(
                currentUser.getMemberId(), selectedFriend.getFriendId());

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "채팅방 생성", result.getMessage());
            setResult(result.getChatRoomId());
            close();
        } else {
            showAlert(Alert.AlertType.ERROR, "채팅방 생성 실패", result.getMessage());
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
     * 친구 정보를 표시하는 커스텀 리스트 셀
     */
    private static class FriendListCell extends ListCell<Friend> {
        @Override
        protected void updateItem(Friend friend, boolean empty) {
            super.updateItem(friend, empty);

            if (empty || friend == null || friend.getFriendInfo() == null) {
                setText(null);
                setGraphic(null);
            } else {
                Member friendInfo = friend.getFriendInfo();

                // 친구 정보 표시
                Label nameLabel = new Label(friendInfo.getNickname());
                nameLabel.setStyle("-fx-font-weight: bold;");

                Label statusLabel = new Label(friendInfo.getStatus());
                statusLabel.setStyle("-fx-text-fill: " +
                        (friendInfo.getStatus().equals("ONLINE") ? "green" : "gray") + ";");

                VBox vbox = new VBox(5);
                vbox.getChildren().addAll(nameLabel, statusLabel);

                setText(null);
                setGraphic(vbox);
            }
        }
    }
}