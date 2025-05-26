package org.example.socket;

/**
 * 채팅 메시지 유형 열거형
 */
public enum ChatMessageType {
    CHAT,      // 일반 채팅 메시지
    SYSTEM,    // 시스템 메시지 (입장, 퇴장 등)
    FILE,      // 파일 전송 (향후 구현)
    TYPING,    // 타이핑 중 (향후 구현)
    READ       // 읽음 확인 (향후 구현)
}