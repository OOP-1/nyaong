// src/main/java/org/example/view/ChatRoomsView.java
package org.example.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.ChatRoom;
import org.example.model.Member;
import org.example.repository.MemberRepository;
import org.example.service.AuthService;
import org.example.service.ChatService;
import org.example.socket.ChatCommand;
import org.example.socket.ChatCommandType;
import org.example.socket.ChatSocketClient;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 채팅방 목록 및 관리 화면
 * MainView의 채팅 탭에 표시됨
 */
public class ChatRoomsView extends VBox {
    private final Stage stage;
    private final ChatService chatService;
    private final Member currentUser;
    private final ListView<ChatRoom> chatRoomsListView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Consumer<ChatRoom> onChatRoomSelectedCallback;
    private Consumer<ChatCommand> commandListener;
    private final ChatSocketClient socketClient;

    public ChatRoomsView(Stage stage) {
        this.stage = stage;
        this.chatService = new ChatService();
        this.currentUser = AuthService.getCurrentUser();
        this.socketClient = ChatSocketClient.getInstance();

        setPadding(new Insets(10));
        setSpacing(10);

        // 상단 영역 - 새 채팅방 생성 버튼
        HBox topBox = createTopBox();
        getChildren().add(topBox);

        // 채팅방 목록
        chatRoomsListView = new ListView<>();
        chatRoomsListView.setCellFactory(param -> new ChatRoomListCell());
//        chatRoomsListView.getSelectionModel().selectedItemProperty().addListener(
//                (observable, oldValue, newValue) -> {
//                    if (newValue != null && onChatRoomSelectedCallback != null) {
//                        onChatRoomSelectedCallback.accept(newValue);
//                    }
//                });
        VBox.setVgrow(chatRoomsListView, Priority.ALWAYS);
        getChildren().add(chatRoomsListView);

        // 채팅방 목록 로드
        loadChatRooms();

        // 소켓 명령 리스너 등록
        setupSocketListener();
    }

    /**
     * 소켓 명령 리스너 설정
     */
    private void setupSocketListener() {
        commandListener = command -> {
            if (command.getType() == ChatCommandType.REFRESH) {
                // 채팅방 목록 새로고침 명령 처리
                Platform.runLater(this::loadChatRooms);
            }
        };

        // 리스너 등록
        socketClient.addCommandListener(commandListener);
    }

    /**
     * 상단 영역 생성 (새 채팅방 생성 버튼 등)
     */
    private HBox createTopBox() {
        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(0, 0, 10, 0));


        //  채팅 버튼
        Button newChatButton = new Button("채팅방 만들기");
        newChatButton.setOnAction(e -> showGroupChatDialog());

        // 새로고침 버튼
        Button refreshButton = new Button("새로고침");
        refreshButton.setOnAction(e -> loadChatRooms());

        topBox.getChildren().addAll(newChatButton, refreshButton);
        return topBox;
    }

    /**
     * 채팅방 목록 로드
     */
    public void loadChatRooms() {
        List<ChatRoom> chatRooms = chatService.getChatRoomsByMemberId(currentUser.getMemberId());

        Platform.runLater(() -> {
            chatRoomsListView.getItems().clear();
            chatRoomsListView.getItems().addAll(chatRooms);
        });
    }

    /**
     * 채팅방 선택 시 콜백 설정
     */
    public void setOnChatRoomSelectedCallback(Consumer<ChatRoom> callback) {
        this.onChatRoomSelectedCallback = callback;
    }

    /**
     * 새 1:1 채팅 다이얼로그 표시
     */
    private void showPrivateChatDialog() {
        PrivateChatDialog dialog = new PrivateChatDialog(stage);
        dialog.showAndWait().ifPresent(targetId -> {
            if (targetId > 0) {
                loadChatRooms();
            }
        });
    }

    /**
     * 새 그룹 채팅 다이얼로그 표시
     */
    private void showGroupChatDialog() {
        GroupChatDialog dialog = new GroupChatDialog(stage);
        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadChatRooms();
            }
        });
    }

    /**
     * 자원 해제
     */
    public void dispose() {
        // 소켓 명령 리스너 제거
        if (commandListener != null) {
            socketClient.removeCommandListener(commandListener);
        }
    }

    /**
     * 채팅방 항목 컨텍스트 메뉴 생성
     */
    private ContextMenu createChatRoomContextMenu(ChatRoom chatRoom) {
        ContextMenu contextMenu = new ContextMenu();

        // 채팅방 정보 메뉴 항목
        MenuItem infoMenuItem = new MenuItem("채팅방 정보");
        infoMenuItem.setOnAction(e -> showChatRoomInfo(chatRoom));

        // 채팅방 이름 변경 메뉴 항목 - 모든 채팅방에 추가
        MenuItem renameMenuItem = new MenuItem("채팅방 이름 변경");
        renameMenuItem.setOnAction(e -> renameChatRoom(chatRoom));

        // 채팅방 나가기 메뉴 항목
        MenuItem leaveMenuItem = new MenuItem("채팅방 나가기");
        leaveMenuItem.setOnAction(e -> leaveChatRoom(chatRoom));

        // 기본 메뉴 항목들 추가
        contextMenu.getItems().addAll(infoMenuItem, renameMenuItem);

        // 그룹 채팅인 경우 멤버 초대 메뉴 추가
        if (chatRoom.isGroupChat()) {
            MenuItem inviteMenuItem = new MenuItem("멤버 초대");
            inviteMenuItem.setOnAction(e -> inviteMember(chatRoom));
            contextMenu.getItems().add(inviteMenuItem);
        }

        // 나가기 메뉴는 맨 마지막에 추가
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(leaveMenuItem);

        return contextMenu;
    }

    /**
     * 채팅방 정보 표시
     */
    private void showChatRoomInfo(ChatRoom chatRoom) {
        StringBuilder infoText = new StringBuilder();
        infoText.append("채팅방 이름: ").append(chatRoom.getChatRoomName()).append("\n");
        infoText.append("유형: ").append(chatRoom.isGroupChat() ? "그룹 채팅" : "1:1 채팅").append("\n");
        infoText.append("생성 시간: ").append(dateFormat.format(chatRoom.getCreatedAt())).append("\n\n");

        infoText.append("참여자 목록:\n");
        List<Member> members = chatService.getChatRoomMembers(chatRoom.getChatRoomId());
        for (Member member : members) {
            infoText.append("- ").append(member.getNickname())
                    .append(" (").append(member.getStatus()).append(")\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("채팅방 정보");
        alert.setHeaderText(chatRoom.getChatRoomName());
        alert.setContentText(infoText.toString());
        alert.showAndWait();
    }

    /**
     * 채팅방 이름 변경
     */
    private void renameChatRoom(ChatRoom chatRoom) {
        // 현재 표시되는 이름을 기본값으로 설정
        String currentDisplayName = getCurrentDisplayName(chatRoom);

        TextInputDialog dialog = new TextInputDialog(currentDisplayName);
        dialog.setTitle("채팅방 이름 변경");
        dialog.setHeaderText("새로운 채팅방 이름을 입력하세요");
        dialog.setContentText("이름:");

        // 다이얼로그 크기 조정
        dialog.getDialogPane().setPrefWidth(400);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();

            ChatService.ChatResult updateResult = chatService.updateChatRoomName(
                    chatRoom.getChatRoomId(), newName);

            if (updateResult.isSuccess()) {
                loadChatRooms(); // 목록 새로고침
                showAlert(Alert.AlertType.INFORMATION, "채팅방 이름 변경",
                        "채팅방 이름이 '" + newName + "'로 변경되었습니다.");
            } else {
                showAlert(Alert.AlertType.ERROR, "채팅방 이름 변경 실패",
                        updateResult.getMessage());
            }
        }
    }

    /**
     * 현재 채팅방의 표시 이름 가져오기
     */
    private String getCurrentDisplayName(ChatRoom chatRoom) {
        List<Member> members = chatService.getChatRoomMembers(chatRoom.getChatRoomId());

        if (!chatRoom.isGroupChat() && members.size() == 2) {
            // 1:1 채팅의 경우 상대방 이름으로 기본값 설정
            Member otherMember = members.stream()
                    .filter(member -> member.getMemberId() != currentUser.getMemberId())
                    .findFirst()
                    .orElse(null);

            if (otherMember != null) {
                return otherMember.getNickname() + "님과의 대화";
            }
        } else if (chatRoom.isGroupChat()) {
            // 그룹 채팅의 경우
            String originalName = chatRoom.getChatRoomName();

            // 자동 생성된 이름이면 현재 멤버 기준으로 생성
            if (originalName.endsWith("의 그룹채팅")) {
                List<String> nicknames = members.stream()
                        .map(Member::getNickname)
                        .sorted()
                        .collect(java.util.stream.Collectors.toList());
                return String.join(", ", nicknames) + "의 그룹채팅";
            } else {
                // 사용자 정의 이름은 그대로
                return originalName;
            }
        }

        return chatRoom.getChatRoomName();
    }

    /**
     * 채팅방에 멤버 초대
     */
    private void inviteMember(ChatRoom chatRoom) {
        InviteMemberDialog dialog = new InviteMemberDialog(stage, chatRoom.getChatRoomId());
        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadChatRooms();
            }
        });
    }

    /**
     * 채팅방 나가기
     */
    private void leaveChatRoom(ChatRoom chatRoom) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("채팅방 나가기");
        confirmAlert.setHeaderText(null);

        // 채팅방 이름 표시 (1:1 채팅인 경우 상대방 이름으로)
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

        confirmAlert.setContentText("정말 '" + displayName + "' 채팅방에서 나가시겠습니까?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ChatService chatService = new ChatService();
            ChatService.ChatResult leaveResult = chatService.leaveChatRoom(
                    chatRoom.getChatRoomId(), currentUser.getMemberId());

            if (leaveResult.isSuccess()) {
                try {
                    // 채팅방 목록 새로고침
                    loadChatRooms();

                    // 성공 메시지 표시
                    showAlert(Alert.AlertType.INFORMATION, "채팅방 나가기",
                            "채팅방에서 나갔습니다.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "오류 발생",
                            "채팅방 나가기 후 새로고침 중 오류가 발생했습니다: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "채팅방 나가기 실패",
                        leaveResult.getMessage());
            }
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
     * 채팅방 정보를 표시하는 커스텀 리스트 셀 - 동적 이름 표시 기능 추가
     */
    private class ChatRoomListCell extends ListCell<ChatRoom> {
        @Override
        protected void updateItem(ChatRoom chatRoom, boolean empty) {
            super.updateItem(chatRoom, empty);

            if (empty || chatRoom == null) {
                setText(null);
                setGraphic(null);
                setContextMenu(null);
                setOnMouseClicked(null);
            } else {
                // 채팅방 정보 표시 레이아웃
                VBox vbox = new VBox(5);
                vbox.setPadding(new Insets(8));

                // 동적 채팅방 이름 생성
                String displayName = generateDisplayName(chatRoom);

                Label nameLabel = new Label(displayName);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                // 채팅방 유형 (그룹/1:1)
                Label typeLabel = new Label(chatRoom.isGroupChat() ? "그룹 채팅" : "1:1 채팅");
                typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #505050;");

                // 마지막 메시지 미리보기
                String previewText = chatRoom.getLastMessagePreview();
                Label previewLabel = new Label(previewText != null && !previewText.isEmpty() ?
                        previewText : "메시지가 없습니다.");
                previewLabel.setStyle("-fx-font-size: 12px;");
                previewLabel.setWrapText(true);
                previewLabel.setMaxWidth(Double.MAX_VALUE);

                // 마지막 메시지 시간
                if (chatRoom.getLastMessageTime() != null) {
                    Label timeLabel = new Label(dateFormat.format(chatRoom.getLastMessageTime()));
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #808080;");
                    vbox.getChildren().addAll(nameLabel, typeLabel, previewLabel, timeLabel);
                } else {
                    vbox.getChildren().addAll(nameLabel, typeLabel, previewLabel);
                }

                setText(null);
                setGraphic(vbox);

                // 컨텍스트 메뉴 설정
                setContextMenu(createChatRoomContextMenu(chatRoom));

                // 마우스 클릭 이벤트 처리 - 중요한 부분!
                setOnMouseClicked(event -> {
                    if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && event.getClickCount() == 1) {
                        // 좌클릭 시에만 채팅방 선택
                        if (onChatRoomSelectedCallback != null) {
                            onChatRoomSelectedCallback.accept(chatRoom);
                        }
                    }
                    // 우클릭은 컨텍스트 메뉴가 자동으로 처리
                });
            }
        }

        /**
         * 채팅방의 동적 표시 이름 생성
         */
        private String generateDisplayName(ChatRoom chatRoom) {
            List<Member> members = chatService.getChatRoomMembers(chatRoom.getChatRoomId());

            if (!chatRoom.isGroupChat() && members.size() == 2) {
                // 1:1 채팅 - 상대방 이름으로 표시
                Member otherMember = members.stream()
                        .filter(member -> member.getMemberId() != currentUser.getMemberId())
                        .findFirst()
                        .orElse(null);

                if (otherMember != null) {
                    return otherMember.getNickname() + "님과의 대화";
                }
            } else if (chatRoom.isGroupChat()) {
                // 그룹 채팅 이름 확인
                String originalName = chatRoom.getChatRoomName();

                // 자동 생성된 이름인지 확인 (끝에 "의 그룹채팅"이 있으면)
                if (originalName.endsWith("의 그룹채팅")) {
                    // 현재 멤버로 새로운 이름 생성
                    List<String> nicknames = members.stream()
                            .map(Member::getNickname)
                            .sorted()
                            .collect(java.util.stream.Collectors.toList());
                    return String.join(", ", nicknames) + "의 그룹채팅";
                } else {
                    // 사용자가 직접 설정한 이름은 그대로 표시
                    return originalName;
                }
            }

            // 기본값 - 원래 이름 그대로
            return chatRoom.getChatRoomName();
        }
    }

}