package org.example.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Member;
import org.example.service.AuthService;
import org.example.service.FriendService;

import java.util.List;

/**
 * 친구 요청을 위한 대화상자
 */
public class AddFriendDialog extends Dialog<String> {
    private final FriendService friendService;
    private final TextField searchField;
    private final ListView<Member> searchResultsListView;
    private final TextArea messageArea; // 소개 메시지 입력 필드

    public AddFriendDialog(Stage owner) {
        this.friendService = new FriendService();

        // 대화상자 설정
        setTitle("친구 요청");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);

        // 헤더 텍스트 설정
        setHeaderText("친구 요청을 보낼 사용자의 ID를 검색하세요");

        // 검색 필드
        searchField = new TextField();
        searchField.setPromptText("사용자 ID 입력");

        // 검색 버튼
        Button searchButton = new Button("검색");
        searchButton.setOnAction(e -> performSearch());

        // 검색 레이아웃
        HBox searchBox = new HBox(10);
        searchBox.getChildren().addAll(searchField, searchButton);

        // 검색 결과 목록
        searchResultsListView = new ListView<>();
        searchResultsListView.setPrefHeight(200);
        searchResultsListView.setCellFactory(param -> new MemberListCell());

        // 소개 메시지 입력 영역
        Label messageLabel = new Label("나를 소개하는 메시지:");
        messageArea = new TextArea();
        messageArea.setPromptText("상대방에게 보낼 메시지를 입력하세요...");
        messageArea.setPrefRowCount(3);

        // 요청 보내기 버튼
        Button requestButton = new Button("친구 요청 보내기");
        requestButton.setOnAction(e -> sendFriendRequest());
        requestButton.setDisable(true);

        // 검색 결과 목록에서 항목 선택 시 요청 버튼 활성화
        searchResultsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> requestButton.setDisable(newValue == null));

        // 취소 버튼
        Button cancelButton = new Button("취소");
        cancelButton.setOnAction(e -> {
            // 대화상자 닫기
            this.setResult("");
            this.close();
        });

        // 버튼 레이아웃
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(cancelButton, requestButton);
        buttonBox.setStyle("-fx-alignment: center-right;");

        // 메인 레이아웃
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.getChildren().addAll(
                searchBox,
                new Label("검색 결과:"),
                searchResultsListView,
                messageLabel,
                messageArea,
                buttonBox
        );

        // 대화상자 컨텐츠 설정
        getDialogPane().setContent(contentBox);

        // 대화상자 종료 버튼(X) 동작 설정
        setOnCloseRequest(event -> {
            // 대화상자 닫기
            this.setResult("");
            this.close();
        });

        // 확인 및 취소 버튼 추가
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // OK, Cancel 버튼 숨기기 (커스텀 버튼 사용)
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        okButton.setVisible(false);
        cancelBtn.setVisible(false);
    }

    /**
     * 사용자 ID로 검색 실행
     */
    private void performSearch() {
        String userId = searchField.getText().trim();

        if (userId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "검색어를 입력하세요", "사용자 ID를 입력해주세요.");
            return;
        }

        Member currentUser = AuthService.getCurrentUser();
        List<Member> searchResults = friendService.searchUsersByUserId(currentUser.getMemberId(), userId);

        searchResultsListView.getItems().clear();
        searchResultsListView.getItems().addAll(searchResults);

        if (searchResults.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "검색 결과", "검색 결과가 없습니다.");
        }
    }

    /**
     * 선택한 사용자에게 친구 요청 보내기 (소개 메시지 포함)
     */
    private void sendFriendRequest() {
        Member selectedMember = searchResultsListView.getSelectionModel().getSelectedItem();

        if (selectedMember == null) {
            return;
        }

        // 소개 메시지 가져오기
        String message = messageArea.getText().trim();

        Member currentUser = AuthService.getCurrentUser();
        FriendService.FriendResult result = friendService.requestFriendByUserId(
                currentUser.getMemberId(), selectedMember.getUserId(), message);

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "친구 요청 전송", result.getMessage());
            this.setResult(selectedMember.getUserId());
            this.close();
        } else {
            showAlert(Alert.AlertType.ERROR, "친구 요청 실패", result.getMessage());
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
     * 사용자 정보를 표시하는 커스텀 리스트 셀 (ID와 함께 표시)
     */
    private static class MemberListCell extends ListCell<Member> {
        private final VBox container;
        private final Label nicknameLabel;
        private final Label userIdLabel;
        private final Label statusLabel;

        public MemberListCell() {
            // 재사용가능한 컴포넌트 생성
            container = new VBox(5);
            container.setPadding(new Insets(5));

            nicknameLabel = new Label();
            nicknameLabel.setStyle("-fx-font-weight: bold;");

            userIdLabel = new Label();
            userIdLabel.setStyle("-fx-font-size: 11px;");

            statusLabel = new Label();

            container.getChildren().addAll(nicknameLabel, userIdLabel, statusLabel);
        }

        @Override
        protected void updateItem(Member member, boolean empty) {
            super.updateItem(member, empty);

            if (empty || member == null) {
                setText(null);
                setGraphic(null);
            } else {
                // 친구 추가 화면에서는 ID와 닉네임 모두 표시
                nicknameLabel.setText(member.getNickname());
//                userIdLabel.setText("ID: " + member.getUserId());
                statusLabel.setText("상태: " + member.getStatus());
                statusLabel.setStyle("-fx-text-fill: " +
                        (member.getStatus().equals("ONLINE") ? "green" : "gray") + ";");

                setText(null);
                setGraphic(container);
            }
        }
    }
}