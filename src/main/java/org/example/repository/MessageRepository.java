package org.example.repository;

import org.example.boundary.DatabaseConnector;
import org.example.model.Member;
import org.example.model.Message;
import org.example.socket.ChatMessage;
import org.example.socket.ChatMessageType;
import org.example.socket.ChatSocketClient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    public int sendMessage(int chatRoomId, int blockchainMessageId, int senderId, String content) {
        String sql = "INSERT INTO Messages (chatroom_id, blockchain_message_id, member_id, message_content, created_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        boolean sent = socketClient.sendMessage(chatRoomId, content);

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, chatRoomId);
            pstmt.setInt(2, blockchainMessageId);
            pstmt.setInt(3, senderId);
            pstmt.setString(4, content);

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
     */
    public List<Message> getMessagesByChatRoomId(int chatRoomId, int limit, int offset) {
        String sql = "SELECT m.message_id, m.blockchain_message_id, m.chatroom_id, m.member_id, m.message_content, m.created_at, " +
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
                    messages.add(mapResultSetToMessage(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("메시지 목록 조회 중 오류 발생: " + e.getMessage());
        }

        return messages;
    }

    /**
     * 채팅방의 모든 메시지 개수 조회
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

    public List<Message> getNewMessages(int chatRoomId, Timestamp lastMessageTime) {
        String sql = "SELECT m.message_id, m.blockchain_message_id, m.chatroom_id, m.member_id, m.message_content, m.created_at, " +
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
                    messages.add(mapResultSetToMessage(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("새 메시지 조회 중 오류 발생: " + e.getMessage());
        }

        return messages;
    }

    public List<Message> searchMessages(Integer chatRoomId, Integer senderId, Timestamp from, Timestamp to) {
        StringBuilder sql = new StringBuilder(
                "SELECT m.message_id, m.blockchain_message_id, m.chatroom_id, m.member_id, m.message_content, m.created_at, " +
                        "mem.user_id, mem.nickname, mem.status " +
                        "FROM Messages m " +
                        "JOIN Members mem ON m.member_id = mem.member_id " +
                        "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (chatRoomId != null) {
            sql.append(" AND m.chatroom_id = ?");
            params.add(chatRoomId);
        }

        if (senderId != null) {
            sql.append(" AND m.member_id = ?");
            params.add(senderId);
        }

        if (from != null) {
            sql.append(" AND m.created_at >= ?");
            params.add(from);
        }

        if (to != null) {
            sql.append(" AND m.created_at <= ?");
            params.add(to);
        }

        sql.append(" ORDER BY m.created_at ASC");

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("메시지 로그 검색 중 오류 발생: " + e.getMessage());
        }

        return messages;
    }

    /**
     * ResultSet → Message 객체 변환
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Member sender = new Member(
                rs.getInt("member_id"),
                rs.getString("user_id"),
                "", // password 제외
                rs.getString("nickname"),
                rs.getString("status"),
                ""
        );

        return new Message(
                rs.getInt("message_id"),
                rs.getInt("blockchain_message_id"),
                rs.getInt("chatroom_id"),
                rs.getInt("member_id"),
                rs.getString("message_content"),
                rs.getTimestamp("created_at"),
                sender
        );
    }

    public void updateBlockchainMessageId(int msgId, int blockchainMessageId) {
        String sql = "UPDATE Messages SET blockchain_message_id = ? " +
                "WHERE message_id = ? " +
                "ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setInt(1, blockchainMessageId);
            pstmt.setInt(2, msgId);

            int updatedRows = pstmt.executeUpdate();
            if (updatedRows == 0) {
                System.err.println("[" + msgId + "] ❗ 블록체인 ID 업데이트 실패: 해당 메시지를 찾을 수 없음");
            } else {
                System.out.println("[" + msgId + "] ✅ 블록체인 ID 업데이트 완료");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

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
