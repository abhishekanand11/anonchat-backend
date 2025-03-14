package com.experimental.anonchat.service.impl;

import com.experimental.anonchat.models.ChatMessage;
import com.experimental.anonchat.models.ChatSession;
import com.experimental.anonchat.models.MatchResponse;
import com.experimental.anonchat.models.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class MatchingService {
    @Getter
    private final Set<String> waitingQueue = new ConcurrentSkipListSet<>(); // Changed to Set
    @Getter
    private final Map<String, User> userDataMap = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, ChatSession> chatSessionMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private SimpUserRegistry simpUserRegistry;


    @Async
    public void matchUsersWithStrictFiltersAndCorrectedInterestToggle() {
        System.out.println("--- Running Matching Algorithm in Background (Async) ---");
        List<String> usersToMatch = new ArrayList<>(waitingQueue); // Create a copy to avoid concurrent modification issues

        for (int i = 0; i < usersToMatch.size(); i++) {
            String user1Id = usersToMatch.get(i);

            for (int j = i + 1; j < usersToMatch.size(); j++) {
                String user2Id = usersToMatch.get(j);

                // Retrieve User objects from userDataMap inside the matching logic
                User user1 = userDataMap.get(user1Id);
                User user2 = userDataMap.get(user2Id);

                if (user1 != null && user2 != null) {
                    if (areUsersStrictlyMatching(user1, user2)) {
                        String chatSessionId = UUID.randomUUID().toString();
                        chatSessionMap.put(chatSessionId, new ChatSession(chatSessionId, user1, user2));
                        user1.getActiveChatSessionIds().add(chatSessionId);
                        user2.getActiveChatSessionIds().add(chatSessionId);
                        waitingQueue.remove(user1Id);
                        waitingQueue.remove(user2Id);
                        notifyUsersOfMatch(user1Id, user2Id);
                        return; // Early exit after finding the first match (as per your original logic)
                    }
                } else {
                    if (user1 == null) System.err.println("Error: User object is null for userId: " + user1Id + " in matching algorithm (areUsersStrictlyMatching).");
                    if (user2 == null) System.err.println("Error: User object is null for userId: " + user2Id + " in matching algorithm (areUsersStrictlyMatching).");
                }
            }
        }
        System.out.println("--- Background Matching Algorithm Finished (Async) ---");
    }

    @Async
    public void enqueueUser(User user) {
        String userId = user.getUserId();
        System.out.println("Received enqueue request for user: " + userId);

        boolean addedToQueue = waitingQueue.add(userId); // Try to add to Set
        if (addedToQueue) {
            userDataMap.put(userId, user);
            System.out.println("User enqueued for matching. Please check /chat/getMatch for a match.");
            matchUsersWithStrictFiltersAndCorrectedInterestToggle(); // Start matching algorithm asynchronously
        } else {
            System.out.println("Duplicate enqueue request received for user: " + userId + ". User is already in the waiting queue (Set). Ignoring request.");
        }
    }

    public void dequeueUser(String userId) {
        boolean removed = waitingQueue.remove(userId); // Use Set's remove
        chatSessionMap.entrySet().removeIf(entry -> entry.getValue().getUser1().equals(userId) || entry.getValue().getUser2().equals(userId));
        if (removed) {
            System.out.println("User dequeued successfully");
        } else {
            System.out.println("User not found in waiting queue for dequeue operation.");
        }
    }


    public ChatSession getChatSession(String sessionId) {
        return chatSessionMap.get(sessionId);
    }

    public MatchResponse getMatchForUser(String userId) {
        ChatSession chatSession = null;
        User requestingUser = userDataMap.get(userId);

        if (requestingUser != null) {
            for (String activeChatSessionId : requestingUser.getActiveChatSessionIds()) {
                ChatSession session = chatSessionMap.get(activeChatSessionId);
                if (session != null) {
                    chatSession = session;
                    break;
                }
            }
        }

        if (chatSession != null) {
            // Match found in existing sessions
            User matchedUser = null;
            if (chatSession.getUser1().getUserId().equalsIgnoreCase(userId)) {
                matchedUser = chatSession.getUser2();

                System.out.println("Returning user: " + requestingUser.getName()
                        + " match with : " + matchedUser.getUserId()
                        + " chatSessionId: " + chatSession.getChatSessionId());

                return new MatchResponse(chatSession.getChatSessionId(), requestingUser, matchedUser, true);
            } else if (chatSession.getUser2().getUserId().equalsIgnoreCase(userId)){
                matchedUser = chatSession.getUser1();

                System.out.println("Returning user: " + requestingUser.getName()
                        + " match with : " + matchedUser.getUserId()
                        + " chatSessionId: " + chatSession.getChatSessionId());

                return new MatchResponse(chatSession.getChatSessionId(), requestingUser, matchedUser, true);
            }
            return new MatchResponse(null, requestingUser, null, false);
        } else {
            // No match found in existing sessions.
            // Re-trigger matching algorithm *only if user is still in waiting queue*
            if (waitingQueue.contains(userId)) { // Check if user is still waiting
                matchUsersWithStrictFiltersAndCorrectedInterestToggle(); // Re-trigger matching
            }
            // No match found at this time
            return new MatchResponse(null, requestingUser, null, false);
        }
    }


    private boolean areUsersStrictlyMatching(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return false; // Handle null users defensively
        }

        // Country Filter (users are NOT from the same country) - Modified to NOT be from same country
        boolean countryMatch = user1.getCountry().equalsIgnoreCase(user2.getCountry());
        if (!countryMatch) {
            return false; // If country doesn't match, immediately return false (early exit)
        }

        // Gender Filter:  (User1's preference matches User2's gender and vice versa)
        boolean genderMatch = isGenderMatchStrict(user1, user2);
        if (!genderMatch) {
            return false; // If gender doesn't match, immediately return false (early exit)
        }

        // Age Compatibility (Scoring - using compatibility score, not strict boolean)
        double ageScore = calculateAgeCompatibilityScore(user1.getBirthYear(), user2.getBirthYear());
        boolean ageCompatible = ageScore >= 0.4; // Consider "Moderate" age compatibility
        if (!ageCompatible) {
            return false; // If age is not compatible, immediately return false (early exit) - Optional early exit for age
        }

        // Interest Match (at least one common interest)
        return calculateInterestOverlapScore(user1.getInterests(), user2.getInterests()) > 0; // If interests don't match, immediately return false (early exit) - Optional early exit for interests
    }


    private boolean isGenderMatchStrict(User user1, User user2) {
        String pref1 = user1.getGenderPreference();
        String pref2 = user2.getGenderPreference();
        String gender1 = user1.getGender();
        String gender2 = user2.getGender();

        if (pref1 == null || pref2 == null || gender1 == null || gender2 == null) {
            return false; // Handle cases where gender or preference is not set
        }

        boolean user1PrefersUser2 = pref1.equalsIgnoreCase("Both") || pref1.equalsIgnoreCase(gender2);
        boolean user2PrefersUser1 = pref2.equalsIgnoreCase("Both") || pref2.equalsIgnoreCase(gender1);

        return user1PrefersUser2 && user2PrefersUser1;
    }


    private double calculateAgeCompatibilityScore(int birthYear1, int birthYear2) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int age1 = currentYear - birthYear1;
        int age2 = currentYear - birthYear2;
        int ageDiff = Math.abs(age1 - age2);

        if (ageDiff <= 5) {
            return 1.0; // Excellent age compatibility
        } else if (ageDiff <= 10) {
            return 0.7; // Good age compatibility
        } else if (ageDiff <= 20) {
            return 0.4; // Moderate age compatibility
        } else {
            return 0.1; // Low age compatibility
        }
    }


    private double calculateInterestOverlapScore(Set<String> interests1, Set<String> interests2) {
        if (interests1 == null || interests2 == null || interests1.isEmpty() || interests2.isEmpty()) {
            return 1.0; // No score if either user has no interests or interest data is missing
        }

        int commonInterests = 0;
        for (String interest : interests1) {
            if (interests2.contains(interest)) {
                commonInterests++;
            }
        }
        // Calculate Jaccard index-like score: common interests / total unique interests
        return (double) commonInterests / (interests1.size() + interests2.size() - commonInterests);
    }

    private void notifyUsersOfMatch(String user1Id, String user2Id) {
        // Placeholder for notification logic (e.g., WebSocket, Push Notification)
        System.out.println("Notification: Users matched! " + user1Id + " and " + user2Id);
        User user1 = userDataMap.get(user1Id);
        User user2 = userDataMap.get(user2Id);
        if (user1 != null) {
            System.out.println("Notifying user: " + user1.getName() + " (ID: " + user1Id + ") of match with " + user2Id);
        }
        if (user2 != null) {
            System.out.println("Notifying user: " + user2.getName() + " (ID: " + user2Id + ") of match with " + user1Id);
        }
    }

    public void storeAndPublishMessage(ChatMessage message) {
        ChatSession session = chatSessionMap.get(message.getChatSessionId());
        if (session != null) {
            session.getMessages().add(message); // Store message in ChatSession

            System.out.println("Message stored in session " + message.getChatSessionId() + ": " + message);

            // --- WebSocket Publishing ---
            String receiverUserId = message.getRecipientId();
            String chatSessionId = message.getChatSessionId();


            String destination = "/queue/chat." + chatSessionId;
            System.out.println("Sending message to destination: " + destination);
            messagingTemplate.convertAndSendToUser(receiverUserId, destination, message); // Publish to user's chat-specific queue
        } else {
            System.err.println("Error: Chat session not found when trying to store message: " + message.getChatSessionId());
        }
    }

    public List<ChatMessage> getChatMessagesForSession(String chatId) {
        ChatSession session = chatSessionMap.get(chatId); // Retrieve ChatSession from map
        if (session != null) {
            return session.getMessages(); // Return messages from ChatSession if session exists
        } else {
            return Collections.emptyList(); // Return empty list if session not found
        }
    }
}
