package org.example.service.refactoring;

import org.example.model.Message;
import org.example.utils.HashUtil;

public class MessageVerificationService {

	private final MessageHashService hashService;
	private final BlockchainClient blockchainClient;

	public MessageVerificationService(MessageHashService hashService, BlockchainClient blockchainClient) {
		this.hashService = hashService;
		this.blockchainClient = blockchainClient;
	}

	public int verifyAndStoreMessage(Message message) {
		String hash = hashService.calculateHash(message.getMessageContent());
		byte[] hashBytes = HashUtil.hexStringToBytes32(hash);
		String senderId = message.getSender().getUserId();

		return blockchainClient.verifyMessageOnChain(senderId, hashBytes);
	}
}
