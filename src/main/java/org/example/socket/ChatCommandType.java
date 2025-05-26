package org.example.socket;

/**
 * 채팅 명령 유형 열거형
 */
public enum ChatCommandType {
    JOIN_CHAT,          // 채팅방 참여
    JOIN_CHAT_SUCCESS,  // 채팅방 참여 성공
    LEAVE_CHAT,         // 채팅방 나가기
    LEAVE_CHAT_SUCCESS, // 채팅방 나가기 성공
    UPDATE_MEMBERS,     // 채팅방 멤버 목록 업데이트
    REFRESH,           // 채팅방 목록 새로고침
    ERROR              // 오류 응답
}