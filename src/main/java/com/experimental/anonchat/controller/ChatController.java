package com.experimental.anonchat.controller;

import com.experimental.anonchat.models.ChatMessage;
import com.experimental.anonchat.models.ChatSession;
import com.experimental.anonchat.models.User;
import com.experimental.anonchat.service.impl.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final MatchingService matchingService;

    @Autowired
    public ChatController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/enqueue") // Endpoint to enqueue a user for matching
    public ResponseEntity<String> enqueueUser(@RequestBody User user) {
        System.out.println("Received enqueue request for user: " + user.getUserId()); // Log received userId
        matchingService.enqueueUser(user);
        return new ResponseEntity<>("User enqueued for matching. Please check /chat/getMatch for a match.", HttpStatus.OK);
    }

    @PostMapping("/dequeue")
    public ResponseEntity<String> dequeueUser(@RequestBody String userId) { // Accepts userId as request body
        matchingService.dequeueUser(userId);
        return new ResponseEntity<>("User dequeued successfully", HttpStatus.OK);
    }

    @GetMapping("/getMatch")
    public ResponseEntity<?> getMatchForUser(@RequestParam("userId") String userId) { // Accepts userId as a query parameter
        return new ResponseEntity<>(matchingService.getMatchForUser(userId), HttpStatus.OK);
    }


    @GetMapping("/messages/{chatId}") // New endpoint to fetch chat messages
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable String chatId) {
        List<ChatMessage> messages = matchingService.getChatMessagesForSession(chatId);
        if (messages != null) {
            return new ResponseEntity<>(messages, HttpStatus.OK); // 200 and return messages
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 if chat session or messages not found
        }
    }

}
