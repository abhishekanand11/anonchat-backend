package com.experimental.anonchat.service.impl;

public class IdGenerator {
    private static long lastTimestamp = System.currentTimeMillis();
    private static long counter = 0;

    public static synchronized long generateId() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp == lastTimestamp) {
            counter++;
        } else {
            lastTimestamp = currentTimestamp;
            counter = 0;
        }
        return (currentTimestamp << 16) | counter; // Shifting timestamp to leave space for counter
    }
}

