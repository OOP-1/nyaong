package org.example.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import org.example.boundary.BlockchainConnector;
import org.example.contract.MessageVerifier;
import org.example.contract.MessageVerifier.MessageVerifiedEventResponse;
import org.example.model.Member;
import org.example.model.Message;
import org.example.utils.HashUtil;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class BlockchainMessageService {
    BlockchainConnector connect = new BlockchainConnector();
    MessageVerifier contract = connect.getContract();

    /**
     * 메시지 내용을 블록체인에 저장하고 검증 시작
     */
    public int verifyMessage(Message message) {
        try {
            // 메시지 내용의 해시값 생성 (HashUtil 사용)
            String hashHex = HashUtil.sha256(message.getMessageContent());
            byte[] hashBytes = HashUtil.hexStringToBytes32(hashHex);

            // 발신자 정보 가져오기
            String senderUserId = message.getSender().getUserId();

            // 컨트랙트 함수 호출
            TransactionReceipt receipt = contract.verifyMessage(
                    senderUserId,
                    hashBytes  // bytes32 타입으로 전달
            ).send();

            // 이벤트에서 메시지 ID 추출
            List<MessageVerifiedEventResponse> events =
                    contract.getMessageVerifiedEvents(receipt);

            if (!events.isEmpty()) {
                return events.get(0).messageId.intValue();
            }

            return -1; // 실패
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 사용자가 메시지에 서명
     */
    public boolean signMessage(int blockchainMessageId, Member signer) {
        try {
            TransactionReceipt receipt = contract.signMessage(
                    BigInteger.valueOf(blockchainMessageId),
                    signer.getUserId()
            ).send();

            return receipt.isStatusOK();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 메시지 서명 여부 확인
     */
    public boolean hasUserSigned(int blockchainMessageId, String userId) {
        try {
            return contract.hasUserSigned(
                    BigInteger.valueOf(blockchainMessageId),
                    userId
            ).send();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 메시지 서명자 목록 조회
     */
    public List<String> getMessageSigners(int blockchainMessageId) {
        try {
            return contract.getSigners(
                    BigInteger.valueOf(blockchainMessageId)
            ).send();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 메시지 해시값 조회 (16진수 문자열로 변환)
     */
    public String getMessageHash(int blockchainMessageId) {
        try {
            byte[] hashBytes = contract.getMessageHash(
                    BigInteger.valueOf(blockchainMessageId)
            ).send();

            return HashUtil.bytesToHex(hashBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 메시지 내용 검증 (해시 비교)
     */
    public boolean verifyMessageContent(int blockchainMessageId, String messageContent) {
        try {
            String storedHashHex = getMessageHash(blockchainMessageId);
            String calculatedHashHex = HashUtil.sha256(messageContent);

            return storedHashHex != null && storedHashHex.equals(calculatedHashHex);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}