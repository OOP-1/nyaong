package org.example.control;

import org.example.model.Message;
import org.example.repository.MessageRepository;
import org.example.service.BlockchainMessageService;

import java.sql.Timestamp;
import java.util.List;

public class LogController {
    private final MessageRepository messageRepository;
    private final BlockchainMessageService blockchainService;

    public LogController() {
        this.messageRepository = new MessageRepository();
        this.blockchainService = new BlockchainMessageService();
    }

    public List<Message> searchAndVerifyLogs(Integer chatRoomId, Integer senderId, Timestamp from, Timestamp to) {
        List<Message> messages = messageRepository.searchMessages(chatRoomId, senderId, from, to);

        for (Message message : messages) {
            message.setVerificationStatus(blockchainService.verifyMessage(message));
        }

        return messages;
    }
}

