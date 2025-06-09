package org.example.socket;

import org.example.config.EnvLoader;
import org.example.model.Member;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 채팅 소켓 클라이언트 클래스
 * 서버와의 소켓 통신을 관리합니다.
 */
public class ChatSocketClient {
    private static final String SERVER_HOST = EnvLoader.get("SERVER_HOST");
    private static final int SERVER_PORT = Integer.parseInt(EnvLoader.get("SERVER_PORT"));
    private static ChatSocketClient instance;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ExecutorService executorService;
    private boolean connected = false;
    private Member currentUser;

    // 채팅방별 메시지 수신 리스너
    private final Map<Integer, List<Consumer<ChatMessage>>> messageListeners = new HashMap<>();
    // 명령 수신 리스너
    private final List<Consumer<ChatCommand>> commandListeners = new ArrayList<>();

    private ChatSocketClient() {
        // 싱글톤 패턴
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * 싱글톤 인스턴스 얻기
     */
    public static synchronized ChatSocketClient getInstance() {
        if (instance == null) {
            instance = new ChatSocketClient();
        }
        return instance;
    }

    /**
     * 서버에 연결 및 인증
     */
    public boolean connect(Member user) {
        if (connected && socket != null && socket.isConnected() && !socket.isClosed()) {
            System.out.println("이미 연결되어 있습니다.");
            return true;
        }

        this.currentUser = user;
        connected = false; // 연결 시작 전에 반드시 false로 설정

        try {
            System.out.println("서버 연결 시도: " + SERVER_HOST + ":" + SERVER_PORT);

            // 기존 리소스 정리
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    System.err.println("이전 소켓 닫기 실패: " + e.getMessage());
                }
            }

            // 새 소켓 연결
            socket = new Socket(SERVER_HOST, SERVER_PORT);

            // 중요: 출력 스트림을 먼저 생성하고 flush
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush(); // 헤더 먼저 전송

            // 그 다음 입력 스트림 생성
            inputStream = new ObjectInputStream(socket.getInputStream());

            // 사용자 ID로 인증 (첫 메시지로 전송)
            System.out.println("인증 시도: 멤버ID=" + user.getMemberId());
            outputStream.writeObject(user.getMemberId());
            outputStream.flush();

            // 인증 응답 확인 (서버 측 수정 필요)
            try {
                Object response = inputStream.readObject();
                System.out.println("인증 응답: " + response);

                if (!(response instanceof Boolean) || !((Boolean)response)) {
                    System.err.println("인증 실패: 서버가 거부했습니다");
                    disconnect();
                    return false;
                }
            } catch (ClassNotFoundException e) {
                System.err.println("인증 응답 처리 오류: " + e.getMessage());
                disconnect();
                return false;
            }

            connected = true;
            System.out.println("서버 연결 및 인증 성공");

            // 메시지 수신 스레드 시작
            startMessageReceiver();

            return true;
        } catch (IOException e) {
            System.err.println("서버 연결 실패: " + e.getMessage());
            e.printStackTrace();
            disconnect();
            return false;
        }
    }

    /**
     * 서버 연결 종료
     */
    public void disconnect() {
        connected = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            System.err.println("연결 종료 오류: " + e.getMessage());
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            executorService = Executors.newCachedThreadPool();
        }
    }

    /**
     * 메시지 수신 스레드 시작
     */
    private void startMessageReceiver() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newCachedThreadPool();
        }

        executorService.submit(() -> {
            System.out.println("메시지 수신 스레드 시작");
            try {
                while (connected && socket != null && !socket.isClosed()) {
                    try {
                        Object received = inputStream.readObject();
                        if (received == null) {
                            System.out.println("수신된 객체가 null입니다.");
                            continue;
                        }

                        System.out.println("메시지 수신: " + received.getClass().getSimpleName());

                        if (received instanceof ChatMessage) {
                            ChatMessage message = (ChatMessage) received;
                            handleIncomingMessage(message);
                        } else if (received instanceof ChatCommand) {
                            ChatCommand command = (ChatCommand) received;
                            handleIncomingCommand(command);
                        } else {
                            System.out.println("알 수 없는 메시지 유형: " + received.getClass().getName());
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("알 수 없는 객체 유형: " + e.getMessage());
                    } catch (java.net.SocketTimeoutException e) {
                        // 타임아웃은 정상적인 것이므로 무시하고 계속 진행
                        System.out.println("소켓 타임아웃 (정상): " + e.getMessage());
                        continue;
                    } catch (java.io.EOFException | java.net.SocketException e) {
                        // 연결이 끊어진 경우
                        System.err.println("서버 연결이 끊어짐: " + e.getMessage());
                        break;
                    } catch (Exception e) {
                        System.err.println("메시지 수신 중 예외 발생: " + e.getMessage());
                        e.printStackTrace();
                        // 치명적이지 않은 예외인 경우 계속 진행
                        if (e instanceof IOException) {
                            break; // IO 예외는 연결 문제이므로 종료
                        }
                    }
                }
            } catch (Exception e) {
                if (connected) {
                    System.err.println("메시지 수신 스레드에서 예외 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            } finally {
                System.out.println("메시지 수신 스레드 종료");
                disconnect();
            }
        });
    }

    /**
     * 수신된 메시지 처리
     */
    private void handleIncomingMessage(ChatMessage message) {
        List<Consumer<ChatMessage>> listeners = messageListeners.get(message.getChatRoomId());

        if (listeners != null) {
            for (Consumer<ChatMessage> listener : listeners) {
                // JavaFX 스레드에서 UI 업데이트 처리를 위해 나중에 Platform.runLater로 래핑 필요
                listener.accept(message);
            }
        }
    }

    /**
     * 수신된 명령 처리
     */
    private void handleIncomingCommand(ChatCommand command) {
        for (Consumer<ChatCommand> listener : commandListeners) {
            // JavaFX 스레드에서 UI 업데이트 처리를 위해 나중에 Platform.runLater로 래핑 필요
            listener.accept(command);
        }
    }

    /**
     * 채팅방에 메시지 전송
     */
    public boolean sendMessage(int chatRoomId, String content) {
        if (!connected) {
            return false;
        }

        try {
            ChatMessage message = new ChatMessage(
                    ChatMessageType.CHAT,
                    chatRoomId,
                    currentUser.getMemberId(),
                    content,
                    new Timestamp(System.currentTimeMillis()),
                    currentUser.getNickname(),
                    currentUser.getStatus()
            );

            outputStream.writeObject(message);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            System.err.println("메시지 전송 오류: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * 명령 전송
     */
    public boolean sendCommand(ChatCommand command) {
        if (!isConnected()) {
            System.err.println("sendCommand: 소켓이 연결되어 있지 않습니다.");
            return false;
        }

        try {
            System.out.println("명령 전송 중: " + command.getType() + ", chatRoomId=" + command.getChatRoomId());
            outputStream.writeObject(command);
            outputStream.flush();
            outputStream.reset(); // 객체 캐시 초기화 (중요!)
            System.out.println("명령 전송 성공");
            return true;
        } catch (IOException e) {
            System.err.println("명령 전송 오류: " + e.getMessage());
            e.printStackTrace();
            disconnect();
            return false;
        }
    }

    // 예: ChatSocketClient.java의 연결 확인 부분
    public boolean ensureConnected(Member user) {
        System.out.println("ensureConnected 호출: " + (user != null ? "memberId=" + user.getMemberId() : "user=null"));
        if (isConnected()) {
            System.out.println("이미 연결 상태임");
            return true;
        }

        System.out.println("소켓 연결이 끊어져 있어 재연결 시도...");
        return connect(user);
    }

    /**
     * 채팅방 입장 명령 전송 (연결 상태 확인 추가)
     * 성공/실패 여부를 명확히 반환
     */
    public boolean joinChatRoom(int chatRoomId) {
        // 연결 상태 확인 및 재연결 시도
        if (!isConnected() && currentUser != null) {
            boolean reconnected = connect(currentUser);
            if (!reconnected) {
                System.err.println("채팅방 입장 전 소켓 재연결 실패");
                return false;
            }
        }

        try {
            ChatCommand command = new ChatCommand(ChatCommandType.JOIN_CHAT, chatRoomId, currentUser.getMemberId());
            boolean sent = sendCommand(command);

            if (!sent) {
                System.err.println("채팅방 입장 명령 전송 실패");
                return false;
            }

            System.out.println("채팅방 " + chatRoomId + " 입장 명령 전송 성공");
            return true;
        } catch (Exception e) {
            System.err.println("채팅방 입장 명령 전송 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 채팅방 퇴장 명령 전송 (개선된 버전)
     * @param chatRoomId 채팅방 ID
     * @return 전송 성공 여부
     */
    public boolean leaveChatRoom(int chatRoomId) {
        if (!isConnected()) {
            System.err.println("leaveChatRoom: 소켓이 연결되어 있지 않습니다.");
            return false;
        }

        try {
            ChatCommand command = new ChatCommand(ChatCommandType.LEAVE_CHAT, chatRoomId, currentUser.getMemberId());
            boolean sent = sendCommand(command);

            if (sent) {
                System.out.println("채팅방 " + chatRoomId + " 퇴장 명령 전송 성공");

                // 해당 채팅방의 모든 메시지 리스너 제거
                clearMessageListeners(chatRoomId);

                return true;
            } else {
                System.err.println("채팅방 퇴장 명령 전송 실패");
                return false;
            }
        } catch (Exception e) {
            System.err.println("채팅방 퇴장 명령 전송 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 채팅방 멤버 목록 업데이트 명령 전송
     */
    public void updateChatRoomMembers(int chatRoomId, List<Integer> members) {
        ChatCommand command = new ChatCommand(ChatCommandType.UPDATE_MEMBERS, chatRoomId, members);
        sendCommand(command);
    }

    /**
     * 메시지 리스너 추가
     */
    public void addMessageListener(int chatRoomId, Consumer<ChatMessage> listener) {
        messageListeners.computeIfAbsent(chatRoomId, k -> new ArrayList<>()).add(listener);
    }

    /**
     * 메시지 리스너 제거
     */
    public void removeMessageListener(int chatRoomId, Consumer<ChatMessage> listener) {
        List<Consumer<ChatMessage>> listeners = messageListeners.get(chatRoomId);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                messageListeners.remove(chatRoomId);
            }
        }
    }

    /**
     * 특정 채팅방의 모든 메시지 리스너 제거
     */
    public void clearMessageListeners(int chatRoomId) {
        messageListeners.remove(chatRoomId);
    }

    /**
     * 명령 리스너 추가
     */
    public void addCommandListener(Consumer<ChatCommand> listener) {
        commandListeners.add(listener);
    }

    /**
     * 명령 리스너 제거
     */
    public void removeCommandListener(Consumer<ChatCommand> listener) {
        commandListeners.remove(listener);
    }

    /**
     * 모든 명령 리스너 제거
     */
    public void clearCommandListeners() {
        commandListeners.clear();
    }

    /**
     * 연결 상태 확인
     */
    public boolean isConnected() {
        boolean result = connected && socket != null && socket.isConnected() && !socket.isClosed();

        if (!result && connected) {
            // 연결 상태 플래그는 true이지만 소켓 상태가 좋지 않음
            System.out.println("연결 상태 불일치 감지: connected 플래그는 true이지만 실제 소켓은 연결되지 않음");
            connected = false; // 상태 일치 시키기
        }

        return result;
    }
}