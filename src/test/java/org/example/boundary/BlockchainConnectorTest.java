package org.example.boundary;

import java.math.BigInteger;
import java.util.List;
import org.example.contract.MessageVerifier;
import org.example.model.Member;
import org.example.model.Message;
import org.example.service.BlockchainMessageService;
import org.example.utils.HashUtil;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.exceptions.ContractCallException;

class BlockchainConnectorTest {
    @Test
    void getBlockchain() {
        BlockchainMessageService blockchainService = new BlockchainMessageService();

        // 1. 메시지 객체 생성
        Member sender = new Member("alice123", "password", "Alice", "USER");
        Message message = new Message();
        message.setSender(sender);
        message.setMessageContent("안녕하세요, 이것은 검증이 필요한 중요한 메시지입니다.");

        // 2. 메시지 해시 출력 (확인용)
        String messageHash = HashUtil.sha256(message.getMessageContent());
        System.out.println("메시지 SHA-256 해시: " + messageHash);

        // 3. 블록체인에 메시지 검증 시작
        int blockchainMessageId = blockchainService.verifyMessage(message);
        if (blockchainMessageId >= 0) {
            System.out.println("메시지가 블록체인에 기록되었습니다. ID: " + blockchainMessageId);
        }

        // 4. 다른 사용자가 메시지에 서명
        Member signer1 = new Member("bob456", "password", "Bob", "USER");
        Member signer2 = new Member("charlie789", "password", "Charlie", "USER");

        blockchainService.signMessage(blockchainMessageId, signer1);
        blockchainService.signMessage(blockchainMessageId, signer2);

        // 5. 서명 목록 조회
        List<String> signers = blockchainService.getMessageSigners(blockchainMessageId);
        System.out.println("메시지 서명자 목록: " + signers);

        // 6. 메시지 내용 검증
        boolean isContentValid = blockchainService.verifyMessageContent(
                blockchainMessageId,
                message.getMessageContent()
        );
        System.out.println("메시지 내용 검증 결과: " + (isContentValid ? "유효함" : "변조됨"));

        // 7. 변조된 메시지 테스트
        String tamperedContent = message.getMessageContent() + " (변조됨)";
        boolean isTamperedValid = blockchainService.verifyMessageContent(
                blockchainMessageId,
                tamperedContent
        );
        System.out.println("변조된 메시지 검증 결과: " + (isTamperedValid ? "유효함" : "변조됨"));
    }
}