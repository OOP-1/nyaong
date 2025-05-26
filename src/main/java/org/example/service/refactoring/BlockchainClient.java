package org.example.service.refactoring;

import org.example.contract.MessageVerifier;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class BlockchainClient {

	private final MessageVerifier contract;

	public BlockchainClient(MessageVerifier contract) {
		this.contract = contract;
	}

	public int verifyMessageOnChain(String senderId, byte[] hash) {
		try {
			TransactionReceipt receipt = contract.verifyMessage(senderId, hash).send();
			return contract.getMessageVerifiedEvents(receipt)
				.stream()
				.findFirst()
				.map(event -> event.messageId.intValue())
				.orElseThrow(() -> new IllegalStateException("메시지 검증 이벤트 없음"));
		} catch (Exception e) {
			throw new RuntimeException("블록체인 메시지 검증 실패", e);
		}
	}

	// 기타 블록체인 관련 메서드들도 여기에 분리
}

