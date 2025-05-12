package org.example.repository;

import org.example.boundary.DatabaseConnector;
import org.example.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberRepository {

    // 회원 추가 (회원가입)
    public boolean add(Member member) {
        String sql = "INSERT INTO Members (user_id, password, nickname, status, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getUserId());
            pstmt.setString(2, member.getPassword());
            pstmt.setString(3, member.getNickname());
            pstmt.setString(4, member.getStatus());
            pstmt.setString(5, member.getRole());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setMemberId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;

        } catch (SQLException e) {
            System.err.println("회원 추가 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    // 로그인 검증
    public Optional<Member> findByUserIdAndPassword(String userId, String password) {
        String sql = "SELECT * FROM Members WHERE user_id = ? AND password = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Member member = new Member(
                            rs.getInt("member_id"),
                            rs.getString("user_id"),
                            rs.getString("password"),
                            rs.getString("nickname"),
                            rs.getString("status"),
                            rs.getString("role")
                    );
                    return Optional.of(member);
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("로그인 검증 중 오류 발생: " + e.getMessage());
            return Optional.empty();
        }
    }

    // 사용자 ID로 회원 찾기
    public Optional<Member> findByUserId(String userId) {
        String sql = "SELECT * FROM Members WHERE user_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Member member = new Member(
                            rs.getInt("member_id"),
                            rs.getString("user_id"),
                            rs.getString("password"),
                            rs.getString("nickname"),
                            rs.getString("status"),
                            rs.getString("role")
                    );
                    return Optional.of(member);
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("회원 조회 중 오류 발생: " + e.getMessage());
            return Optional.empty();
        }
    }

    // 상태 업데이트 (온라인/오프라인)
    public boolean updateStatus(int memberId, String status) {
        String sql = "UPDATE Members SET status = ? WHERE member_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, memberId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("상태 업데이트 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 회원 ID로 회원 정보 조회
     * @param memberId 회원 ID
     * @return 회원 정보
     */
    public Optional<Member> findById(int memberId) {
        String sql = "SELECT * FROM Members WHERE member_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Member member = new Member(
                            rs.getInt("member_id"),
                            rs.getString("user_id"),
                            rs.getString("password"),
                            rs.getString("nickname"),
                            rs.getString("status"),
                            rs.getString("role")
                    );
                    return Optional.of(member);
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("회원 조회 중 오류 발생: " + e.getMessage());
            return Optional.empty();
        }
    }

    // 전체 회원 목록 조회
    public List<Member> findAll() {
        String sql = "SELECT * FROM Members";
        List<Member> members = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Member member = new Member(
                        rs.getInt("member_id"),
                        rs.getString("user_id"),
                        rs.getString("password"),
                        rs.getString("nickname"),
                        rs.getString("status"),
                        rs.getString("role")
                );
                members.add(member);
            }

        } catch (SQLException e) {
            System.err.println("회원 목록 조회 중 오류 발생: " + e.getMessage());
        }

        return members;
    }
}

