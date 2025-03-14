package com.experimental.anonchat.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor // Add NoArgsConstructor for easier object creation if needed
@AllArgsConstructor
@Builder
public class ChatSession {
    private String chatSessionId; // String to store UUID
    private User user1;
    private User user2;
    private List<ChatMessage> messages;

    public ChatSession(String chatSessionId, User user1, User user2) {
        this.chatSessionId = chatSessionId;
        this.user1 = user1;
        this.user2 = user2;
        this.messages = new ArrayList<>();
    }
}
