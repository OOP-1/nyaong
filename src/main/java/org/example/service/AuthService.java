package org.example.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.model.Member;
import org.example.repository.MemberRepository;
import org.example.socket.ChatSocketClient;

import java.util.Optional;

public class AuthService {
    private final MemberRepository memberRepository;
    private static Member currentUser = null; // 현재 로그인한 사용자
    private final ChatSocketClient socketClient;

    public AuthService() {
        this.memberRepository = new MemberRepository();
        this.socketClient = ChatSocketClient.getInstance();
    }

    // 회원가입
    public boolean register(String userId, String password, String nickname) {
        // 이미 존재하는 사용자 ID인지 확인
        if (memberRepository.findByUserId(userId).isPresent()) {
            return false; // 이미 존재하는 사용자 ID
        }

        // 새 회원 생성 및 저장
        Member newMember = new Member(userId, password, nickname, "USER");
        return memberRepository.add(newMember);
    }

    // 로그인 메서드 수정
    public boolean login(String userId, String password) {
        Optional<Member> member = memberRepository.findByUserIdAndPassword(userId, password);

        if (member.isPresent()) {
            currentUser = member.get();

            // 상태를 ONLINE으로 변경
            memberRepository.updateStatus(currentUser.getMemberId(), "ONLINE");

            // 소켓 연결을 별도 스레드에서 시도 (UI 블로킹 방지)
            new Thread(() -> {
                try {
                    // 소켓 서버에 연결 시도
                    boolean connected = socketClient.connect(currentUser);

                    if (!connected) {
                        // 연결 실패시 경고 메시지 표시 (UI 스레드에서)
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("채팅 서버 연결 실패");
                            alert.setHeaderText(null);
                            alert.setContentText("채팅 서버에 연결할 수 없습니다. 메시지 전송 및 수신이 즉시 업데이트되지 않을 수 있습니다.");
                            alert.showAndWait();
                        });
                    }
                } catch (Exception e) {
                    System.err.println("소켓 연결 중 오류 발생: " + e.getMessage());
                }
            }).start();

            // 소켓 연결 결과와 상관없이 로그인은 성공 처리
            return true;
        }

        return false;
    }

    // 로그아웃
    public void logout() {
        if (currentUser != null) {
            // 소켓 연결 종료
            if (socketClient.isConnected()) {
                socketClient.disconnect();
            }

            // 상태를 OFFLINE으로 변경
            memberRepository.updateStatus(currentUser.getMemberId(), "OFFLINE");
            currentUser = null;
        }
    }

    // 현재 로그인한 사용자 가져오기
    public static Member getCurrentUser() {
        return currentUser;
    }

    // 로그인 상태 확인
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}