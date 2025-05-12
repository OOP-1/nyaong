// src/main/java/org/example/repository/MessageRepository.java
package org.example.repository;

import org.example.boundary.DatabaseConnector;
import org.example.model.Message;
import org.example.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    /**
     * 메시지 전송 (저장)
     * @param chatRoomId 채팅방 ID
     * @param senderId 발신자 ID
     * @param content 메시지 내용
     * @return 생성된 메시지 ID
     */
    public int sendMessage(int chatRoomId, int senderId, String content) {
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
     * 특정 시간 이후의 새 메시지 조회 (채팅 자동 업데이트용)
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
}