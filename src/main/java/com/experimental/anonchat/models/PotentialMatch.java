package com.experimental.anonchat.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotentialMatch {
    private String user1Id; // ID of the "current user" in the matching loop
    private String potentialMatchUserId; // ID of the potential match user
    private double matchScore;
}
