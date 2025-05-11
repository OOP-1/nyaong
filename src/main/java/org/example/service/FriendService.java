package org.example.service;

import org.example.model.Friend;
import org.example.model.FriendRequest;
import org.example.model.Member;
import org.example.repository.FriendRepository;
import org.example.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관리 기능을 제공하는 서비스 클래스
 */
public class FriendService {
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    public FriendService() {
        this.friendRepository = new FriendRepository();
        this.memberRepository = new MemberRepository();
    }

    /**
     * 사용자 ID로 친구 요청 (소개 메시지 포함)
     * @param currentMemberId 현재 사용자 ID
     * @param friendUserId 추가할 친구의 사용자 ID
     * @param message 소개 메시지
     * @return 요청 결과 (성공 여부 및 메시지)
     */
    public FriendResult requestFriendByUserId(int currentMemberId, String friendUserId, String message) {
        // 친구의 사용자 ID로 회원 정보 조회
        Optional<Member> friendMember = memberRepository.findByUserId(friendUserId);

        // 존재하지 않는 사용자인 경우
        if (friendMember.isEmpty()) {
            return new FriendResult(false, "존재하지 않는 사용자입니다.");
        }

        int friendId = friendMember.get().getMemberId();

        // 자기 자신을 친구로 추가하려는 경우
        if (currentMemberId == friendId) {
            return new FriendResult(false, "자기 자신을 친구로 추가할 수 없습니다.");
        }

        // 이미 친구인 경우
        if (friendRepository.isFriend(currentMemberId, friendId)) {
            return new FriendResult(false, "이미 친구로 등록된 사용자입니다.");
        }

        // 이미 친구 요청을 보낸 경우
        if (friendRepository.existsFriendRequest(currentMemberId, friendId)) {
            return new FriendResult(false, "이미 친구 요청을 보낸 사용자입니다.");
        }

        // 상대방이 이미 친구 요청을 보낸 경우 자동 수락
        Optional<Integer> reverseRequestId = friendRepository.getReverseFriendRequest(currentMemberId, friendId);
        if (reverseRequestId.isPresent()) {
            boolean accepted = friendRepository.acceptFriendRequest(reverseRequestId.get());
            if (accepted) {
                return new FriendResult(true, friendMember.get().getNickname() + "님의 친구 요청을 수락하여 친구가 되었습니다.");
            } else {
                return new FriendResult(false, "친구 요청 수락 중 오류가 발생했습니다.");
            }
        }

        // 친구 요청 생성
        boolean success = friendRepository.createFriendRequest(currentMemberId, friendId, message);

        if (success) {
            return new FriendResult(true, friendMember.get().getNickname() + "님에게 친구 요청을 보냈습니다.");
        } else {
            return new FriendResult(false, "친구 요청에 실패했습니다.");
        }
    }

    /**
     * 친구 요청 수락
     * @param requestId 요청 ID
     * @return 수락 결과 (성공 여부 및 메시지)
     */
    public FriendResult acceptFriendRequest(int requestId) {
        boolean success = friendRepository.acceptFriendRequest(requestId);

        if (success) {
            return new FriendResult(true, "친구 요청을 수락했습니다.");
        } else {
            return new FriendResult(false, "친구 요청 수락에 실패했습니다.");
        }
    }

    /**
     * 친구 요청 거절
     * @param requestId 요청 ID
     * @return 거절 결과 (성공 여부 및 메시지)
     */
    public FriendResult rejectFriendRequest(int requestId) {
        boolean success = friendRepository.rejectFriendRequest(requestId);

        if (success) {
            return new FriendResult(true, "친구 요청을 거절했습니다.");
        } else {
            return new FriendResult(false, "친구 요청 거절에 실패했습니다.");
        }
    }

    /**
     * 친구 삭제
     * @param currentMemberId 현재 사용자 ID
     * @param friendId 삭제할 친구 ID
     * @return 삭제 성공 여부 및 결과 메시지
     */
    public FriendResult removeFriend(int currentMemberId, int friendId) {
        // 친구 관계 확인
        if (!friendRepository.isFriend(currentMemberId, friendId)) {
            return new FriendResult(false, "친구 목록에 존재하지 않는 사용자입니다.");
        }

        // 친구 삭제
        boolean success = friendRepository.removeFriend(currentMemberId, friendId);

        if (success) {
            return new FriendResult(true, "친구가 삭제되었습니다.");
        } else {
            return new FriendResult(false, "친구 삭제에 실패했습니다.");
        }
    }

    /**
     * 친구 목록 조회
     * @param memberId 사용자 ID
     * @return 친구 목록
     */
    public List<Friend> getFriendsList(int memberId) {
        return friendRepository.getFriendsByMemberId(memberId);
    }

    /**
     * 대기 중인 친구 요청 목록 조회
     * @param memberId 사용자 ID
     * @return 친구 요청 목록
     */
    public List<FriendRequest> getPendingFriendRequests(int memberId) {
        return friendRepository.getPendingFriendRequests(memberId);
    }

    /**
     * 사용자 ID로 검색 (친구 추가를 위한 검색)
     * @param currentMemberId 현재 사용자 ID
     * @param userId 검색 키워드 (사용자 ID)
     * @return 검색 결과 목록
     */
    public List<Member> searchUsersByUserId(int currentMemberId, String userId) {
        return friendRepository.searchMembersByUserId(currentMemberId, userId);
    }

    /**
     * 친구 작업 결과를 전달하는 내부 클래스
     */
    public static class FriendResult {
        private final boolean success;
        private final String message;

        public FriendResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}