package com.experimental.anonchat.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessage {
    private String messageId;
    private String chatSessionId;
    private String senderId;
    private String recipientId;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessage() {
        this.messageId = java.util.UUID.randomUUID().toString(); // Generate unique message ID
        this.timestamp = LocalDateTime.now(); // Set timestamp to current time
    }

    public ChatMessage(String chatSessionId, String senderId, String recipientId, String content) {
        this(); // Call default constructor to set messageId and timestamp
        this.chatSessionId = chatSessionId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", sessionId='" + chatSessionId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + recipientId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}