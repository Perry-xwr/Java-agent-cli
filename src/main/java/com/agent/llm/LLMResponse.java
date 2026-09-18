package com.agent.llm;

import java.util.List;

public record LLMResponse(String content, List<ToolCall> toolCalls) {
    public LLMResponse {
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }
}
