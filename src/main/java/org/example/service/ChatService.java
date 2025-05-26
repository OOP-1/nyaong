// src/main/java/org/example/service/ChatService.java
package org.example.service;

import org.example.model.ChatRoom;
import org.example.model.Member;
import org.example.model.Message;
import org.example.repository.ChatRoomRepository;
import org.example.repository.MessageRepository;
import org.example.repository.MemberRepository;
import org.example.socket.ChatCommandType;
import org.example.socket.ChatCommand;
import org.example.socket.ChatSocketClient;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ChatSocketClient socketClient;
    private final BlockchainMessageService blockchainMessageService = new BlockchainMessageService();

    public ChatService() {
        this.chatRoomRepository = new ChatRoomRepository();
        this.messageRepository = new MessageRepository();
        this.memberRepository = new MemberRepository();
        this.socketClient = ChatSocketClient.getInstance();
    }

    /**
     * 1:1 채팅방 생성 또는 조회
     * @param currentMemberId 현재 사용자 ID
     * @param targetMemberId 대화 상대 ID
     * @return 채팅방 생성/조회 결과
     */
    public ChatResult createPrivateChatRoom(int currentMemberId, int targetMemberId) {
        if (currentMemberId == targetMemberId) {
            return new ChatResult(false, "자기 자신과 채팅할 수 없습니다.", -1);
        }

        int chatRoomId = chatRoomRepository.getOrCreatePrivateChatRoom(currentMemberId, targetMemberId);

        if (chatRoomId > 0) {
            // 채팅방 생성 후 소켓 서버에 멤버 목록 업데이트
            List<Integer> members = new ArrayList<>();
            members.add(currentMemberId);
            members.add(targetMemberId);
            socketClient.updateChatRoomMembers(chatRoomId, members);

            Optional<Member> targetMember = memberRepository.findById(targetMemberId);
            String targetName = targetMember.isPresent() ? targetMember.get().getNickname() : "상대방";
            return new ChatResult(true, targetName + "님과의 대화방이 생성되었습니다.", chatRoomId);
        } else {
            return new ChatResult(false, "채팅방 생성에 실패했습니다.", -1);
        }
    }

    /**
     * 그룹 채팅방 생성
     * @param currentMemberId 현재 사용자 ID
     * @param chatRoomName 채팅방 이름
     * @param memberIds 초대할 멤버 ID 목록
     * @return 채팅방 생성 결과
     */
    public ChatResult createGroupChatRoom(int currentMemberId, String chatRoomName, List<Integer> memberIds) {
        // 자기 자신도 멤버 목록에 추가
        if (!memberIds.contains(currentMemberId)) {
            memberIds.add(currentMemberId);
        }

        // 최소 2명 이상 필요
        if (memberIds.size() < 2) {
            return new ChatResult(false, "채팅방에는 최소 2명 이상이 필요합니다.", -1);
        }

        int chatRoomId = chatRoomRepository.createChatRoom(chatRoomName, true, memberIds);

        if (chatRoomId > 0) {
            // 채팅방 생성 후 소켓 서버에 멤버 목록 업데이트
            socketClient.updateChatRoomMembers(chatRoomId, memberIds);

            return new ChatResult(true, "그룹 채팅방이 생성되었습니다.", chatRoomId);
        } else {
            return new ChatResult(false, "채팅방 생성에 실패했습니다.", -1);
        }
    }

    /**
     * 사용자의 채팅방 목록 조회
     * @param memberId 사용자 ID
     * @return 채팅방 목록
     */
    public List<ChatRoom> getChatRoomsByMemberId(int memberId) {
        return chatRoomRepository.getChatRoomsByMemberId(memberId);
    }

    /**
     * 채팅방 정보 조회
     * @param chatRoomId 채팅방 ID
     * @return 채팅방 정보
     */
    public Optional<ChatRoom> getChatRoomById(int chatRoomId) {
        return chatRoomRepository.getChatRoomById(chatRoomId);
    }

    /**
     * 채팅방 참여자 목록 조회
     * @param chatRoomId 채팅방 ID
     * @return 참여자 목록
     */
    public List<Member> getChatRoomMembers(int chatRoomId) {
        return chatRoomRepository.getChatRoomMembers(chatRoomId);
    }

    /**
     * 채팅방 이름 변경
     * @param chatRoomId 채팅방 ID
     * @param newName 새 채팅방 이름
     * @return 변경 결과
     */
    public ChatResult updateChatRoomName(int chatRoomId, String newName) {
        boolean success = chatRoomRepository.updateChatRoomName(chatRoomId, newName);

        if (success) {
            return new ChatResult(true, "채팅방 이름이 변경되었습니다.", chatRoomId);
        } else {
            return new ChatResult(false, "채팅방 이름 변경에 실패했습니다.", chatRoomId);
        }
    }

    /**
     * 채팅방에 참여자 추가
     * @param chatRoomId 채팅방 ID
     * @param memberId 추가할 참여자 ID
     * @return 추가 결과
     */
    public ChatResult addChatRoomMember(int chatRoomId, int memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isEmpty()) {
            return new ChatResult(false, "존재하지 않는 사용자입니다.", chatRoomId);
        }

        boolean success = chatRoomRepository.addChatRoomMember(chatRoomId, memberId);

        if (success) {
            // 멤버 추가 후 소켓 서버에 멤버 목록 업데이트
            List<Member> members = getChatRoomMembers(chatRoomId);
            List<Integer> memberIds = members.stream()
                    .map(Member::getMemberId)
                    .collect(Collectors.toList());
            socketClient.updateChatRoomMembers(chatRoomId, memberIds);

            // 소켓 서버에 채팅방 입장 알림
            socketClient.joinChatRoom(chatRoomId);

            return new ChatResult(true, member.get().getNickname() + "님이 채팅방에 추가되었습니다.", chatRoomId);
        } else {
            return new ChatResult(false, "참여자 추가에 실패했습니다.", chatRoomId);
        }
    }

    /**
     * 채팅방에서 참여자 제거
     * @param chatRoomId 채팅방 ID
     * @param memberId 제거할 참여자 ID
     * @return 제거 결과
     */
    public ChatResult removeChatRoomMember(int chatRoomId, int memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isEmpty()) {
            return new ChatResult(false, "존재하지 않는 사용자입니다.", chatRoomId);
        }

        boolean success = chatRoomRepository.removeChatRoomMember(chatRoomId, memberId);

        if (success) {
            // 소켓 서버에 채팅방 퇴장 알림
            socketClient.leaveChatRoom(chatRoomId);

            // 멤버 제거 후 소켓 서버에 멤버 목록 업데이트
            List<Member> members = getChatRoomMembers(chatRoomId);
            List<Integer> memberIds = members.stream()
                    .map(Member::getMemberId)
                    .collect(Collectors.toList());
            socketClient.updateChatRoomMembers(chatRoomId, memberIds);

            return new ChatResult(true, member.get().getNickname() + "님이 채팅방에서 제거되었습니다.", chatRoomId);
        } else {
            return new ChatResult(false, "참여자 제거에 실패했습니다.", chatRoomId);
        }
    }

    /**
     * 채팅방 나가기 (소켓 처리 없이) - ChatView에서 이미 소켓 처리했으므로
     * @param chatRoomId 채팅방 ID
     * @param memberId 나가는 참여자 ID
     * @return 나가기 결과
     */
    public ChatResult leaveChatRoomWithoutSocket(int chatRoomId, int memberId) {
        boolean success = chatRoomRepository.leaveChatRoom(chatRoomId, memberId);

        if (success) {
            // 채팅방이 아직 존재하는지 확인
            Optional<ChatRoom> chatRoom = getChatRoomById(chatRoomId);
            if (chatRoom.isPresent()) {
                // 멤버 제거 후 소켓 서버에 멤버 목록 업데이트
                List<Member> members = getChatRoomMembers(chatRoomId);
                List<Integer> memberIds = members.stream()
                        .map(Member::getMemberId)
                        .collect(Collectors.toList());
                socketClient.updateChatRoomMembers(chatRoomId, memberIds);
            }

            return new ChatResult(true, "채팅방에서 나갔습니다.", -1);
        } else {
            return new ChatResult(false, "채팅방 나가기에 실패했습니다.", chatRoomId);
        }
    }

    /**
     * 채팅방 나가기 (기존 메서드 - 호환성 유지)
     * @param chatRoomId 채팅방 ID
     * @param memberId 나가는 참여자 ID
     * @return 나가기 결과
     */
    public ChatResult leaveChatRoom(int chatRoomId, int memberId) {
        // 소켓 서버에 채팅방 퇴장 알림
        socketClient.leaveChatRoom(chatRoomId);

        return leaveChatRoomWithoutSocket(chatRoomId, memberId);
    }

    /**
     * 메시지 전송
     * @param chatRoomId 채팅방 ID
     * @param senderId 발신자 ID
     * @param content 메시지 내용
     * @return 전송 결과
     */
    public ChatResult sendMessage(int chatRoomId, int senderId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ChatResult(false, "메시지 내용을 입력해주세요.", chatRoomId);
        }

        // 블록체인 처리 기다리지 않고, 메시지 먼저 저장 (blockchainId는 -1로 임시 저장)
        int msgId = messageRepository.sendMessage(chatRoomId, -1, senderId, content);

        // 메시지를 블록체인에 저장하는 작업을 별도 스레드로 처리
        new Thread(() -> {
            int blockchainId = blockchainMessageService.addMessage(senderId, content);
            if (blockchainId > 0) {
                // blockchainId가 성공적으로 발급되면, 기존 메시지의 blockchainMessageId를 갱신
                messageRepository.updateBlockchainMessageId(msgId, blockchainId);
            }
        }).start();

        if (msgId > 0) {
            return new ChatResult(true, "메시지가 전송되었습니다.", chatRoomId);
        } else {
            return new ChatResult(false, "메시지 전송에 실패했습니다.", chatRoomId);
        }
    }

    /**
     * 채팅방의 메시지 목록 조회
     * @param chatRoomId 채팅방 ID
     * @param limit 가져올 메시지 수 (최신순)
     * @param offset 건너뛸 메시지 수
     * @return 메시지 목록 (시간순으로 정렬됨)
     */
    public List<Message> getMessagesByChatRoomId(int chatRoomId, int limit, int offset) {
        List<Message> messages = messageRepository.getMessagesByChatRoomId(chatRoomId, limit, offset);
        // 시간 오름차순으로 정렬 (오래된 메시지가 위에 표시되도록)
        Collections.reverse(messages);
        return messages;
    }

    /**
     * 새 메시지 조회 (폴링 방식, 소켓 통신이 사용 불가능한 경우 백업 용도)
     * @param chatRoomId 채팅방 ID
     * @param lastMessageTime 마지막으로 받은 메시지 시간
     * @return 새 메시지 목록
     */
    public List<Message> getNewMessages(int chatRoomId, Timestamp lastMessageTime) {
        if (lastMessageTime == null) {
            return new ArrayList<>();
        }
        return messageRepository.getNewMessages(chatRoomId, lastMessageTime);
    }

    /**
     * 채팅 작업 결과를 전달하는 내부 클래스
     */
    public static class ChatResult {
        private final boolean success;
        private final String message;
        private final int chatRoomId;

        public ChatResult(boolean success, String message, int chatRoomId) {
            this.success = success;
            this.message = message;
            this.chatRoomId = chatRoomId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getChatRoomId() {
            return chatRoomId;
        }
    }
}