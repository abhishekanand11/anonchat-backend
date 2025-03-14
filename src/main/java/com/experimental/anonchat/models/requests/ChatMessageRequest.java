package com.experimental.anonchat.models.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageRequest { // DTO for incoming request
    private String senderUserId;
    private String receiverUserId;
    private String messageContent;
}