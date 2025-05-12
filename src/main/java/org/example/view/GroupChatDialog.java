// src/main/java/org/example/view/GroupChatDialog.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 그룹 채팅 생성을 위한 대화상자
 */
public class GroupChatDialog extends Dialog<Boolean> {
    private final FriendService friendService;
    private final ChatService chatService;
    private final Member currentUser;
    private final ListView<Friend> friendsListView;
    private final TextField chatNameField;

    public GroupChatDialog(Stage owner) {
        this.friendService = new FriendService();
        this.chatService = new ChatService();
        this.currentUser = AuthService.getCurrentUser();

        // 대화상자 설정
        setTitle("그룹 채팅 생성");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setHeaderText("그룹 채팅에 초대할 친구를 선택하세요");

        // 채팅방 이름 입력 필드
        Label nameLabel = new Label("채팅방 이름:");
        chatNameField = new TextField();
        chatNameField.setPromptText("채팅방 이름 입력");

        // 친구 목록 (다중 선택 가능)
        friendsListView = new ListView<>();
        friendsListView.setCellFactory(param -> new FriendListCell());
        friendsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        friendsListView.setPrefHeight(300);

        // 채팅 생성 버튼
        Button createButton = new Button("채팅방 생성");
        createButton.setOnAction(e -> createGroupChat());

        // 취소 버튼
        Button cancelButton = new Button("취소");
        cancelButton.setOnAction(e -> {
            setResult(false);
            close();
        });

        // 버튼 레이아웃
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, createButton);

        // 메인 레이아웃
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.getChildren().addAll(
                nameLabel,
                chatNameField,
                new Label("초대할 친구 선택:"),
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
     * 그룹 채팅 생성
     */
    private void createGroupChat() {
        String chatName = chatNameField.getText().trim();
        if (chatName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "입력 확인", "채팅방 이름을 입력해주세요.");
            return;
        }

        List<Friend> selectedFriends = friendsListView.getSelectionModel().getSelectedItems();
        if (selectedFriends.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "선택 확인", "최소 한 명 이상의 친구를 선택해주세요.");
            return;
        }

        // 선택한 친구들의 ID 목록 생성
        List<Integer> memberIds = selectedFriends.stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());

        // 그룹 채팅방 생성
        ChatService.ChatResult result = chatService.createGroupChatRoom(
                currentUser.getMemberId(), chatName, memberIds);

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "채팅방 생성", result.getMessage());
            setResult(true);
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