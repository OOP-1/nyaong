package org.example.service;

import org.example.model.Member;
import org.example.repository.MemberRepository;

import java.util.Optional;

public class AuthService {
    private final MemberRepository memberRepository;
    private static Member currentUser = null; // 현재 로그인한 사용자

    public AuthService() {
        this.memberRepository = new MemberRepository();
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

    // 로그인
    public boolean login(String userId, String password) {
        Optional<Member> member = memberRepository.findByUserIdAndPassword(userId, password);

        if (member.isPresent()) {
            currentUser = member.get();
            // 상태를 ONLINE으로 변경
            memberRepository.updateStatus(currentUser.getMemberId(), "ONLINE");
            return true;
        }

        return false;
    }

    // 로그아웃
    public void logout() {
        if (currentUser != null) {
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