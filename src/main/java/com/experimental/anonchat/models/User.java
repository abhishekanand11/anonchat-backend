package com.experimental.anonchat.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String userId; // Will store a UUID as a String
    private String name;
    private int birthYear;
    private String gender;
    private String genderPreference;
    private Set<String> interests;
    private boolean isInterestFilterEnabled;
    private String country;
    private Set<String> activeChatSessionIds; // Use a Set to store multiple session IDs
    private Set<String> friendUserIds; // Set to store User IDs of friends


}

