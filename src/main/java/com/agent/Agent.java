package com.agent;

public class Agent {
    public String respond(String input) {
        if (input == null || input.isBlank() || input.trim().equalsIgnoreCase("hello")) {
            return "Hello, Agent!";
        }

        return "I heard: " + input;
    }
}
