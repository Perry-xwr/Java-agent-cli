package com.agent.llm;

import java.util.Objects;

public record Message(String role, String content) {
    public Message {
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(content, "content must not be null");
    }

    public static Message user(String content) {
        return new Message("user", content);
    }
}
