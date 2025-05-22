package org.example.socket;

import org.example.model.Message;
import org.example.model.Member;
import org.example.repository.MemberRepository;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 채팅 소켓 서버 클래스
 * 사용자 연결과 메시지 브로드캐스팅을 처리합니다.
 */
public class ChatSocketServer {
    private static final int PORT = 9000;
    private static ChatSocketServer instance;
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService executorService;

    // 사용자 ID와 소켓 연결 매핑
    private final Map<Integer, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    // 채팅방 ID와 참여자 목록 매핑
    private final Map<Integer, List<Integer>> chatRoomMembers = new ConcurrentHashMap<>();

    private ChatSocketServer() {
        // 싱글톤 패턴
    }

    /**
     * 싱글톤 인스턴스 얻기
     */
    public static synchronized ChatSocketServer getInstance() {
        if (instance == null) {
            instance = new ChatSocketServer();
        }
        return instance;
    }

    /**
     * 서버 시작
     */
    public void start() {
        if (running) {
            System.out.println("서버가 이미 실행 중입니다.");
            return;
        }

        executorService = Executors.newCachedThreadPool();
        running = true;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("채팅 서버가 포트 " + PORT + "에서 시작되었습니다.");

            // 클라이언트 연결 수락 스레드
            executorService.submit(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClientConnection(clientSocket);
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("클라이언트 연결 수락 중 오류 발생: " + e.getMessage());
                        }
                    }
                }
            });

        } catch (IOException e) {
            System.err.println("서버 시작 중 오류 발생: " + e.getMessage());
            running = false;
        }
    }

    /**
     * 서버 종료
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("서버 소켓 닫기 오류: " + e.getMessage());
        }

        // 모든 클라이언트 연결 종료
        for (ClientHandler handler : clientHandlers.values()) {
            handler.close();
        }
        clientHandlers.clear();
        chatRoomMembers.clear();

        if (executorService != null) {
            executorService.shutdownNow();
        }
        System.out.println("채팅 서버가 종료되었습니다.");
    }

    /**
     * 클라이언트 연결 처리
     */
    private void handleClientConnection(Socket clientSocket) {
        executorService.submit(() -> {
            ObjectInputStream inputStream = null;
            ObjectOutputStream outputStream = null;

            try {
                System.out.println("새 클라이언트 연결 수락됨");

                // 중요: 출력 스트림을 먼저 생성하고 flush
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStream.flush(); // 헤더 먼저 전송

                // 그 다음 입력 스트림 생성
                inputStream = new ObjectInputStream(clientSocket.getInputStream());

                // 초기 인증 단계 (사용자 ID 수신)
                Object receivedId = inputStream.readObject();

                if (receivedId == null) {
                    System.err.println("클라이언트 인증 실패: 받은 ID가 null입니다");
                    outputStream.writeObject(false); // 인증 실패 응답
                    outputStream.flush();
                    clientSocket.close();
                    return;
                }

                if (!(receivedId instanceof Integer)) {
                    System.err.println("클라이언트 인증 실패: 잘못된 형식의 ID: " + receivedId.getClass().getName());
                    outputStream.writeObject(false); // 인증 실패 응답
                    outputStream.flush();
                    clientSocket.close();
                    return;
                }

                Integer memberId = (Integer) receivedId;
                System.out.println("사용자 ID: " + memberId + " 연결됨");

                // 인증 성공 응답
                outputStream.writeObject(true);
                outputStream.flush();

                // 인증 성공
                ClientHandler clientHandler = new ClientHandler(clientSocket, memberId, inputStream, outputStream);
                clientHandlers.put(memberId, clientHandler);
                executorService.submit(clientHandler);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("클라이언트 연결 처리 중 오류: " + e.getMessage());
                e.printStackTrace();
                try {
                    if (outputStream != null) {
                        try {
                            outputStream.writeObject(false); // 오류 발생 시 실패 응답
                            outputStream.flush();
                        } catch (Exception ex) {
                            // 무시
                        }
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    clientSocket.close();
                } catch (IOException ex) {
                    System.err.println("소켓 닫기 오류: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * 채팅방 멤버 목록 업데이트
     */
    public void updateChatRoomMembers(int chatRoomId, List<Integer> members) {
        chatRoomMembers.put(chatRoomId, new ArrayList<>(members));
    }

    /**
     * 채팅방에 메시지 브로드캐스팅
     */
    public void broadcastMessage(ChatMessage chatMessage) {
        int chatRoomId = chatMessage.getChatRoomId();
        List<Integer> members = chatRoomMembers.get(chatRoomId);

        if (members != null) {
            for (Integer memberId : members) {
                ClientHandler handler = clientHandlers.get(memberId);
                if (handler != null) {
                    handler.sendMessage(chatMessage);
                }
            }
        }
    }

    /**
     * 사용자가 채팅방에 입장함을 알림
     */
    public void notifyChatRoomJoin(int chatRoomId, int memberId) {
        List<Integer> members = chatRoomMembers.get(chatRoomId);
        if (members == null) {
            members = new ArrayList<>();
            chatRoomMembers.put(chatRoomId, members);
        }

        if (!members.contains(memberId)) {
            members.add(memberId);
        }

        // 입장 메시지 생성
        MemberRepository memberRepo = new MemberRepository();
        Member member = memberRepo.findById(memberId).orElse(null);
        String nickname = member != null ? member.getNickname() : "알 수 없는 사용자";

        ChatMessage joinMessage = new ChatMessage(
                ChatMessageType.SYSTEM,
                chatRoomId,
                -1, // 시스템 메시지는 송신자 ID가 -1
                nickname + "님이 입장했습니다.",
                new Timestamp(System.currentTimeMillis())
        );

        broadcastMessage(joinMessage);
    }

    /**
     * 사용자가 채팅방에서 퇴장함을 알림
     */
    public void notifyChatRoomLeave(int chatRoomId, int memberId) {
        List<Integer> members = chatRoomMembers.get(chatRoomId);
        if (members != null) {
            members.remove(Integer.valueOf(memberId));

            // 채팅방에 아무도 없으면 목록에서 제거
            if (members.isEmpty()) {
                chatRoomMembers.remove(chatRoomId);
                return;
            }

            // 퇴장 메시지 생성
            MemberRepository memberRepo = new MemberRepository();
            Member member = memberRepo.findById(memberId).orElse(null);
            String nickname = member != null ? member.getNickname() : "알 수 없는 사용자";

            ChatMessage leaveMessage = new ChatMessage(
                    ChatMessageType.SYSTEM,
                    chatRoomId,
                    -1, // 시스템 메시지는 송신자 ID가 -1
                    nickname + "님이 퇴장했습니다.",
                    new Timestamp(System.currentTimeMillis())
            );

            broadcastMessage(leaveMessage);
        }
    }

    /**
     * 클라이언트 핸들러 클래스
     * 각 클라이언트 연결을 처리합니다.
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int memberId;
        private final ObjectInputStream inputStream;
        private final ObjectOutputStream outputStream;
        private boolean running = true;

        public ClientHandler(Socket socket, int memberId, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
            this.socket = socket;
            this.memberId = memberId;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                // 클라이언트가 연결되어 있는 동안 메시지 대기
                while (running && !socket.isClosed()) {
                    try {
                        // 클라이언트로부터 메시지 수신
                        Object message = inputStream.readObject();
                        System.out.println("클라이언트로부터 메시지 수신: " +
                                (message != null ? message.getClass().getSimpleName() : "null"));

                        if (message instanceof ChatMessage) {
                            handleChatMessage((ChatMessage) message);
                        } else if (message instanceof ChatCommand) {
                            handleChatCommand((ChatCommand) message);
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("알 수 없는 메시지 타입: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("클라이언트 " + memberId + " 처리 중 오류: " + e.getMessage());
                }
            } finally {
                close();
            }
        }

        /**
         * 채팅 메시지 처리
         */
        private void handleChatMessage(ChatMessage message) {
            // 채팅방에 메시지 브로드캐스팅
            broadcastMessage(message);
        }

        /**
         * 채팅 명령 처리 (개선된 버전)
         */
        private void handleChatCommand(ChatCommand command) {
            System.out.println("채팅 명령 수신: " + command.getType() + ", chatRoomId=" + command.getChatRoomId());

            try {
                switch (command.getType()) {
                    case JOIN_CHAT:
                        System.out.println("JOIN_CHAT 처리 시작 - 멤버ID: " + memberId + ", 채팅방ID: " + command.getChatRoomId());
                        notifyChatRoomJoin(command.getChatRoomId(), memberId);
                        // 성공 응답 전송
                        sendCommandResponse(new ChatCommand(ChatCommandType.JOIN_CHAT_SUCCESS, command.getChatRoomId(), memberId));
                        System.out.println("JOIN_CHAT 처리 완료");
                        break;

                    case LEAVE_CHAT:
                        System.out.println("LEAVE_CHAT 처리 시작");
                        notifyChatRoomLeave(command.getChatRoomId(), memberId);
                        // 성공 응답 전송
                        sendCommandResponse(new ChatCommand(ChatCommandType.LEAVE_CHAT_SUCCESS, command.getChatRoomId(), memberId));
                        System.out.println("LEAVE_CHAT 처리 완료");
                        break;

                    case UPDATE_MEMBERS:
                        System.out.println("UPDATE_MEMBERS 처리 시작");
                        updateChatRoomMembers(command.getChatRoomId(), command.getMembers());
                        System.out.println("UPDATE_MEMBERS 처리 완료");
                        break;
                }
            } catch (Exception e) {
                System.err.println("명령 처리 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                // 실패 응답 전송
                sendCommandResponse(new ChatCommand(ChatCommandType.ERROR, command.getChatRoomId(), memberId));
            }
        }

        /**
         * 명령 응답 전송
         */
        private void sendCommandResponse(ChatCommand response) {
            try {
                if (outputStream != null && running) {
                    outputStream.writeObject(response);
                    outputStream.flush();
                    outputStream.reset();
                }
            } catch (IOException e) {
                System.err.println("명령 응답 전송 오류: " + e.getMessage());
            }
        }

        /**
         * 클라이언트에 메시지 전송
         */
        public void sendMessage(ChatMessage message) {
            try {
                if (outputStream != null && running) {
                    outputStream.writeObject(message);
                    outputStream.flush();
                    outputStream.reset(); // 캐시 초기화 (중요!)
                }
            } catch (IOException e) {
                System.err.println("메시지 전송 오류 (사용자 " + memberId + "): " + e.getMessage());
                close();
            }
        }

        /**
         * 연결 종료
         */
        public void close() {
            running = false;
            clientHandlers.remove(memberId, this);

            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("소켓 종료 오류: " + e.getMessage());
            }
        }
    }
}