package org.example.socket;

import java.io.Serializable;
import java.util.List;

/**
 * 클라이언트와 서버 간 교환되는 채팅 명령
 */
public class ChatCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private ChatCommandType type;
    private int chatRoomId;
    private int memberId;
    private List<Integer> members;

    /**
     * 기본 생성자 (직렬화용)
     */
    public ChatCommand() {
    }

    /**
     * 채팅방 관련 명령 생성자
     */
    public ChatCommand(ChatCommandType type, int chatRoomId) {
        this.type = type;
        this.chatRoomId = chatRoomId;
    }

    /**
     * 멤버 관련 명령 생성자
     */
    public ChatCommand(ChatCommandType type, int chatRoomId, int memberId) {
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.memberId = memberId;
    }

    /**
     * 멤버 목록 업데이트 명령 생성자
     */
    public ChatCommand(ChatCommandType type, int chatRoomId, List<Integer> members) {
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.members = members;
    }

    // Getter 및 Setter 메서드
    public ChatCommandType getType() {
        return type;
    }

    public void setType(ChatCommandType type) {
        this.type = type;
    }

    public int getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public List<Integer> getMembers() {
        return members;
    }

    public void setMembers(List<Integer> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "ChatCommand{" +
                "type=" + type +
                ", chatRoomId=" + chatRoomId +
                ", memberId=" + memberId +
                '}';
    }
}