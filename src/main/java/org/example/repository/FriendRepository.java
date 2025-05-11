package org.example.repository;

import org.example.boundary.DatabaseConnector;
import org.example.model.Friend;
import org.example.model.FriendRequest;
import org.example.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 친구 관계를 데이터베이스에 저장하고 관리하는 Repository 클래스
 */
public class FriendRepository {

    /**
     * 친구 추가 (양방향 친구 관계 생성)
     * @param memberId 사용자 ID
     * @param friendId 친구 ID
     * @return 추가 성공 여부
     */
    public boolean addFriend(int memberId, int friendId) {
        // 자기 자신을 친구로 추가할 수 없음
        if (memberId == friendId) {
            return false;
        }

        // 이미 친구 관계인지 확인
        if (isFriend(memberId, friendId)) {
            return false;
        }

        String sql = "INSERT INTO Friends (member_id, friend_id) VALUES (?, ?), (?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 양방향 친구 관계 생성 (A->B, B->A)
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId); // 반대 방향도 추가
            pstmt.setInt(4, memberId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("친구 추가 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 친구 삭제 (양방향 삭제)
     * @param memberId 사용자 ID
     * @param friendId 친구 ID
     * @return 삭제 성공 여부
     */
    public boolean removeFriend(int memberId, int friendId) {
        String sql = "DELETE FROM Friends WHERE (member_id = ? AND friend_id = ?) OR (member_id = ? AND friend_id = ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 양방향 친구 관계 모두 삭제
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, memberId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("친구 삭제 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 특정 사용자의 친구 목록 조회
     * @param memberId 사용자 ID
     * @return 친구 목록 (친구의 상세 정보 포함)
     */
    public List<Friend> getFriendsByMemberId(int memberId) {
        String sql = "SELECT f.friend_id, m.member_id, m.user_id, m.nickname, m.status, m.role " +
                "FROM Friends f " +
                "JOIN Members m ON f.friend_id = m.member_id " +
                "WHERE f.member_id = ?";

        List<Friend> friends = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Member friendInfo = new Member(
                            rs.getInt("member_id"),
                            rs.getString("user_id"),
                            "", // 비밀번호는 보안상 가져오지 않음
                            rs.getString("nickname"),
                            rs.getString("status"),
                            rs.getString("role")
                    );

                    Friend friend = new Friend(memberId, rs.getInt("friend_id"), friendInfo);
                    friends.add(friend);
                }
            }

        } catch (SQLException e) {
            System.err.println("친구 목록 조회 중 오류 발생: " + e.getMessage());
        }

        return friends;
    }

    /**
     * 친구 관계인지 확인
     * @param memberId 사용자 ID
     * @param friendId 친구 ID
     * @return 친구 관계 여부
     */
    public boolean isFriend(int memberId, int friendId) {
        String sql = "SELECT COUNT(*) FROM Friends WHERE member_id = ? AND friend_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setInt(2, friendId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("친구 관계 확인 중 오류 발생: " + e.getMessage());
        }

        return false;
    }

    /**
     * 사용자 ID로 검색 (친구 추가를 위한 검색)
     * @param currentMemberId 현재 사용자 ID (검색 결과에서 본인 제외)
     * @param userId 검색할 사용자 ID
     * @return 검색 결과 목록
     */
    public List<Member> searchMembersByUserId(int currentMemberId, String userId) {
        String sql = "SELECT * FROM Members " +
                "WHERE member_id != ? AND user_id LIKE ?";

        List<Member> results = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentMemberId);
            pstmt.setString(2, "%" + userId + "%");

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
                    results.add(member);
                }
            }

        } catch (SQLException e) {
            System.err.println("사용자 검색 중 오류 발생: " + e.getMessage());
        }

        return results;
    }

    /**
     * 친구 요청 생성 (소개 메시지 포함)
     * @param senderId 요청자 ID
     * @param receiverId 수신자 ID
     * @param message 소개 메시지
     * @return 요청 성공 여부
     */
    public boolean createFriendRequest(int senderId, int receiverId, String message) {
        // 자기 자신에게 요청할 수 없음
        if (senderId == receiverId) {
            return false;
        }

        // 이미 친구인 경우
        if (isFriend(senderId, receiverId)) {
            return false;
        }

        // 이미 친구 요청이 존재하는지 확인
        if (existsFriendRequest(senderId, receiverId)) {
            return false;
        }

        String sql = "INSERT INTO FriendRequests (sender_id, receiver_id, message, status, created_at) " +
                "VALUES (?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, message);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("친구 요청 생성 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 특정 사용자에게 온 친구 요청 목록 조회
     * @param receiverId 수신자 ID
     * @return 친구 요청 목록 (요청자 정보 포함)
     */
    public List<FriendRequest> getPendingFriendRequests(int receiverId) {
        String sql = "SELECT fr.request_id, fr.sender_id, fr.receiver_id, fr.message, fr.status, fr.created_at, " +
                "m.user_id, m.nickname, m.status as member_status " +
                "FROM FriendRequests fr " +
                "JOIN Members m ON fr.sender_id = m.member_id " +
                "WHERE fr.receiver_id = ? AND fr.status = 'PENDING'";

        List<FriendRequest> requests = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, receiverId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Member senderInfo = new Member(
                            rs.getInt("sender_id"),
                            rs.getString("user_id"),
                            "", // 비밀번호는 보안상 가져오지 않음
                            rs.getString("nickname"),
                            rs.getString("member_status"),
                            ""  // 역할은 필요 없음
                    );

                    FriendRequest request = new FriendRequest(
                            rs.getInt("request_id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("message"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at"),
                            senderInfo
                    );

                    requests.add(request);
                }
            }

        } catch (SQLException e) {
            System.err.println("친구 요청 조회 중 오류 발생: " + e.getMessage());
        }

        return requests;
    }

    /**
     * 친구 요청 수락
     * @param requestId 요청 ID
     * @return 수락 성공 여부
     */
    public boolean acceptFriendRequest(int requestId) {
        // 트랜잭션 시작
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // 1. 요청 정보 조회
            String selectSql = "SELECT sender_id, receiver_id FROM FriendRequests WHERE request_id = ? AND status = 'PENDING'";
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, requestId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int senderId = rs.getInt("sender_id");
                        int receiverId = rs.getInt("receiver_id");

                        // 2. 요청 상태 업데이트
                        String updateSql = "UPDATE FriendRequests SET status = 'ACCEPTED' WHERE request_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, requestId);
                            updateStmt.executeUpdate();
                        }

                        // 3. 친구 관계 추가 (양방향)
                        String insertSql = "INSERT INTO Friends (member_id, friend_id) VALUES (?, ?), (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, senderId);
                            insertStmt.setInt(2, receiverId);
                            insertStmt.setInt(3, receiverId);
                            insertStmt.setInt(4, senderId);
                            insertStmt.executeUpdate();
                        }

                        conn.commit();
                        return true;
                    }
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            System.err.println("친구 요청 수락 중 오류 발생: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("롤백 중 오류 발생: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("연결 닫기 중 오류 발생: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * 친구 요청 거절
     * @param requestId 요청 ID
     * @return 거절 성공 여부
     */
    public boolean rejectFriendRequest(int requestId) {
        String sql = "UPDATE FriendRequests SET status = 'REJECTED' WHERE request_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, requestId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("친구 요청 거절 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 친구 요청이 이미 존재하는지 확인
     * @param senderId 요청자 ID
     * @param receiverId 수신자 ID
     * @return 요청 존재 여부
     */
    public boolean existsFriendRequest(int senderId, int receiverId) {
        String sql = "SELECT COUNT(*) FROM FriendRequests " +
                "WHERE sender_id = ? AND receiver_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("친구 요청 확인 중 오류 발생: " + e.getMessage());
        }

        return false;
    }

    /**
     * 반대 방향으로 친구 요청이 이미 존재하는지 확인
     * @param senderId 요청자 ID
     * @param receiverId 수신자 ID
     * @return 반대 방향 요청 존재 여부 및 요청 ID (존재하는 경우)
     */
    public Optional<Integer> getReverseFriendRequest(int senderId, int receiverId) {
        String sql = "SELECT request_id FROM FriendRequests " +
                "WHERE sender_id = ? AND receiver_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 반대 방향 검색 (receiver->sender)
            pstmt.setInt(1, receiverId);
            pstmt.setInt(2, senderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("request_id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("반대 방향 친구 요청 확인 중 오류 발생: " + e.getMessage());
        }

        return Optional.empty();
    }
}