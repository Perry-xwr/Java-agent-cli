package com.agent.agent;

import com.agent.llm.GlmClient;
import com.agent.llm.LLMClient;
import com.agent.llm.LLMResponse;
import com.agent.llm.Message;
import com.agent.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentTest {
    @TempDir
    Path tempDir;

    @Test
    void returnsAnswerWithoutCallingToolForOrdinaryQuestion() throws IOException {
        FakeLLMClient llmClient = new FakeLLMClient(List.of(
                new LLMResponse("Java 17 是一个长期支持版本。", List.of())
        ));
        Agent agent = new Agent(llmClient, new ToolRegistry());

        String answer = agent.run("介绍一下Java17");

        assertEquals("Java 17 是一个长期支持版本。", answer);
        assertEquals(1, llmClient.callCount());
        assertTrue(agent.history().stream().anyMatch(
                message -> message.role().equals("user") && message.content().equals("介绍一下Java17")
        ));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GLM_API_KEY", matches = ".+")
    void realGlmCallsReadFileToolAndReturnsFinalAnswer() throws IOException {
        Files.writeString(tempDir.resolve("README.md"), "Agent CLI README live tool marker");
        Agent agent = new Agent(new GlmClient(), ToolRegistry.withFileTools(tempDir));

        String answer = agent.run("读取README.md");

        assertTrue(!answer.isBlank());
        assertTrue(agent.history().stream().anyMatch(
                message -> message.role().equals("tool")
                        && message.content().equals("Agent CLI README live tool marker")
                        && message.toolCallId() != null
        ));
        assertTrue(agent.history().stream().anyMatch(
                message -> message.role().equals("assistant")
                        && message.toolCalls().stream().anyMatch(
                                call -> call.get("function").toString().contains("read_file")
                        )
        ));
    }

    private static final class FakeLLMClient implements LLMClient {
        private final Deque<LLMResponse> responses;
        private final List<List<Message>> requests = new ArrayList<>();

        private FakeLLMClient(List<LLMResponse> responses) {
            this.responses = new ArrayDeque<>(responses);
        }

        @Override
        public LLMResponse chat(List<Message> messages) {
            requests.add(List.copyOf(messages));
            return responses.removeFirst();
        }

        private int callCount() {
            return requests.size();
        }

        private List<List<Message>> requests() {
            return List.copyOf(requests);
        }
    }
}
