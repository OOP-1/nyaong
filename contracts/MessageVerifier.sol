// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

contract MessageVerification {

    struct Message {
        address sender;
        string messageHash;  // 메시지의 SHA-256 해시
        uint256 timestamp;
        address[] signatures; // 서명한 사용자들
        mapping(address => bool) hasSigned; // 서명 여부를 빠르게 확인하기 위한 매핑
    }

    // Solidity 0.8.0 이상에서는 구조체 내부에 매핑이 있으면 다음과 같이 매핑을 사용해야 합니다
    mapping(uint256 => Message) public messages;
    uint256 public messageCount = 0;

    event MessageSent(uint256 indexed messageId, address indexed sender, string messageHash);
    event MessageSigned(uint256 indexed messageId, address indexed signer);

    function sendMessage(string memory messageHash) public returns (uint256) {
        uint256 currentId = messageCount;
        messageCount++;
        
        Message storage newMessage = messages[currentId];
        newMessage.sender = msg.sender;
        newMessage.messageHash = messageHash;
        newMessage.timestamp = block.timestamp;
        // 배열 초기화는 선언만으로 충분하며, 매핑은 기본값이 false로 자동 초기화됩니다

        emit MessageSent(currentId, msg.sender, messageHash);
        return currentId;
    }

    function signMessage(uint256 messageId) public {
        require(messageId < messageCount, "Message does not exist.");
        require(!messages[messageId].hasSigned[msg.sender], "Already signed.");
        
        messages[messageId].signatures.push(msg.sender);
        messages[messageId].hasSigned[msg.sender] = true;
        
        emit MessageSigned(messageId, msg.sender);
    }

    function getSigners(uint256 messageId) public view returns (address[] memory) {
        require(messageId < messageCount, "Message does not exist.");
        return messages[messageId].signatures;
    }

    function getMessageHash(uint256 messageId) public view returns (string memory) {
        require(messageId < messageCount, "Message does not exist.");
        return messages[messageId].messageHash;
    }
    
    function hasUserSigned(uint256 messageId, address user) public view returns (bool) {
        require(messageId < messageCount, "Message does not exist.");
        return messages[messageId].hasSigned[user];
    }
    
    function getMessageDetails(uint256 messageId) public view returns (
        address sender,
        string memory messageHash,
        uint256 timestamp,
        uint256 signatureCount
    ) {
        require(messageId < messageCount, "Message does not exist.");
        Message storage messageData = messages[messageId];
        return (
            messageData.sender,
            messageData.messageHash,
            messageData.timestamp,
            messageData.signatures.length
        );
    }
}