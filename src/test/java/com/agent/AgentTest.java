package com.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentTest {
    private final Agent agent = new Agent();

    @Test
    void respondsWithGreetingForHello() {
        assertEquals("Hello, Agent!", agent.respond("hello"));
    }

    @Test
    void echoesHi() {
        assertEquals("I heard: hi", agent.respond("hi"));
    }

    @Test
    void respondsWithGreetingForWhitespace() {
        assertEquals("Hello, Agent!", agent.respond(" "));
    }

    @Test
    void respondsWithGreetingForUppercaseHello() {
        assertEquals("Hello, Agent!", agent.respond("HELLO"));
    }

    @Test
    void respondsWithGreetingForPaddedHello() {
        assertEquals("Hello, Agent!", agent.respond("  hello  "));
    }

    @Test
    void respondsWithGreetingForNullInput() {
        assertEquals("Hello, Agent!", agent.respond(null));
    }
}
