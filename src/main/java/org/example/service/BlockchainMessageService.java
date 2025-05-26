package org.example.service;

import java.math.BigInteger;
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
    private final MessageVerifier contract = new BlockchainConnector().getContract();

    /**
     * 메시지 내용 검증 (해시 비교)
     */
    public boolean verifyMessage(Message msg) {
        try {
            String messageContent = msg.getMessageContent();
            int blockchainMessageId = msg.getBlockchainMessageId();
            String storedHashHex = getMessageHash(blockchainMessageId);
            String calculatedHashHex = HashUtil.sha256(messageContent);

            return storedHashHex != null && storedHashHex.equals(calculatedHashHex);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자가 메시지에 서명
     */
    public boolean signMessage(Message msg, Member signer) {
        try {
            int blockchainMessageId = msg.getBlockchainMessageId();
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
    public boolean getSigned(int blockchainMessageId, String userId) {
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
    public List<String> getMessageSigners(Message msg) {
        try {
            int blockchainMessageId = msg.getBlockchainMessageId();
            return contract.getSigners(
                    BigInteger.valueOf(blockchainMessageId)
            ).send();
        } catch (Exception e) {
            System.out.println("id -1 : 블록체인 등록중인 메시지입니다.");
            return new ArrayList<>();
        }
    }

    /**
     * 메시지 내용을 블록체인에 저장하고 검증 시작
     */
    public int addMessage(int senderId, String content) {
        try {
            // 메시지 내용의 해시값 생성 (HashUtil 사용)
            String hashHex = HashUtil.sha256(content);
            byte[] hashBytes = HashUtil.hexStringToBytes32(hashHex);

            // 발신자 정보 가져오기
            String senderUserId = senderId + "";

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

    private String getMessageHash(int blockchainMessageId) {
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
}