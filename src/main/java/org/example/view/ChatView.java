// src/main/java/org/example/view/ChatView.java
package org.example.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node; // Node 임포트 추가
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.ChatRoom;
import org.example.model.Member;
import org.example.model.Message;
import org.example.repository.MemberRepository;
import org.example.service.AuthService;
import org.example.service.BlockchainMessageService;
import org.example.service.ChatService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * 채팅 화면
 * 채팅방을 선택하면 메인 화면에 표시됨
 */
public class ChatView extends BorderPane {
    private final ChatService chatService;
    private final BlockchainMessageService blockchainService = new BlockchainMessageService();
    private final Member currentUser;
    private ChatRoom currentChatRoom;

    private final VBox messagesContainer;
    private TextField messageField; // final 제거
    private final ListView<Member> memberListView;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Timeline messageRefreshTimeline;
    private Timestamp lastMessageTime;
    private final int LOAD_MESSAGE_COUNT = 20;
    private int messageOffset = 0;

    public ChatView() {
        this.chatService = new ChatService();
        this.currentUser = AuthService.getCurrentUser();

        setPadding(new Insets(10));

        // 메시지 표시 영역
        messagesContainer = new VBox(10);
        messagesContainer.setPadding(new Insets(10));

        // 스크롤 가능한 메시지 컨테이너
        ScrollPane scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            // 스크롤이 맨 위로 올라가면 이전 메시지 로드
            if (newVal.doubleValue() == 0.0 && currentChatRoom != null) {
                loadMoreMessages();
            }
        });

        // 중앙 영역에 메시지 컨테이너 배치
        setCenter(scrollPane);

        // 메시지 입력 영역
        HBox inputBox = createMessageInputBox();
        setBottom(inputBox);

        // 오른쪽 멤버 목록 영역
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(0, 0, 0, 10));
        rightBox.setPrefWidth(150);

        Label membersLabel = new Label("참여자");
        membersLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        memberListView = new ListView<>();
        memberListView.setCellFactory(param -> new MemberListCell());
        memberListView.setPrefHeight(Integer.MAX_VALUE);
        VBox.setVgrow(memberListView, Priority.ALWAYS);

        rightBox.getChildren().addAll(membersLabel, memberListView);
        setRight(rightBox);
    }

    /**
     * 메시지 입력 영역 생성
     */
    private HBox createMessageInputBox() {
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10, 0, 0, 0));
        inputBox.setAlignment(Pos.CENTER);

        messageField = new TextField(); // 여기서 초기화
        messageField.setPromptText("메시지를 입력하세요...");
        messageField.setOnAction(e -> sendMessage());
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendButton = new Button("전송");
        sendButton.setOnAction(e -> sendMessage());

        inputBox.getChildren().addAll(messageField, sendButton);
        return inputBox;
    }

    /**
     * 채팅방 설정
     */
    public void setChatRoom(ChatRoom chatRoom) {
        // 이전 채팅방 리소스 정리
        if (messageRefreshTimeline != null) {
            messageRefreshTimeline.stop();
        }

        this.currentChatRoom = chatRoom;
        messageOffset = 0;
        lastMessageTime = null;

        // UI 초기화
        messagesContainer.getChildren().clear();
        memberListView.getItems().clear();

        if (chatRoom != null) {
            // 채팅방 제목 설정 (1:1 채팅인 경우 상대방 이름으로 표시)
            String displayName = chatRoom.getChatRoomName();

            if (!chatRoom.isGroupChat()) {
                // 1:1 채팅방인 경우 이름 포맷을 확인
                String originalName = chatRoom.getChatRoomName();
                if (originalName.startsWith("1:1_")) {
                    // "1:1_멤버ID1_멤버ID2" 형식에서 상대방 ID 추출
                    String[] parts = originalName.split("_");
                    if (parts.length == 3) {
                        int id1 = Integer.parseInt(parts[1]);
                        int id2 = Integer.parseInt(parts[2]);

                        // 상대방 ID 결정
                        int targetId = (id1 == currentUser.getMemberId()) ? id2 : id1;

                        // 상대방 정보 조회
                        MemberRepository memberRepo = new MemberRepository();
                        Optional<Member> targetMember = memberRepo.findById(targetId);

                        if (targetMember.isPresent()) {
                            displayName = targetMember.get().getNickname() + "님과의 대화";
                        }
                    }
                }
            }

            // 채팅방 상단 영역 구성 (제목 + 나가기 버튼)
            HBox topBox = new HBox(10);
            topBox.setAlignment(Pos.CENTER_LEFT);
            topBox.setPadding(new Insets(5));

            Label titleLabel = new Label(displayName);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            // 나가기 버튼
            Button leaveButton = new Button("채팅방 나가기");
            leaveButton.setOnAction(e -> leaveChatRoom());

            topBox.getChildren().addAll(titleLabel, leaveButton);
            setTop(topBox);

            // 메시지 로드
            loadMessages(false);

            // 참여자 목록 로드
            loadMembers();

            // 자동 새로고침 시작
            startMessageRefresher();
        } else {
            setTop(null);
        }
    }

    /**
     * 채팅방 나가기
     */
    private void leaveChatRoom() {
        if (currentChatRoom == null) {
            return;
        }

        // 나가기 확인 대화상자
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("채팅방 나가기");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 이 채팅방에서 나가시겠습니까?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 채팅방 나가기 실행
            ChatService chatService = new ChatService();
            ChatService.ChatResult leaveResult = chatService.leaveChatRoom(
                    currentChatRoom.getChatRoomId(), currentUser.getMemberId());

            if (leaveResult.isSuccess()) {
                // 성공 메시지 표시
                showAlert(Alert.AlertType.INFORMATION, "채팅방 나가기", leaveResult.getMessage());

                // 자원 해제
                dispose();

                // 부모 BorderPane 찾기
                if (getParent() instanceof BorderPane) {
                    BorderPane parent = (BorderPane) getParent();

                    // 뒤로가기 버튼이 있는 HBox 제거
                    parent.setTop(null);

                    try {
                        // 채팅방 목록으로 돌아가기
                        ChatRoomsView chatRoomsView = new ChatRoomsView(
                                (Stage) getScene().getWindow());

                        // ChatRoomsView의 선택 핸들러 재설정
                        chatRoomsView.setOnChatRoomSelectedCallback(chatRoom -> {
                            // 새로운 ChatView 인스턴스 생성
                            ChatView newChatView = new ChatView();
                            newChatView.setChatRoom(chatRoom);

                            // 뒤로가기 버튼 추가
                            Button backButton = new Button("← 채팅방 목록");
                            backButton.setOnAction(ev -> {
                                newChatView.setChatRoom(null);
                                newChatView.dispose();
                                chatRoomsView.loadChatRooms();
                                parent.setCenter(chatRoomsView);
                                parent.setTop(null);
                            });

                            HBox topBox = new HBox(backButton);
                            topBox.setPadding(new Insets(5));

                            parent.setCenter(newChatView);
                            parent.setTop(topBox);
                        });

                        chatRoomsView.loadChatRooms();
                        parent.setCenter(chatRoomsView);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "오류 발생",
                                "채팅방 나가기 후 화면 전환 중 오류가 발생했습니다: " + e.getMessage());
                    }
                }
            } else {
                // 실패 메시지 표시
                showAlert(Alert.AlertType.ERROR, "채팅방 나가기 실패", leaveResult.getMessage());
            }
        }
    }

    /**
     * 메시지 전송
     */
    private void sendMessage() {
        if (currentChatRoom == null) {
            return;
        }

        String messageText = messageField.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }

        ChatService.ChatResult result = chatService.sendMessage(
                currentChatRoom.getChatRoomId(), currentUser.getMemberId(), messageText);

        if (result.isSuccess()) {
            messageField.clear();
            loadMessages(true); // 최신 메시지만 로드
        } else {
            showAlert(Alert.AlertType.ERROR, "메시지 전송 실패", result.getMessage());
        }
    }

    /**
     * 메시지 목록 로드
     * @param onlyNewMessages true면 새 메시지만, false면 전체 로드
     */
    private void loadMessages(boolean onlyNewMessages) {
        if (currentChatRoom == null) {
            return;
        }

        if (onlyNewMessages && lastMessageTime != null) {
            // 새 메시지만 로드
            List<Message> newMessages = chatService.getNewMessages(
                    currentChatRoom.getChatRoomId(), lastMessageTime);

            for (Message message : newMessages) {
                addMessageToUI(message);
                if (message.getCreatedAt().after(lastMessageTime)) {
                    lastMessageTime = message.getCreatedAt();
                }
            }

            // 새 메시지가 있으면 스크롤을 아래로 이동
            if (!newMessages.isEmpty()) {
                Platform.runLater(() -> {
                    ScrollPane scrollPane = (ScrollPane) getCenter();
                    scrollPane.setVvalue(1.0);
                });
            }
        } else {
            // 초기 로드 - 최근 메시지부터 일정 개수만큼
            List<Message> messages = chatService.getMessagesByChatRoomId(
                    currentChatRoom.getChatRoomId(), LOAD_MESSAGE_COUNT, messageOffset);

            // UI에 메시지 추가
            messagesContainer.getChildren().clear();

            String currentDateStr = null;
            for (Message message : messages) {
                // 날짜가 바뀌면 날짜 구분선 추가
                String messageDate = dateFormat.format(message.getCreatedAt());
                if (!messageDate.equals(currentDateStr)) {
                    currentDateStr = messageDate;
                    addDateSeparator(currentDateStr);
                }

                addMessageToUI(message);

                // 가장 최근 메시지 시간 기록
                if (lastMessageTime == null || message.getCreatedAt().after(lastMessageTime)) {
                    lastMessageTime = message.getCreatedAt();
                }
            }

            // 스크롤을 아래로 이동
            Platform.runLater(() -> {
                ScrollPane scrollPane = (ScrollPane) getCenter();
                scrollPane.setVvalue(1.0);
            });
        }
    }

    /**
     * 이전 메시지 더 로드 (스크롤 위로 당길 때)
     */
    private void loadMoreMessages() {
        if (currentChatRoom == null) {
            return;
        }

        messageOffset += LOAD_MESSAGE_COUNT;

        List<Message> olderMessages = chatService.getMessagesByChatRoomId(
                currentChatRoom.getChatRoomId(), LOAD_MESSAGE_COUNT, messageOffset);

        if (!olderMessages.isEmpty()) {
            // 현재 스크롤 위치 저장
            ScrollPane scrollPane = (ScrollPane) getCenter();
            double currentHeight = messagesContainer.getHeight();

            // 메시지 표시 전 현재 첫 번째 메시지 노드 저장
            Node firstNode = messagesContainer.getChildren().isEmpty() ?
                    null : messagesContainer.getChildren().get(0);

            String currentDateStr = null;
            if (!messagesContainer.getChildren().isEmpty() &&
                    messagesContainer.getChildren().get(0) instanceof Label) {
                Label dateLabel = (Label) messagesContainer.getChildren().get(0);
                currentDateStr = dateLabel.getText();
            }

            // 이전 메시지 추가
            for (int i = olderMessages.size() - 1; i >= 0; i--) {
                Message message = olderMessages.get(i);

                // 날짜가 바뀌면 날짜 구분선 추가
                String messageDate = dateFormat.format(message.getCreatedAt());
                if (!messageDate.equals(currentDateStr)) {
                    currentDateStr = messageDate;
                    addDateSeparatorAtTop(currentDateStr);
                }

                addMessageToUIAtTop(message);
            }

            // 스크롤 위치 조정
            Platform.runLater(() -> {
                if (firstNode != null) {
                    double newHeight = messagesContainer.getHeight();
                    double offset = newHeight - currentHeight;
                    scrollPane.setVvalue(offset / newHeight);
                }
            });
        }
    }

    /**
     * 메시지를 UI에 추가
     */
    private void addMessageToUI(Message message) {
        HBox messageBox = createMessageBox(message);
        messagesContainer.getChildren().add(messageBox);
    }

    /**
     * 메시지를 UI 상단에 추가 (이전 메시지 로드 시)
     */
    private void addMessageToUIAtTop(Message message) {
        HBox messageBox = createMessageBox(message);
        messagesContainer.getChildren().add(0, messageBox);
    }

    /**
     * 날짜 구분선 추가
     */
    private void addDateSeparator(String dateStr) {
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #808080; -fx-padding: 5 0;");
        dateLabel.setMaxWidth(Double.MAX_VALUE);
        dateLabel.setAlignment(Pos.CENTER);

        HBox separator = new HBox();
        separator.setAlignment(Pos.CENTER);
        separator.getChildren().add(dateLabel);

        messagesContainer.getChildren().add(separator);
    }

    /**
     * 날짜 구분선을 상단에 추가 (이전 메시지 로드 시)
     */
    private void addDateSeparatorAtTop(String dateStr) {
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #808080; -fx-padding: 5 0;");
        dateLabel.setMaxWidth(Double.MAX_VALUE);
        dateLabel.setAlignment(Pos.CENTER);

        HBox separator = new HBox();
        separator.setAlignment(Pos.CENTER);
        separator.getChildren().add(dateLabel);

        messagesContainer.getChildren().add(0, separator);
    }

    /**
     * 메시지 박스 생성 (우클릭 기능 추가)
     */
    private HBox createMessageBox(Message message) {
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(5));

        boolean isMyMessage = message.getSenderId() == currentUser.getMemberId();

        if (isMyMessage) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);

            VBox contentBox = new VBox(5);
            contentBox.setAlignment(Pos.CENTER_RIGHT);

            Label timeLabel = new Label(timeFormat.format(message.getCreatedAt()));
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #808080;");

            Label messageLabel = new Label(message.getMessageContent());
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 8; -fx-background-radius: 8;");
            messageLabel.setMaxWidth(300);

            // 우클릭 메뉴 추가
            addRightClickMenu(messageLabel, message);

            contentBox.getChildren().addAll(timeLabel, messageLabel);
            messageBox.getChildren().add(contentBox);
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);

            Circle profileCircle = new Circle(20, Color.LIGHTGRAY);

            VBox contentBox = new VBox(5);

            Label nameLabel = new Label(message.getSender().getNickname());
            nameLabel.setStyle("-fx-font-weight: bold;");

            HBox messageTimeBox = new HBox(10);

            Label messageLabel = new Label(message.getMessageContent());
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 8; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8;");
            messageLabel.setMaxWidth(300);

            // 우클릭 메뉴 추가
            addRightClickMenu(messageLabel, message);

            Label timeLabel = new Label(timeFormat.format(message.getCreatedAt()));
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #808080;");

            messageTimeBox.getChildren().addAll(messageLabel, timeLabel);
            messageTimeBox.setAlignment(Pos.BOTTOM_LEFT);

            contentBox.getChildren().addAll(nameLabel, messageTimeBox);
            messageBox.getChildren().addAll(profileCircle, contentBox);
        }

        return messageBox;
    }

    /**
     * 메시지 라벨에 우클릭 메뉴 추가
     */
    private void addRightClickMenu(Label messageLabel, Message message) {
        // 우클릭 메뉴 생성
        ContextMenu contextMenu = new ContextMenu();

        MenuItem signItem = new MenuItem("전자서명 추가");
        signItem.setOnAction(e -> handleSignAction(message));

        MenuItem viewSignersItem = new MenuItem("전자서명 목록 조회 (사용자 ID)");
        viewSignersItem.setOnAction(e -> {
            try {
                new SignView().showSigners(message);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "오류", "전자서명 목록 조회 중 오류가 발생했습니다: " + ex.getMessage());
            }
        });


        contextMenu.getItems().addAll(signItem, viewSignersItem);

        // 우클릭 이벤트 처리
        messageLabel.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(messageLabel, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
    }

    /**
     * 전자서명 처리
     */
    private void handleSignAction(Message message) {
        try {
            SignDialog dialog = new SignDialog();

            boolean alreadySigned = blockchainService.getSigned(message.getBlockchainMessageId(), currentUser.getUserId());
            if (alreadySigned) {
                dialog.showAlreadySignedAlert();
                return;
            }

            boolean confirmed = dialog.showConfirmation();
            if (!confirmed) return;

            boolean success = blockchainService.signMessage(message, currentUser);
            if (success) {
                dialog.showSuccessAlert();
            } else {
                dialog.showFailureAlert();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "전자서명 오류", "전자서명 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 채팅방 참여자 목록 로드
     */
    private void loadMembers() {
        if (currentChatRoom == null) {
            return;
        }

        List<Member> members = chatService.getChatRoomMembers(currentChatRoom.getChatRoomId());
        memberListView.getItems().clear();
        memberListView.getItems().addAll(members);
    }

    /**
     * 메시지 자동 새로고침 시작
     */
    private void startMessageRefresher() {
        if (messageRefreshTimeline != null) {
            messageRefreshTimeline.stop();
        }

        // 3초마다 새 메시지 확인
        messageRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> {
                    if (currentChatRoom != null) {
                        loadMessages(true); // 새 메시지만 로드
                    }
                })
        );

        messageRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        messageRefreshTimeline.play();
    }

    /**
     * 자원 해제
     */
    public void dispose() {
        if (messageRefreshTimeline != null) {
            messageRefreshTimeline.stop();
            messageRefreshTimeline = null;
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
     * 참여자 정보를 표시하는 커스텀 리스트 셀
     */
    private class MemberListCell extends ListCell<Member> {
        @Override
        protected void updateItem(Member member, boolean empty) {
            super.updateItem(member, empty);

            if (empty || member == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox vbox = new VBox(3);
                vbox.setPadding(new Insets(5));

                Label nameLabel = new Label(member.getNickname());
                nameLabel.setStyle("-fx-font-weight: bold;");

                // 현재 사용자인 경우 표시
                if (member.getMemberId() == currentUser.getMemberId()) {
                    nameLabel.setText(nameLabel.getText() + " (나)");
                }

                // 온라인 상태 표시
                HBox statusBox = new HBox(5);
                statusBox.setAlignment(Pos.CENTER_LEFT);

                Circle statusCircle = new Circle(4);
                statusCircle.setFill(member.getStatus().equals("ONLINE") ? Color.GREEN : Color.GRAY);

                Label statusLabel = new Label(member.getStatus());
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " +
                        (member.getStatus().equals("ONLINE") ? "green" : "gray") + ";");

                statusBox.getChildren().addAll(statusCircle, statusLabel);

                vbox.getChildren().addAll(nameLabel, statusBox);

                setText(null);
                setGraphic(vbox);
            }
        }
    }
}
