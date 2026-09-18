package com.agent.agent;

import com.agent.llm.LLMClient;
import com.agent.llm.LLMResponse;
import com.agent.llm.Message;
import com.agent.llm.ToolCall;
import com.agent.tool.ToolRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Agent {
    public static final int MAX_ITERATIONS = 5;

    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are a helpful coding assistant. Use tools when they are needed to inspect files.";

    private final LLMClient llmClient;
    private final ToolRegistry toolRegistry;
    private final List<Message> history = new ArrayList<>();

    public Agent(LLMClient llmClient, ToolRegistry toolRegistry) {
        this(llmClient, toolRegistry, DEFAULT_SYSTEM_PROMPT);
    }

    public Agent(LLMClient llmClient, ToolRegistry toolRegistry, String systemPrompt) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient must not be null");
        this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry must not be null");
        history.add(Message.system(Objects.requireNonNull(systemPrompt, "systemPrompt must not be null")));
    }

    public String run(String input) throws IOException {
        history.add(Message.user(Objects.requireNonNull(input, "input must not be null")));

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            LLMResponse response = llmClient.chat(
                    List.copyOf(history),
                    toolRegistry.definitions()
            );
            String content = response.content() == null ? "" : response.content();

            List<ToolCall> toolCalls = response.toolCalls();
            history.add(Message.assistant(content, toolCalls));
            if (toolCalls.isEmpty()) {
                return content;
            }

            for (ToolCall toolCall : toolCalls) {
                String result = executeTool(toolCall);
                history.add(Message.tool(toolCall.id(), result));
            }
        }

        throw new IllegalStateException("Agent exceeded maximum iterations: " + MAX_ITERATIONS);
    }

    public List<Message> history() {
        return List.copyOf(history);
    }

    private String executeTool(ToolCall toolCall) {
        try {
            return toolRegistry.execute(toolCall.name(), toolCall.arguments());
        } catch (RuntimeException exception) {
            return "Tool " + toolCall.name() + " failed: " + exception.getMessage();
        }
    }
}
