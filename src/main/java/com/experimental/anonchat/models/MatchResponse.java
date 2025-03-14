package com.experimental.anonchat.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchResponse {
    private String chatSessionId;
    private User user1;
    private User user2;
    private boolean matchFound; // Added matchFound boolean
}

