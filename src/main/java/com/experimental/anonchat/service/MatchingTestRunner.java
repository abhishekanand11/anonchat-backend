package com.experimental.anonchat.service;

import com.experimental.anonchat.models.User;
import com.experimental.anonchat.service.impl.MatchingService;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class MatchingTestRunner {

    public static void main(String[] args) {

        // 1. Create a dummy MessageChannel (a mock that does nothing)
        MessageChannel dummyMessageChannel = new MessageChannel() {
            @Override
            public boolean send(org.springframework.messaging.Message<?> message, long timeout) {
                return false;
            }
        };

        // 2. Create a dummy SimpMessagingTemplate, passing the dummy MessageChannel
        SimpMessagingTemplate dummyMessagingTemplate = new SimpMessagingTemplate(dummyMessageChannel);

        MatchingService matchingService = new MatchingService();

        // 1. Create sample users
        User user1 = new User(UUID.randomUUID().toString(), "Alice", 1990, "Female", "Male", Set.of("hiking", "movies", "cooking"), false, "USA", new HashSet<>(), new HashSet<>());
        user1.setInterests(Set.of("hiking", "movies", "cooking"));
        User user2 = new User(UUID.randomUUID().toString(), "Bob", 1988, "Male", "Female", Set.of("movies", "music", "travel"), false, "USA", new HashSet<>(), new HashSet<>());
        user2.setInterests(Set.of("movies", "music", "travel"));
        User user3 = new User(UUID.randomUUID().toString(), "Charlie", 1995, "Male", "Female", Set.of("hiking", "reading", "coding"), false, "Canada", new HashSet<>(), new HashSet<>()); // Different country
        user3.setInterests(Set.of("hiking", "reading", "coding"));
        User user4 = new User(UUID.randomUUID().toString(), "Diana", 1992, "Female", "Female", Set.of("yoga", "movies", "painting"), false, "USA", new HashSet<>(), new HashSet<>()); // Same gender preference, same country
        user4.setInterests(Set.of("yoga", "movies", "painting"));
        User user5 = new User(UUID.randomUUID().toString(), "Eve", 2002, "Female", "Male", Set.of("music", "dancing", "photography"), false, "USA", new HashSet<>(), new HashSet<>()); // Different age group
        user5.setInterests(Set.of("music", "dancing", "photography"));


        // 2. Add users to userDataMap
        matchingService.getUserDataMap().put(user1.getUserId(), user1);
        matchingService.getUserDataMap().put(user2.getUserId(), user2);
        matchingService.getUserDataMap().put(user3.getUserId(), user3);
        matchingService.getUserDataMap().put(user4.getUserId(), user4);
        matchingService.getUserDataMap().put(user5.getUserId(), user5);

        // 3. Add users to waitingQueue (user1, user2, user4, user5 will wait for match)
        matchingService.getWaitingQueue().add(user1.getUserId());
        matchingService.getWaitingQueue().add(user2.getUserId());
        matchingService.getWaitingQueue().add(user4.getUserId());
        matchingService.getWaitingQueue().add(user5.getUserId());
        // Charlie (user3) is NOT added to waitingQueue - should not be matched in this run

        // 4. Run the matching algorithm
        System.out.println("--- Running Matching Algorithm ---");
        matchingService.matchUsersWithStrictFiltersAndCorrectedInterestToggle();
        System.out.println("--- Matching Algorithm Finished ---");

        // 5. (Optional) Inspect chatSessionMap and user activeChatSessionIds if needed for detailed verification
        System.out.println("\nChat Sessions Created:");
        matchingService.getChatSessionMap().forEach((sessionId, chatSession) -> {
            System.out.println("Session ID: " + sessionId + ", Users: " + chatSession.getUser1() + ", " + chatSession.getUser2());
        });

        System.out.println("\nWaiting Queue after matching: " + matchingService.getWaitingQueue());
    }
}