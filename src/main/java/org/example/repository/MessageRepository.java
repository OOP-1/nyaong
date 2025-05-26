// src/main/java/org/example/repository/MessageRepository.java
package org.example.repository;

import org.example.boundary.DatabaseConnector;
import org.example.model.Message;
import org.example.model.Member;
import org.example.socket.ChatMessage;
import org.example.socket.ChatMessageType;
import org.example.socket.ChatSocketClient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    /**
     * 메시지 전송 (저장)
     * 소켓을 통해 메시지를 전송하고 데이터베이스에도 저장합니다.
     * @param chatRoomId 채팅방 ID
     * @param senderId 발신자 ID
     * @param content 메시지 내용
     * @return 생성된 메시지 ID
     */
    public int sendMessage(int chatRoomId, int senderId, String content) {
        // 1. 소켓을 통해 메시지 전송
        ChatSocketClient socketClient = ChatSocketClient.getInstance();
        boolean sent = socketClient.sendMessage(chatRoomId, content);

        // 2. 데이터베이스에 메시지 저장
        String sql = "INSERT INTO Messages (chatroom_id, member_id, message_content, created_at) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, chatRoomId);
            pstmt.setInt(2, senderId);
            pstmt.setString(3, content);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;

        } catch (SQLException e) {
            System.err.println("메시지 전송 중 오류 발생: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 채팅방의 메시지 목록 조회
     * @param chatRoomId 채팅방 ID
     * @param limit 가져올 메시지 수 (최신순)
     * @param offset 건너뛸 메시지 수
     * @return 메시지 목록
     */
    public List<Message> getMessagesByChatRoomId(int chatRoomId, int limit, int offset) {
        String sql = "SELECT m.message_id, m.chatroom_id, m.member_id, m.message_content, m.created_at, " +
                "mem.user_id, mem.nickname, mem.status " +
                "FROM Messages m " +
                "JOIN Members mem ON m.member_id = mem.member_id " +
                "WHERE m.chatroom_id = ? " +
                "ORDER BY m.created_at DESC " +
                "LIMIT ? OFFSET ?";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatRoomId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Member sender = new Member(
                            rs.getInt("member_id"),
                            rs.getString("user_id"),
                            "", // 비밀번호는 보안상 가져오지 않음
                            rs.getString("nickname"),
                            rs.getString("status"),
                            "" // 역할은 필요 없음
                    );

                    Message message = new Message(
                            rs.getInt("message_id"),
                            rs.getInt("chatroom_id"),
                            rs.getInt("member_id"),
                            rs.getString("message_content"),
                            rs.getTimestamp("created_at"),
                            sender
                    );

                    messages.add(message);
                }
            }

        } catch (SQLException e) {
            System.err.println("메시지 목록 조회 중 오류 발생: " + e.getMessage());
        }

        return messages;
    }

    /**
     * 채팅방의 모든 메시지 개수 조회
     * @param chatRoomId 채팅방 ID
     * @return 메시지 개수
     */
    public int getMessageCount(int chatRoomId) {
        String sql = "SELECT COUNT(*) FROM Messages WHERE chatroom_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatRoomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("메시지 개수 조회 중 오류 발생: " + e.getMessage());
        }

        return 0;
    }

    /**
     * 특정 시간 이후의 새 메시지 조회 (소켓 통신으로 대체되므로 필요 시에만 사용)
     * @param chatRoomId 채팅방 ID
     * @param lastMessageTime 마지막으로 받은 메시지 시간
     * @return 새 메시지 목록
     */
    public List<Message> getNewMessages(int chatRoomId, Timestamp lastMessageTime) {
        String sql = "SELECT m.message_id, m.chatroom_id, m.member_id, m.message_content, m.created_at, " +
                "mem.user_id, mem.nickname, mem.status " +
                "FROM Messages m " +
                "JOIN Members mem ON m.member_id = mem.member_id " +
                "WHERE m.chatroom_id = ? AND m.created_at > ? " +
                "ORDER BY m.created_at ASC";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatRoomId);
            pstmt.setTimestamp(2, lastMessageTime);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Member sender = new Member(
                            rs.getInt("member_id"),
                            rs.getString("user_id"),
                            "", // 비밀번호는 보안상 가져오지 않음
                            rs.getString("nickname"),
                            rs.getString("status"),
                            "" // 역할은 필요 없음
                    );

                    Message message = new Message(
                            rs.getInt("message_id"),
                            rs.getInt("chatroom_id"),
                            rs.getInt("member_id"),
                            rs.getString("message_content"),
                            rs.getTimestamp("created_at"),
                            sender
                    );

                    messages.add(message);
                }
            }

        } catch (SQLException e) {
            System.err.println("새 메시지 조회 중 오류 발생: " + e.getMessage());
        }

        return messages;
    }

    /**
     * ChatMessage 객체를 Message 객체로 변환
     */
    public Message convertChatMessageToMessage(ChatMessage chatMessage) {
        // Member 객체 생성 (발신자 정보)
        Member sender = null;
        if (chatMessage.getSenderNickname() != null) {
            sender = new Member(
                    chatMessage.getSenderId(),
                    "", // 사용자 ID는 알 수 없음
                    "", // 비밀번호는 필요 없음
                    chatMessage.getSenderNickname(),
                    chatMessage.getSenderStatus() != null ? chatMessage.getSenderStatus() : "UNKNOWN",
                    "" // 역할은 필요 없음
            );
        }

        // Message 객체 생성
        return new Message(
                0, // 메시지 ID는 데이터베이스에서 할당됨
                chatMessage.getChatRoomId(),
                chatMessage.getSenderId(),
                chatMessage.getContent(),
                chatMessage.getTimestamp(),
                sender
        );
    }
}