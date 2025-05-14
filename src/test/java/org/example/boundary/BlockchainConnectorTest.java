package org.example.boundary;

import java.math.BigInteger;
import java.util.List;
import org.example.contract.MessageVerifier;
import org.example.utils.HashUtil;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.exceptions.ContractCallException;

class BlockchainConnectorTest {
    @Test
    void getBlockchain() throws Exception {
        BlockchainConnector connect = new BlockchainConnector();
        MessageVerifier contract = connect.getContract();

        System.out.println("📦 Contract deployed at: " + contract.getContractAddress());

        // ✅ 메시지 원문 → SHA-256 해시
        String originalMessage = "Hello, blockchain world!";
        String messageHash = HashUtil.sha256(originalMessage);
        System.out.println("🔒 SHA-256 Hash: " + messageHash);

        // ✅ 메시지 등록
        TransactionReceipt receipt = contract.sendMessage(messageHash).send();
        BigInteger messageCount = contract.messageCount().send();
        BigInteger messageId = messageCount.subtract(BigInteger.ONE);

        System.out.println("📨 Message sent with ID: " + messageId);

        // ✅ 메시지에 서명
        contract.signMessage(messageId).send();
        System.out.println("✅ Signed message ID " + messageId + " by: tester");

        // ✅ 서명자 목록 조회
        try {
            List<String> signers = contract.getSigners(messageId).send();
            System.out.println("✍️ Signers: " + signers);
        } catch (ContractCallException e) {
            System.out.println("⚠️ 아직 이 메시지에 대한 서명이 없습니다.");
        }
        // ✅ 무결성 검증
        String onChainHash = contract.getMessageHash(messageId).send();
        if (onChainHash.equals(messageHash)) {
            System.out.println("✅ Message integrity verified.");
        } else {
            System.out.println("❌ Message has been tampered!");
        }
    }
}