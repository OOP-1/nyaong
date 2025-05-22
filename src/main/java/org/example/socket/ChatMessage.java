package org.example.socket;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 클라이언트와 서버 간 교환되는 채팅 메시지
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ChatMessageType type;
    private int chatRoomId;
    private int senderId;
    private String content;
    private Timestamp timestamp;

    // 발신자 정보
    private String senderNickname;
    private String senderStatus;

    /**
     * 기본 생성자 (직렬화용)
     */
    public ChatMessage() {
    }

    /**
     * 메시지 생성자
     */
    public ChatMessage(ChatMessageType type, int chatRoomId, int senderId, String content, Timestamp timestamp) {
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * 발신자 정보가 포함된 메시지 생성자
     */
    public ChatMessage(ChatMessageType type, int chatRoomId, int senderId, String content,
                       Timestamp timestamp, String senderNickname, String senderStatus) {
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.senderNickname = senderNickname;
        this.senderStatus = senderStatus;
    }

    // Getter 및 Setter 메서드
    public ChatMessageType getType() {
        return type;
    }

    public void setType(ChatMessageType type) {
        this.type = type;
    }

    public int getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public String getSenderStatus() {
        return senderStatus;
    }

    public void setSenderStatus(String senderStatus) {
        this.senderStatus = senderStatus;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "type=" + type +
                ", chatRoomId=" + chatRoomId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", senderNickname='" + senderNickname + '\'' +
                '}';
    }
}