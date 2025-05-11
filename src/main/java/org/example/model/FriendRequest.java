package org.example.model;

import javafx.beans.property.*;

import java.sql.Timestamp;

/**
 * 친구 요청을 나타내는 모델 클래스
 */
public class FriendRequest {
    private final IntegerProperty requestId;
    private final IntegerProperty senderId;
    private final IntegerProperty receiverId;
    private final StringProperty message; // 소개 메시지 추가
    private final StringProperty status; // PENDING, ACCEPTED, REJECTED
    private final ObjectProperty<Timestamp> createdAt;
    private Member senderInfo; // 요청자 정보 (조인 시 사용)

    // 기본 생성자
    public FriendRequest() {
        this(0, 0, 0, "", "PENDING", null);
    }

    // 모든 필드를 포함한 생성자
    public FriendRequest(int requestId, int senderId, int receiverId, String message, String status, Timestamp createdAt) {
        this.requestId = new SimpleIntegerProperty(requestId);
        this.senderId = new SimpleIntegerProperty(senderId);
        this.receiverId = new SimpleIntegerProperty(receiverId);
        this.message = new SimpleStringProperty(message);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    // 상세 정보를 포함한 생성자
    public FriendRequest(int requestId, int senderId, int receiverId, String message, String status, Timestamp createdAt, Member senderInfo) {
        this(requestId, senderId, receiverId, message, status, createdAt);
        this.senderInfo = senderInfo;
    }

    // Getter, Setter 메서드
    public int getRequestId() {
        return requestId.get();
    }

    public IntegerProperty requestIdProperty() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId.set(requestId);
    }

    public int getSenderId() {
        return senderId.get();
    }

    public IntegerProperty senderIdProperty() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId.set(senderId);
    }

    public int getReceiverId() {
        return receiverId.get();
    }

    public IntegerProperty receiverIdProperty() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId.set(receiverId);
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
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

    public Timestamp getCreatedAt() {
        return createdAt.get();
    }

    public ObjectProperty<Timestamp> createdAtProperty() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt.set(createdAt);
    }

    public Member getSenderInfo() {
        return senderInfo;
    }

    public void setSenderInfo(Member senderInfo) {
        this.senderInfo = senderInfo;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "requestId=" + getRequestId() +
                ", senderId=" + getSenderId() +
                ", receiverId=" + getReceiverId() +
                ", message='" + getMessage() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", senderInfo=" + (senderInfo != null ? senderInfo.getNickname() : "null") +
                '}';
    }
}