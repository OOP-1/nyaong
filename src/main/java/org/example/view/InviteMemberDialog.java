// src/main/java/org/example/view/InviteMemberDialog.java
package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
import java.util.stream.Collectors;

/**
 * 채팅방에 멤버 초대를 위한 대화상자
 */
public class InviteMemberDialog extends Dialog<Boolean> {
    private final FriendService friendService;
    private final ChatService chatService;
    private final Member currentUser;
    private final int chatRoomId;
    private final ListView<Friend> friendsListView;

    public InviteMemberDialog(Stage owner, int chatRoomId) {
        this.friendService = new FriendService();
        this.chatService = new ChatService();
        this.currentUser = AuthService.getCurrentUser();
        this.chatRoomId = chatRoomId;

        // 대화상자 설정
        setTitle("멤버 초대");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setHeaderText("채팅방에 초대할 친구를 선택하세요");

        // 친구 목록 (다중 선택 가능)
        friendsListView = new ListView<>();
        friendsListView.setCellFactory(param -> new FriendListCell());
        friendsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        friendsListView.setPrefHeight(300);

        // 초대 버튼
        Button inviteButton = new Button("초대하기");
        inviteButton.setOnAction(e -> inviteMembers());

        // 취소 버튼
        Button cancelButton = new Button("취소");
        cancelButton.setOnAction(e -> {
            setResult(false);
            close();
        });

        // 버튼 레이아웃
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, inviteButton);

        // 메인 레이아웃
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.getChildren().addAll(
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

        // 초대 가능한 친구 목록 로드
        loadInvitableFriends();
    }

    /**
     * 초대 가능한 친구 목록 로드 (채팅방에 아직 참여하지 않은 친구만 표시)
     */
    private void loadInvitableFriends() {
        List<Friend> allFriends = friendService.getFriendsList(currentUser.getMemberId());
        List<Member> chatRoomMembers = chatService.getChatRoomMembers(chatRoomId);

        // 채팅방에 이미 참여 중인 멤버 ID 목록
        List<Integer> memberIds = chatRoomMembers.stream()
                .map(Member::getMemberId)
                .collect(Collectors.toList());

        // 아직 참여하지 않은 친구만 필터링
        List<Friend> invitableFriends = allFriends.stream()
                .filter(friend -> !memberIds.contains(friend.getFriendId()))
                .collect(Collectors.toList());

        friendsListView.getItems().clear();

        if (invitableFriends.isEmpty()) {
            // 초대할 수 있는 친구가 없는 경우
            Label emptyLabel = new Label("초대할 수 있는 친구가 없습니다.");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            friendsListView.setPlaceholder(emptyLabel);
        } else {
            friendsListView.getItems().addAll(invitableFriends);
        }
    }

    /**
     * 선택한 친구들을 채팅방에 초대
     */
    private void inviteMembers() {
        List<Friend> selectedFriends = friendsListView.getSelectionModel().getSelectedItems();
        if (selectedFriends.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "선택 확인", "초대할 친구를 선택해주세요.");
            return;
        }

        boolean allSuccess = true;
        StringBuilder resultMessage = new StringBuilder();

        for (Friend friend : selectedFriends) {
            ChatService.ChatResult result = chatService.addChatRoomMember(chatRoomId, friend.getFriendId());

            if (result.isSuccess()) {
                resultMessage.append(friend.getFriendInfo().getNickname()).append(" 초대 성공\n");
            } else {
                resultMessage.append(friend.getFriendInfo().getNickname()).append(" 초대 실패: ")
                        .append(result.getMessage()).append("\n");
                allSuccess = false;
            }
        }

        Alert.AlertType alertType = allSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
        showAlert(alertType, "멤버 초대 결과", resultMessage.toString());

        if (allSuccess) {
            setResult(true);
            close();
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