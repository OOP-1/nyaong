// src/main/java/org/example/model/ChatRoom.java
package org.example.model;

import javafx.beans.property.*;
import java.sql.Timestamp;
import java.util.List;

public class ChatRoom {
    private final IntegerProperty chatRoomId;
    private final StringProperty chatRoomName;
    private final BooleanProperty isGroupChat;
    private final ObjectProperty<Timestamp> createdAt;
    private final ObjectProperty<Timestamp> lastMessageTime;
    private final StringProperty lastMessagePreview;
    private List<Member> members; // 채팅방 참여 멤버 목록

    // 기본 생성자
    public ChatRoom() {
        this(0, "", false, null, null, "");
    }

    // 모든 필드를 포함한 생성자
    public ChatRoom(int chatRoomId, String chatRoomName, boolean isGroupChat,
                    Timestamp createdAt, Timestamp lastMessageTime, String lastMessagePreview) {
        this.chatRoomId = new SimpleIntegerProperty(chatRoomId);
        this.chatRoomName = new SimpleStringProperty(chatRoomName);
        this.isGroupChat = new SimpleBooleanProperty(isGroupChat);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.lastMessageTime = new SimpleObjectProperty<>(lastMessageTime);
        this.lastMessagePreview = new SimpleStringProperty(lastMessagePreview);
    }

    // 멤버 정보를 포함한 생성자
    public ChatRoom(int chatRoomId, String chatRoomName, boolean isGroupChat,
                    Timestamp createdAt, Timestamp lastMessageTime, String lastMessagePreview,
                    List<Member> members) {
        this(chatRoomId, chatRoomName, isGroupChat, createdAt, lastMessageTime, lastMessagePreview);
        this.members = members;
    }

    // Getter, Setter 메서드
    public int getChatRoomId() {
        return chatRoomId.get();
    }

    public IntegerProperty chatRoomIdProperty() {
        return chatRoomId;
    }

    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId.set(chatRoomId);
    }

    public String getChatRoomName() {
        return chatRoomName.get();
    }

    public StringProperty chatRoomNameProperty() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName.set(chatRoomName);
    }

    public boolean isGroupChat() {
        return isGroupChat.get();
    }

    public BooleanProperty isGroupChatProperty() {
        return isGroupChat;
    }

    public void setIsGroupChat(boolean isGroupChat) {
        this.isGroupChat.set(isGroupChat);
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

    public Timestamp getLastMessageTime() {
        return lastMessageTime.get();
    }

    public ObjectProperty<Timestamp> lastMessageTimeProperty() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime.set(lastMessageTime);
    }

    public String getLastMessagePreview() {
        return lastMessagePreview.get();
    }

    public StringProperty lastMessagePreviewProperty() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview.set(lastMessagePreview);
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "chatRoomId=" + getChatRoomId() +
                ", chatRoomName='" + getChatRoomName() + '\'' +
                ", isGroupChat=" + isGroupChat() +
                ", createdAt=" + getCreatedAt() +
                ", lastMessageTime=" + getLastMessageTime() +
                ", lastMessagePreview='" + getLastMessagePreview() + '\'' +
                ", membersCount=" + (members != null ? members.size() : 0) +
                '}';
    }
}