// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

contract MessageVerification {

    struct VerifiedMessage {
        string senderUserId;       // 발신자 userId
        bytes32 messageHash;       // messageContent의 SHA-256 해시값 (바이트 배열로 저장)
        uint256 timestamp;         // 블록체인 타임스탬프
        string[] signerUserIds;    // 서명한 사용자의 userId 배열
        mapping(string => bool) hasSigned; // userId로 서명 여부 확인
    }

    mapping(uint256 => VerifiedMessage) public messages;
    uint256 public messageCount = 0;

    event MessageVerified(uint256 indexed messageId, string senderUserId, bytes32 messageHash);
    event MessageSigned(uint256 indexed messageId, string signerUserId);

    // messageContent의 해시값을 bytes32로 저장하고 검증 시작
    function verifyMessage(string memory senderUserId, bytes32 messageHash) public returns (uint256) {
        uint256 currentId = messageCount;
        messageCount++;
        
        VerifiedMessage storage newMessage = messages[currentId];
        newMessage.senderUserId = senderUserId;
        newMessage.messageHash = messageHash;
        newMessage.timestamp = block.timestamp;
        
        emit MessageVerified(currentId, senderUserId, messageHash);
        return currentId;
    }

    // 사용자가 메시지에 서명
    function signMessage(uint256 messageId, string memory signerUserId) public {
        require(messageId < messageCount, "Message does not exist.");
        require(!messages[messageId].hasSigned[signerUserId], "Already signed by this user.");
        
        messages[messageId].signerUserIds.push(signerUserId);
        messages[messageId].hasSigned[signerUserId] = true;
        
        emit MessageSigned(messageId, signerUserId);
    }

    // 메시지에 서명한 사용자 목록 조회
    function getSigners(uint256 messageId) public view returns (string[] memory) {
        require(messageId < messageCount, "Message does not exist.");
        return messages[messageId].signerUserIds;
    }

    // 메시지 해시값 조회
    function getMessageHash(uint256 messageId) public view returns (bytes32) {
        require(messageId < messageCount, "Message does not exist.");
        return messages[messageId].messageHash;
    }
    
    // 특정 사용자의 서명 여부 확인
    function hasUserSigned(uint256 messageId, string memory userId) public view returns (bool) {
        require(messageId < messageCount, "Message does not exist.");
        return messages[messageId].hasSigned[userId];
    }
    
    // 메시지 상세 정보 조회
    function getMessageDetails(uint256 messageId) public view returns (
        string memory senderUserId,
        bytes32 messageHash,
        uint256 timestamp,
        uint256 signatureCount
    ) {
        require(messageId < messageCount, "Message does not exist.");
        VerifiedMessage storage messageData = messages[messageId];
        return (
            messageData.senderUserId,
            messageData.messageHash,
            messageData.timestamp,
            messageData.signerUserIds.length
        );
    }
}