package org.example.boundary;

import java.util.List;
import org.example.model.Message;
import org.example.repository.MessageRepository;
import org.example.service.BlockchainMessageService;
import org.junit.jupiter.api.Test;

public class BlockTest {
    @Test
    void 메세지테스트() {
        MessageRepository repo = new MessageRepository();
        BlockchainMessageService blockchainMessageService = new BlockchainMessageService();
        int chatRoomId = 2;

        List<Message> list = repo.getMessagesByChatRoomId(chatRoomId, 999, 0);

        for (Message msg : list) {
            System.out.print(msg.getMessageId() + ": ");
            if (blockchainMessageService.verifyMessage(msg.getBlockchainMessageId(), msg.getMessageContent())) {
                System.out.println("✅");
            } else {
                System.out.println("🔥");
            }
        }
    }
}
