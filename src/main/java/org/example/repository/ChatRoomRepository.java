// src/main/java/org/example/repository/ChatRoomRepository.java
package org.example.repository;

import org.example.boundary.DatabaseConnector;
import org.example.model.ChatRoom;
import org.example.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatRoomRepository {

    /**
     * 채팅방 생성
     * @param chatRoomName 채팅방 이름
     * @param isGroupChat 그룹 채팅 여부
     * @param memberIds 참여자 ID 목록
     * @return 생성된 채팅방 ID
     */
    public int createChatRoom(String chatRoomName, boolean isGroupChat, List<Integer> memberIds) {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // 1. 채팅방 생성
            String chatRoomSql = "INSERT INTO ChatRooms (chatroom_name, is_group_chat, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
            int chatRoomId;

            try (PreparedStatement pstmt = conn.prepareStatement(chatRoomSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, chatRoomName);
                pstmt.setBoolean(2, isGroupChat);
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        chatRoomId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("채팅방 생성 실패, ID를 가져올 수 없습니다.");
                    }
                }
            }

            // 2. 참여자 추가
            String memberSql = "INSERT INTO ChatRoomMembers (chatroom_id, member_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(memberSql)) {
                for (Integer memberId : memberIds) {
                    pstmt.setInt(1, chatRoomId);
                    pstmt.setInt(2, memberId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
            return chatRoomId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("롤백 실패: " + ex.getMessage());
                }
            }
            System.err.println("채팅방 생성 중 오류 발생: " + e.getMessage());
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("연결 닫기 실패: " + e.getMessage());
                }
            }
        }
    }

    // ChatRoomRepository.java의 getChatRoomsByMemberId 메서드 수정

    /**
     * 사용자가 참여 중인 채팅방 목록 조회 - 통합 최신순 정렬
     * @param memberId 사용자 ID
     * @return 채팅방 목록 (최근 활동순)
     */
    public List<ChatRoom> getChatRoomsByMemberId(int memberId) {
        // 채팅방 생성시간과 메시지 시간을 통합해서 가장 최근 활동 기준으로 정렬
        String sql = "SELECT cr.chatroom_id, cr.chatroom_name, cr.is_group_chat, cr.created_at, " +
                "MAX(m.created_at) as last_message_time, " +
                "(SELECT m2.message_content FROM Messages m2 WHERE m2.chatroom_id = cr.chatroom_id ORDER BY m2.created_at DESC LIMIT 1) as last_message, " +
                // 최근 활동 시간 = 메시지 시간이 있으면 메시지 시간, 없으면 채팅방 생성 시간
                "CASE WHEN MAX(m.created_at) IS NOT NULL THEN MAX(m.created_at) ELSE cr.created_at END as last_activity " +
                "FROM ChatRooms cr " +
                "JOIN ChatRoomMembers crm ON cr.chatroom_id = crm.chatroom_id " +
                "LEFT JOIN Messages m ON cr.chatroom_id = m.chatroom_id " +
                "WHERE crm.member_id = ? " +
                "GROUP BY cr.chatroom_id " +
                // 최근 활동 시간 기준으로 내림차순 정렬 (가장 최근이 맨 위)
                "ORDER BY last_activity DESC";

        List<ChatRoom> chatRooms = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int chatRoomId = rs.getInt("chatroom_id");

                    // 채팅방 기본 정보
                    ChatRoom chatRoom = new ChatRoom(
                            chatRoomId,
                            rs.getString("chatroom_name"),
                            rs.getBoolean("is_group_chat"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("last_message_time"),
                            rs.getString("last_message")
                    );

                    // 채팅방 참여자 목록 조회
                    chatRoom.setMembers(getChatRoomMembers(chatRoomId));

                    chatRooms.add(chatRoom);
                }
            }

        } catch (SQLException e) {
            System.err.println("채팅방 목록 조회 중 오류 발생: " + e.getMessage());
        }

        return chatRooms;
    }

    /**
     * 채팅방 정보 조회
     * @param chatRoomId 채팅방 ID
     * @return 채팅방 정보
     */
    public Optional<ChatRoom> getChatRoomById(int chatRoomId) {
        String sql = "SELECT cr.chatroom_id, cr.chatroom_name, cr.is_group_chat, cr.created_at, " +
                "MAX(m.created_at) as last_message_time, " +
                "(SELECT m2.message_content FROM Messages m2 WHERE m2.chatroom_id = cr.chatroom_id ORDER BY m2.created_at DESC LIMIT 1) as last_message " +
                "FROM ChatRooms cr " +
                "LEFT JOIN Messages m ON cr.chatroom_id = m.chatroom_id " +
                "WHERE cr.chatroom_id = ? " +
                "GROUP BY cr.chatroom_id";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatRoomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ChatRoom chatRoom = new ChatRoom(
                            rs.getInt("chatroom_id"),
                            rs.getString("chatroom_name"),
                            rs.getBoolean("is_group_chat"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("last_message_time"),
                            rs.getString("last_message")
                    );

                    // 채팅방 참여자 목록 조회
                    chatRoom.setMembers(getChatRoomMembers(chatRoomId));

                    return Optional.of(chatRoom);
                }
            }

        } catch (SQLException e) {
            System.err.println("채팅방 정보 조회 중 오류 발생: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * 채팅방 참여자 목록 조회
     * @param chatRoomId 채팅방 ID
     * @return 참여자 목록
     */
    public List<Member> getChatRoomMembers(int chatRoomId) {
        String sql = "SELECT m.member_id, m.user_id, m.nickname, m.status, m.role " +
                "FROM Members m " +
                "JOIN ChatRoomMembers crm ON m.member_id = crm.member_id " +
                "WHERE crm.chatroom_id = ?";

        List<Member> members = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatRoomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Member member = new Member(
                            rs.getInt("member_id"),
                            rs.getString("user_id"),
                            "", // 비밀번호는 보안상 가져오지 않음
                            rs.getString("nickname"),
                            rs.getString("status"),
                            rs.getString("role")
                    );
                    members.add(member);
                }
            }

        } catch (SQLException e) {
            System.err.println("채팅방 참여자 목록 조회 중 오류 발생: " + e.getMessage());
        }

        return members;
    }

    /**
     * 채팅방 이름 변경
     * @param chatRoomId 채팅방 ID
     * @param newName 새 채팅방 이름
     * @return 변경 성공 여부
     */
    public boolean updateChatRoomName(int chatRoomId, String newName) {
        String sql = "UPDATE ChatRooms SET chatroom_name = ? WHERE chatroom_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setInt(2, chatRoomId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("채팅방 이름 변경 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 채팅방에 참여자 추가
     * @param chatRoomId 채팅방 ID
     * @param memberId 추가할 참여자 ID
     * @return 추가 성공 여부
     */
    public boolean addChatRoomMember(int chatRoomId, int memberId) {
        String sql = "INSERT INTO ChatRoomMembers (chatroom_id, member_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatRoomId);
            pstmt.setInt(2, memberId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("채팅방 참여자 추가 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 채팅방에서 참여자 제거
     * @param chatRoomId 채팅방 ID
     * @param memberId 제거할 참여자 ID
     * @return 제거 성공 여부
     */
    public boolean removeChatRoomMember(int chatRoomId, int memberId) {
        String sql = "DELETE FROM ChatRoomMembers WHERE chatroom_id = ? AND member_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, chatRoomId);
            pstmt.setInt(2, memberId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("채팅방 참여자 제거 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 채팅방 나가기 (참여자 제거)
     * @param chatRoomId 채팅방 ID
     * @param memberId 나가는 참여자 ID
     * @return 나가기 성공 여부
     */
    public boolean leaveChatRoom(int chatRoomId, int memberId) {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // 1. 참여자 제거
            boolean removed = removeChatRoomMember(chatRoomId, memberId);
            if (!removed) {
                return false;
            }

            // 2. 참여자가 없으면 채팅방 삭제
            String countSql = "SELECT COUNT(*) FROM ChatRoomMembers WHERE chatroom_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
                pstmt.setInt(1, chatRoomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        String deleteSql = "DELETE FROM ChatRooms WHERE chatroom_id = ?";
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, chatRoomId);
                            deleteStmt.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("롤백 실패: " + ex.getMessage());
                }
            }
            System.err.println("채팅방 나가기 중 오류 발생: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("연결 닫기 실패: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 1:1 채팅방 조회 또는 생성
     * @param currentMemberId 현재 사용자 ID
     * @param targetMemberId 대화 상대 ID
     * @return 채팅방 ID
     */
    public int getOrCreatePrivateChatRoom(int currentMemberId, int targetMemberId) {
        // 1. 이미 존재하는 1:1 채팅방 찾기
        String findSql = "SELECT cr.chatroom_id " +
                "FROM ChatRooms cr " +
                "JOIN ChatRoomMembers crm1 ON cr.chatroom_id = crm1.chatroom_id " +
                "JOIN ChatRoomMembers crm2 ON cr.chatroom_id = crm2.chatroom_id " +
                "WHERE cr.is_group_chat = false " +
                "AND crm1.member_id = ? AND crm2.member_id = ? " +
                "AND (SELECT COUNT(*) FROM ChatRoomMembers WHERE chatroom_id = cr.chatroom_id) = 2";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(findSql)) {

            pstmt.setInt(1, currentMemberId);
            pstmt.setInt(2, targetMemberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("chatroom_id");
                }
            }

            // 2. 상대방 정보 조회하여 채팅방 이름에 사용
            MemberRepository memberRepo = new MemberRepository();
            Optional<Member> targetMember = memberRepo.findById(targetMemberId);

            // 채팅방 이름은 "1:1_멤버ID1_멤버ID2" 형식으로 저장 (ID 기준 오름차순 정렬)
            int smallerId = Math.min(currentMemberId, targetMemberId);
            int largerId = Math.max(currentMemberId, targetMemberId);
            String chatRoomName = "1:1_" + smallerId + "_" + largerId;

            // 3. 채팅방이 없으면 새로 생성
            List<Integer> members = new ArrayList<>();
            members.add(currentMemberId);
            members.add(targetMemberId);

            return createChatRoom(chatRoomName, false, members);

        } catch (SQLException e) {
            System.err.println("1:1 채팅방 조회/생성 중 오류 발생: " + e.getMessage());
            return -1;
        }
    }
}