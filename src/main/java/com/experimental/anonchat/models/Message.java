package com.experimental.anonchat.models;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String senderId;
    private String receiverId;
    private String chatId;
    private String content;
    private long timestamp;
}

