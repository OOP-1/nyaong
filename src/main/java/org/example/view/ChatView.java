// src/main/java/org/example/view/ChatView.java
package org.example.view;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.example.model.ChatRoom;
import org.example.model.Member;
import org.example.model.Message;
import org.example.repository.MemberRepository;
import org.example.repository.MessageRepository;
import org.example.service.AuthService;
import org.example.service.BlockchainMessageService;
import org.example.service.ChatService;
import org.example.service.ChatService.ChatResult;
import org.example.socket.ChatMessage;
import org.example.socket.ChatSocketClient;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 채팅 화면
 * 채팅방을 선택하면 메인 화면에 표시됨
 */
public class ChatView extends BorderPane {
    private final ChatService chatService;
    private final MessageRepository messageRepository;
    private final BlockchainMessageService blockchainService = new BlockchainMessageService();
    private final Member currentUser;
    private ChatRoom currentChatRoom;

    private final VBox messagesContainer;
    private TextField messageField;
    private final ListView<Member> memberListView;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Consumer<ChatMessage> messageListener;  // 메시지 수신 리스너
    private Timestamp lastMessageTime;
    private final int LOAD_MESSAGE_COUNT = 20;
    private int messageOffset = 0;

    // 소켓 클라이언트
    private final ChatSocketClient socketClient;

    public ChatView() {
        this.chatService = new ChatService();
        this.messageRepository = new MessageRepository();
        this.currentUser = AuthService.getCurrentUser();
        this.socketClient = ChatSocketClient.getInstance();

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

        // 소켓 연결 상태 확인 및 필요시 재연결
        ensureSocketConnection();
    }

    /**
     * 소켓 연결 확인 및 필요시 재연결
     * 중요: 동기 방식으로 연결을 확인하고 명확한 결과를 반환
     */
    private boolean ensureSocketConnection() {
        if (!socketClient.isConnected()) {
            System.out.println("소켓 연결이 끊어져 있어 재연결 시도합니다...");

            // 최대 3번까지 재시도
            for (int i = 0; i < 3; i++) {
                System.out.println("연결 시도 " + (i + 1) + "...");
                boolean connected = socketClient.connect(currentUser);
                if (connected) {
                    System.out.println("소켓 재연결 성공");
                    return true;
                }

                try {
                    // 재시도 간 잠시 대기
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.err.println("소켓 재연결 실패");
            return false;
        }
        return true;
    }

    /**
     * 메시지 입력 영역 생성
     */
    private HBox createMessageInputBox() {
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10, 0, 0, 0));
        inputBox.setAlignment(Pos.CENTER);

        messageField = new TextField();
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
        if (currentChatRoom != null && messageListener != null) {
            socketClient.removeMessageListener(currentChatRoom.getChatRoomId(), messageListener);
        }

        this.currentChatRoom = chatRoom;
        messageOffset = 0;
        lastMessageTime = null;

        // UI 초기화
        messagesContainer.getChildren().clear();
        memberListView.getItems().clear();

        if (chatRoom == null) {
            setTop(null);
            return;
        }

        // 채팅방 제목 설정 및 UI 요소 구성 (기존 코드 유지)
        setupChatRoomUI(chatRoom);

        // 메시지 로드
        loadMessages();

        // 참여자 목록 로드
        loadMembers();

        // 메시지 리스너 먼저 등록 - 비동기 처리 전에
        messageListener = this::handleIncomingMessage;
        socketClient.addMessageListener(chatRoom.getChatRoomId(), messageListener);

        // 로딩 인디케이터 추가
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(30, 30);
        HBox loadingBox = new HBox(progressIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(10));
        Label loadingLabel = new Label("채팅방 연결 중...");
        loadingBox.getChildren().add(loadingLabel);
        messagesContainer.getChildren().add(loadingBox);

        // 소켓 연결 및 채팅방 입장 처리를 새 스레드에서 실행
        new Thread(() -> {
            try {
                // 소켓 연결 상태 확인 - 여기서 시간이 오래 걸릴 수 있음
                boolean connected = ensureSocketConnection();

                if (!connected) {
                    Platform.runLater(() -> {
                        // 로딩 표시 제거
                        messagesContainer.getChildren().removeIf(node ->
                                node instanceof HBox && ((HBox) node).getChildren().get(0) instanceof ProgressIndicator);

                        showAlert(Alert.AlertType.ERROR, "연결 오류",
                                "채팅 서버에 연결할 수 없습니다. 네트워크 연결을 확인하고 다시 시도해 주세요.");
                    });
                    return;
                }

                // 채팅방 참여 명령 전송
                boolean joined = socketClient.joinChatRoom(chatRoom.getChatRoomId());

                if (!joined) {
                    Platform.runLater(() -> {
                        // 로딩 표시 제거
                        messagesContainer.getChildren().removeIf(node ->
                                node instanceof HBox && ((HBox) node).getChildren().get(0) instanceof ProgressIndicator);

                        showAlert(Alert.AlertType.ERROR, "채팅방 입장 오류",
                                "채팅방 입장 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
                    });
                    return;
                }

                // 채팅방 멤버 목록 업데이트
                List<Member> members = chatService.getChatRoomMembers(chatRoom.getChatRoomId());
                List<Integer> memberIds = members.stream()
                        .map(Member::getMemberId)
                        .collect(java.util.stream.Collectors.toList());
                socketClient.updateChatRoomMembers(chatRoom.getChatRoomId(), memberIds);

                // 모든 처리가 완료되면 로딩 표시 제거
                Platform.runLater(() -> {
                    messagesContainer.getChildren().removeIf(node ->
                            node instanceof HBox && ((HBox) node).getChildren().get(0) instanceof ProgressIndicator);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    // 로딩 표시 제거
                    messagesContainer.getChildren().removeIf(node ->
                            node instanceof HBox && ((HBox) node).getChildren().get(0) instanceof ProgressIndicator);

                    showAlert(Alert.AlertType.ERROR, "오류 발생",
                            "채팅방 입장 중 오류가 발생했습니다: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * 채팅방 UI 설정 (기존 코드에서 추출)
     */
    private void setupChatRoomUI(ChatRoom chatRoom) {
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
    }

    /**
     * 소켓으로부터 수신된 메시지 처리 - 날짜 구분선 중복 문제 해결
     */
    private void handleIncomingMessage(ChatMessage chatMessage) {
        Platform.runLater(() -> {
            // ChatMessage를 Message 객체로 변환
            Message message = messageRepository.convertChatMessageToMessage(chatMessage);

            // 스크롤이 맨 아래에 있는지 확인 (새 메시지 자동 스크롤 여부 결정)
            ScrollPane scrollPane = (ScrollPane) getCenter();
            boolean wasAtBottom = scrollPane.getVvalue() >= 0.95; // 95% 이상 스크롤되어 있으면 맨 아래로 간주

            // 날짜가 바뀌면 날짜 구분선 추가
            String messageDate = dateFormat.format(message.getCreatedAt());

            // 이미 같은 날짜의 구분선이 있는지 확인 - 수정된 로직
            boolean needDateSeparator = true;
            if (!messagesContainer.getChildren().isEmpty()) {
                // 모든 노드를 역순으로 검사하여 가장 최근의 날짜 구분선 찾기
                for (int i = messagesContainer.getChildren().size() - 1; i >= 0; i--) {
                    Node node = messagesContainer.getChildren().get(i);

                    // 날짜 구분선인지 확인 (HBox 안에 Label이 있고, 날짜 형식인 경우)
                    if (node instanceof HBox) {
                        HBox hbox = (HBox) node;
                        if (!hbox.getChildren().isEmpty() && hbox.getChildren().get(0) instanceof Label) {
                            Label label = (Label) hbox.getChildren().get(0);
                            String labelText = label.getText();

                            // 날짜 형식인지 확인 (yyyy-MM-dd 형식)
                            if (labelText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                // 같은 날짜의 구분선이 이미 있으면 추가하지 않음
                                if (labelText.equals(messageDate)) {
                                    needDateSeparator = false;
                                }
                                break; // 가장 최근 날짜 구분선을 찾았으므로 종료
                            }
                        }
                    }
                }
            }

            if (needDateSeparator) {
                addDateSeparator(messageDate);
            }

            // 메시지 UI에 추가
            addMessageToUI(message);

            // 새 메시지가 도착했을 때 자동 스크롤 (사용자가 맨 아래에 있었던 경우에만)
            if (wasAtBottom) {
                // UI 업데이트가 완료된 후 스크롤 이동을 위해 추가 지연
                Platform.runLater(() -> {
                    scrollPane.setVvalue(1.0);

                    // 한 번 더 확실히 하기 위해 약간의 지연 후 다시 스크롤
                    Platform.runLater(() -> {
                        scrollPane.setVvalue(1.0);
                    });
                });
            }

            // 마지막 메시지 시간 업데이트
            if (lastMessageTime == null || message.getCreatedAt().after(lastMessageTime)) {
                lastMessageTime = message.getCreatedAt();
            }
        });
    }

    /**
     * 채팅방 나가기 - 오류 수정된 버전
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

            // 현재 채팅방 정보를 final 변수로 저장 (람다에서 사용하기 위해)
            final ChatRoom chatRoomToLeave = currentChatRoom;
            final int currentUserId = currentUser.getMemberId();


            // 로딩 인디케이터 표시
            showLoadingOverlay("채팅방에서 나가는 중...");

            // 기존 UI 비활성화
            setDisable(true);

            // 백그라운드 스레드에서 처리
            new Thread(() -> {
                try {
                    // 1. 소켓 연결 확인
                    if (!ensureSocketConnection()) {
                        Platform.runLater(() -> {
                            setDisable(false);
                            removeLoadingOverlay();
                            showAlert(Alert.AlertType.ERROR, "연결 오류",
                                    "서버 연결이 끊어져 있습니다. 다시 시도해 주세요.");
                        });
                        return;
                    }

                    // 2. 소켓으로 퇴장 명령 전송
                    boolean socketResult = socketClient.leaveChatRoom(chatRoomToLeave.getChatRoomId());
                    if (!socketResult) {
                        System.err.println("소켓 퇴장 명령 전송 실패, 계속 진행...");
                    }

                    // 3. 데이터베이스에서 채팅방 나가기 처리
                    ChatService chatService = new ChatService();
                    ChatService.ChatResult leaveResult = chatService.leaveChatRoom(
                            chatRoomToLeave.getChatRoomId(), currentUserId);

                    // 4. UI 스레드에서 결과 처리
                    Platform.runLater(() -> {
                        setDisable(false);
                        removeLoadingOverlay();

                        if (leaveResult.isSuccess()) {
                            // 성공 메시지 표시
                            showAlert(Alert.AlertType.INFORMATION, "채팅방 나가기", leaveResult.getMessage());

                            // 자원 해제
                            dispose();

                            // 채팅방 목록으로 돌아가기
                            navigateBackToChatRoomsList();
                        } else {
                            // 실패 메시지 표시
                            showAlert(Alert.AlertType.ERROR, "채팅방 나가기 실패", leaveResult.getMessage());
                        }
                    });

                } catch (Exception e) {
                    System.err.println("채팅방 나가기 중 오류 발생: " + e.getMessage());
                    Platform.runLater(() -> {
                        setDisable(false);
                        removeLoadingOverlay();
                        showAlert(Alert.AlertType.ERROR, "오류 발생",
                                "채팅방 나가기 중 오류가 발생했습니다: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    /**
     * 로딩 오버레이 표시
     */
    private void showLoadingOverlay(String message) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(30, 30);

        Label loadingLabel = new Label(message);
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(20));
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        overlay.getChildren().add(loadingBox);
        overlay.setId("loadingOverlay"); // ID 설정으로 나중에 찾기 쉽게

        // 부모에 오버레이 추가
        if (getParent() instanceof BorderPane) {
            BorderPane parent = (BorderPane) getParent();
            StackPane container = new StackPane();
            container.getChildren().addAll(this, overlay);
            parent.setCenter(container);
        }
    }

    /**
     * 로딩 오버레이 제거
     */
    private void removeLoadingOverlay() {
        if (getParent() instanceof StackPane) {
            StackPane container = (StackPane) getParent();
            // 오버레이 제거
            container.getChildren().removeIf(node ->
                    node instanceof StackPane && "loadingOverlay".equals(node.getId()));

            // 원래 구조로 복원
            if (container.getParent() instanceof BorderPane) {
                BorderPane parent = (BorderPane) container.getParent();
                parent.setCenter(this);
            }
        }
    }

    /**
     * 채팅방 목록으로 돌아가기
     */
    private void navigateBackToChatRoomsList() {
        try {
            // 부모 BorderPane 찾기
            BorderPane parent = findParentBorderPane();

            if (parent != null) {
                // 뒤로가기 버튼이 있는 HBox 제거
                parent.setTop(null);

                // 채팅방 목록으로 돌아가기
                ChatRoomsView chatRoomsView = new ChatRoomsView((Stage) getScene().getWindow());

                // ChatRoomsView의 선택 핸들러 재설정
                chatRoomsView.setOnChatRoomSelectedCallback(chatRoom -> {
                    createNewChatView(chatRoom, parent, chatRoomsView);
                });

                chatRoomsView.loadChatRooms();
                parent.setCenter(chatRoomsView);
            }
        } catch (Exception e) {
            System.err.println("화면 전환 중 오류 발생: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "오류 발생",
                    "채팅방 나가기 후 화면 전환 중 오류가 발생했습니다.");
        }
    }

    /**
     * 부모 BorderPane 찾기
     */
    private BorderPane findParentBorderPane() {
        if (getParent() instanceof BorderPane) {
            return (BorderPane) getParent();
        } else if (getParent() instanceof StackPane &&
                getParent().getParent() instanceof BorderPane) {
            return (BorderPane) getParent().getParent();
        }
        return null;
    }

    /**
     * 새로운 ChatView 생성 및 설정
     */
    private void createNewChatView(ChatRoom chatRoom, BorderPane parent, ChatRoomsView chatRoomsView) {
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
    }

    /**
     * 메시지 전송 - 스크롤 문제 해결
     */
    private void sendMessage() {
        if (currentChatRoom == null) {
            return;
        }

        String messageText = messageField.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // 소켓 연결 확인
        ensureSocketConnection();

        // 메시지 전송 (데이터베이스 저장 + 소켓 전송)
        ChatResult chatResult = chatService.sendMessage(
                currentChatRoom.getChatRoomId(), currentUser.getMemberId(), messageText);

        if (chatResult.isSuccess()) {
            messageField.clear();

            // 메시지 전송 후 스크롤을 맨 아래로 이동
            Platform.runLater(() -> {
                ScrollPane scrollPane = (ScrollPane) getCenter();
                scrollPane.setVvalue(1.0);
            });
        } else {
            showAlert(
                Alert.AlertType.ERROR,
                "메시지 전송 실패",
                chatResult.getMessage()        // ← 서비스에서 온 상세 메시지
            );        }
    }

    /**
     * 메시지 목록 로드 - 실제 데이터베이스에서 메시지를 가져와서 표시
     */
    private void loadMessages() {
        if (currentChatRoom == null) {
            return;
        }

        System.out.println("📋 채팅방 " + currentChatRoom.getChatRoomId() + "의 메시지 로딩 중...");

        // 데이터베이스에서 최근 메시지들 가져오기
        List<Message> messages = messageRepository.getMessagesByChatRoomId(
                currentChatRoom.getChatRoomId(), LOAD_MESSAGE_COUNT, 0);

        System.out.println("📋 로드된 메시지 수: " + messages.size());

        if (messages.isEmpty()) {
            System.out.println("📋 표시할 메시지가 없습니다.");
            // 빈 채팅방 안내 메시지 표시
            Label emptyLabel = new Label("대화를 시작해보세요!");
            emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));
            messagesContainer.getChildren().add(emptyBox);
        } else {
            // 메시지들을 UI에 표시
            displayMessages(messages);

            // 마지막 메시지 시간 업데이트
            if (!messages.isEmpty()) {
                lastMessageTime = messages.get(messages.size() - 1).getCreatedAt();
            }
        }

        // 메시지 로딩 완료 후 스크롤을 맨 아래로
        Platform.runLater(() -> {
            ScrollPane scrollPane = (ScrollPane) getCenter();
            // 레이아웃이 완료될 때까지 기다린 후 스크롤
            messagesContainer.layoutBoundsProperty().addListener(new javafx.beans.value.ChangeListener<javafx.geometry.Bounds>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends javafx.geometry.Bounds> obs,
                                    javafx.geometry.Bounds oldBounds, javafx.geometry.Bounds newBounds) {
                    Platform.runLater(() -> scrollPane.setVvalue(1.0));
                    // 리스너 제거 (한 번만 실행되도록)
                    messagesContainer.layoutBoundsProperty().removeListener(this);
                }
            });

            // 즉시 스크롤도 시도
            scrollPane.setVvalue(1.0);
        });
    }

    /**
     * 메시지 목록을 UI에 표시하는 헬퍼 메서드
     */
    private void displayMessages(List<Message> messages) {
        String currentDateStr = null;

        // *** 메시지를 시간순으로 정렬 (오래된 메시지부터 표시) ***
        List<Message> sortedMessages = new ArrayList<>(messages);
        sortedMessages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));

        for (Message message : sortedMessages) {
            // 날짜가 바뀌면 날짜 구분선 추가
            String messageDate = dateFormat.format(message.getCreatedAt());
            if (!messageDate.equals(currentDateStr)) {
                currentDateStr = messageDate;
                addDateSeparator(currentDateStr);
            }

            // 메시지 UI에 추가
            addMessageToUI(message);
        }
    }

    /**
     * 스크롤을 맨 아래로 강제 이동하는 헬퍼 메서드
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            ScrollPane scrollPane = (ScrollPane) getCenter();
            if (scrollPane != null) {
                // 여러 번 시도하여 확실히 스크롤되도록 함
                for (int i = 0; i < 3; i++) {
                    final int attempt = i;
                    Platform.runLater(() -> {
                        scrollPane.setVvalue(1.0);
                        if (attempt == 2) { // 마지막 시도에서 추가 확인
                            Platform.runLater(() -> scrollPane.setVvalue(1.0));
                        }
                    });
                }
            }
        });
    }

    /**
     * 새 메시지 알림 및 자동 스크롤을 위한 헬퍼 메서드
     */
    private void addMessageAndScroll(Message message) {
        // 스크롤 위치 확인
        ScrollPane scrollPane = (ScrollPane) getCenter();
        boolean shouldAutoScroll = scrollPane.getVvalue() >= 0.9; // 90% 이상이면 자동 스크롤

        // 메시지 추가
        addMessageToUI(message);

        // 자동 스크롤이 필요한 경우
        if (shouldAutoScroll) {
            scrollToBottom();
        } else {
            // 스크롤이 위에 있는 경우 새 메시지 알림 표시 (선택적 기능)
            showNewMessageNotification();
        }
    }

    /**
     * 새 메시지 알림 표시 (사용자가 스크롤을 위로 올려두었을 때)
     */
    private void showNewMessageNotification() {
        // 새 메시지 알림 버튼을 표시하여 사용자가 클릭하면 맨 아래로 이동
        // 이 기능은 선택적으로 구현할 수 있습니다
        Platform.runLater(() -> {
            // 간단한 알림 구현 예시
            if (getBottom() instanceof HBox) {
                HBox bottomBox = (HBox) getBottom();

                // 이미 알림이 있는지 확인
                boolean hasNotification = bottomBox.getChildren().stream()
                        .anyMatch(node -> node.getId() != null && node.getId().equals("newMessageNotification"));

                if (!hasNotification) {
                    Button newMessageBtn = new Button("새 메시지 ↓");
                    newMessageBtn.setId("newMessageNotification");
                    newMessageBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
                    newMessageBtn.setOnAction(e -> {
                        scrollToBottom();
                        bottomBox.getChildren().remove(newMessageBtn);
                    });

                    bottomBox.getChildren().add(0, newMessageBtn);

                    // 3초 후 자동으로 제거
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                        bottomBox.getChildren().remove(newMessageBtn);
                    }));
                    timeline.play();
                }
            }
        });
    }

    /**
     * 이전 메시지 더 로드 (스크롤 위로 당길 때)
     */
    private void loadMoreMessages() {
        if (currentChatRoom == null) {
            return;
        }

        messageOffset += LOAD_MESSAGE_COUNT;

        List<Message> olderMessages = messageRepository.getMessagesByChatRoomId(
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
     * 채팅방 설정 (소켓 처리 없이 - 이미 처리됨)
     */
    public void setChatRoomWithoutSocket(ChatRoom chatRoom, boolean socketConnected) {
        // 이전 채팅방 리소스 정리
        if (currentChatRoom != null && messageListener != null) {
            socketClient.removeMessageListener(currentChatRoom.getChatRoomId(), messageListener);
        }

        this.currentChatRoom = chatRoom;
        messageOffset = 0;
        lastMessageTime = null;

        // UI 초기화
        messagesContainer.getChildren().clear();
        memberListView.getItems().clear();

        if (chatRoom == null) {
            setTop(null);
            return;
        }

        // 채팅방 제목 설정 및 UI 구성
        setupChatRoomUI(chatRoom);

        // 소켓 연결 여부 검사
        if (!socketConnected) {
            VBox errorBox = new VBox(10);
            errorBox.setAlignment(Pos.CENTER);

            Label errorLabel = new Label("채팅 서버 연결 오류");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");

            Label detailLabel = new Label("메시지를 보내거나 받을 수 없습니다.\n다시 연결하려면 채팅방을 다시 선택해주세요.");
            detailLabel.setStyle("-fx-text-fill: #555;");

            Button reconnectButton = new Button("재연결 시도");
            reconnectButton.setOnAction(e -> {
                // 현재 채팅방 정보 저장
                ChatRoom current = this.currentChatRoom;

                // UI 초기화
                dispose();

                // 재연결 시도
                boolean connected = socketClient.connect(AuthService.getCurrentUser());

                if (connected) {
                    // 성공 시 채팅방 다시 설정
                    setChatRoom(current);
                } else {
                    // 실패 시 오류 메시지
                    VBox reconnectErrorBox = new VBox(10);
                    reconnectErrorBox.setAlignment(Pos.CENTER);
                    reconnectErrorBox.getChildren().add(new Label("재연결 실패. 나중에 다시 시도해주세요."));
                    messagesContainer.getChildren().add(reconnectErrorBox);
                }
            });

            errorBox.getChildren().addAll(errorLabel, detailLabel, reconnectButton);
            messagesContainer.getChildren().add(errorBox);
            return;
        }

        // 메시지 리스너 등록
        messageListener = this::handleIncomingMessage;
        socketClient.addMessageListener(chatRoom.getChatRoomId(), messageListener);

        // 메시지 로드
        loadMessages();

        // 참여자 목록 로드
        loadMembers();

        System.out.println("ChatView 설정 완료");
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

            // 발신자 이름
            Label nameLabel = new Label(message.getSender() != null ?
                    message.getSender().getNickname() : "알 수 없음");
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
     * 자원 해제
     */
    public void dispose() {
        // 소켓 메시지 리스너 제거
        if (currentChatRoom != null && messageListener != null) {
            socketClient.removeMessageListener(currentChatRoom.getChatRoomId(), messageListener);
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