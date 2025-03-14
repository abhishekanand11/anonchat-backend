package com.experimental.anonchat.controller;

import com.experimental.anonchat.models.ChatMessage;
import com.experimental.anonchat.service.impl.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class WebSocketMessageController {
    @Autowired
    private MatchingService matchingService;


    @MessageMapping("/chat.sendMessage") // Clients will send messages to /app/chat.sendMessage
    public void sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("WebSocket message received at /chat.sendMessage: " + chatMessage);
        matchingService.storeAndPublishMessage(chatMessage); // Delegate message storing and publishing to service
    }
}