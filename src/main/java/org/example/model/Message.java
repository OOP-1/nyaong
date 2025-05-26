// src/main/java/org/example/model/Message.java
package org.example.model;

import javafx.beans.property.*;
import java.sql.Timestamp;

public class Message {
    private final IntegerProperty messageId;
    private final IntegerProperty blockchainMessageId;
    private final IntegerProperty chatRoomId;
    private final IntegerProperty senderId;
    private final StringProperty messageContent;
    private final ObjectProperty<Timestamp> createdAt;
    private Member sender; // 메시지 발신자 정보

    // 기본 생성자
    public Message() {
        this(0, 0, 0, 0, "", null);
    }

    // 모든 필드를 포함한 생성자
    public Message(int messageId, int blockchainMessageId, int chatRoomId, int senderId, String messageContent, Timestamp createdAt) {
        this.messageId = new SimpleIntegerProperty(messageId);
        this.blockchainMessageId = new SimpleIntegerProperty(blockchainMessageId);
        this.chatRoomId = new SimpleIntegerProperty(chatRoomId);
        this.senderId = new SimpleIntegerProperty(senderId);
        this.messageContent = new SimpleStringProperty(messageContent);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    // 발신자 정보를 포함한 생성자
    public Message(int messageId, int blockchainMessageId, int chatRoomId, int senderId, String messageContent, Timestamp createdAt, Member sender) {
        this(messageId, blockchainMessageId, chatRoomId, senderId, messageContent, createdAt);
        this.sender = sender;
    }

    // Getter, Setter 메서드
    public int getMessageId() {
        return messageId.get();
    }

    public IntegerProperty messageIdProperty() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId.set(messageId);
    }

    public int getBlockchainMessageId() { return blockchainMessageId.get(); }

    public IntegerProperty blockchainMessageIdProperty() { return blockchainMessageId; }

    public void setBlockchainMessageId(int blockchainMessageId) { this.blockchainMessageId.set(blockchainMessageId); }

    public int getChatRoomId() {
        return chatRoomId.get();
    }

    public IntegerProperty chatRoomIdProperty() {
        return chatRoomId;
    }

    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId.set(chatRoomId);
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

    public String getMessageContent() {
        return messageContent.get();
    }

    public StringProperty messageContentProperty() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent.set(messageContent);
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

    public Member getSender() {
        return sender;
    }

    public void setSender(Member sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + getMessageId() +
                ", blockchainMessageId=" + getBlockchainMessageId() +
                ", chatRoomId=" + getChatRoomId() +
                ", senderId=" + getSenderId() +
                ", messageContent='" + getMessageContent() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", sender=" + (sender != null ? sender.getNickname() : "null") +
                '}';
    }
}