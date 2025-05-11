package org.example.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * 친구 관계를 나타내는 모델 클래스
 */
public class Friend {
    private final IntegerProperty memberId;
    private final IntegerProperty friendId;
    private Member friendInfo; // 친구의 상세 정보 (조인 시 사용)

    // 기본 생성자
    public Friend() {
        this(0, 0);
    }

    // 모든 필드를 포함한 생성자
    public Friend(int memberId, int friendId) {
        this.memberId = new SimpleIntegerProperty(memberId);
        this.friendId = new SimpleIntegerProperty(friendId);
    }

    // 상세 정보를 포함한 생성자
    public Friend(int memberId, int friendId, Member friendInfo) {
        this(memberId, friendId);
        this.friendInfo = friendInfo;
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

    public int getFriendId() {
        return friendId.get();
    }

    public IntegerProperty friendIdProperty() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId.set(friendId);
    }

    public Member getFriendInfo() {
        return friendInfo;
    }

    public void setFriendInfo(Member friendInfo) {
        this.friendInfo = friendInfo;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "memberId=" + getMemberId() +
                ", friendId=" + getFriendId() +
                ", friendInfo=" + (friendInfo != null ? friendInfo.getNickname() : "null") +
                '}';
    }
}