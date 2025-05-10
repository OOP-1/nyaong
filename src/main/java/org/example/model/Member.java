package org.example.model;

import javafx.beans.property.*;

public class Member {
    private final IntegerProperty memberId;
    private final StringProperty userId;
    private final StringProperty password;
    private final StringProperty nickname;
    private final StringProperty status;
    private final StringProperty role;

    // 기본 생성자
    public Member() {
        this(0, "", "", "", "", "USER");
    }

    // 모든 필드를 포함한 생성자
    public Member(int memberId, String userId, String password, String nickname, String status, String role) {
        this.memberId = new SimpleIntegerProperty(memberId);
        this.userId = new SimpleStringProperty(userId);
        this.password = new SimpleStringProperty(password);
        this.nickname = new SimpleStringProperty(nickname);
        this.status = new SimpleStringProperty(status);
        this.role = new SimpleStringProperty(role);
    }

    // 회원가입용 생성자 (ID는 자동 생성)
    public Member(String userId, String password, String nickname, String role) {
        this(0, userId, password, nickname, "OFFLINE", role);
    }

    // Getter, Setter 메서드
    public int getMemberId() {
        return memberId.get();
    }

    public IntegerProperty memberIdProperty() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId.set(memberId);
    }

    public String getUserId() {
        return userId.get();
    }

    public StringProperty userIdProperty() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId.set(userId);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getNickname() {
        return nickname.get();
    }

    public StringProperty nicknameProperty() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname.set(nickname);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId=" + getMemberId() +
                ", userId='" + getUserId() + '\'' +
                ", nickname='" + getNickname() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", role='" + getRole() + '\'' +
                '}';
    }
}