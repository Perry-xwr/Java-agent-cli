package com.agent.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Message(
        String role,
        String content,
        @JsonProperty("tool_calls") List<Map<String, Object>> toolCalls,
        @JsonProperty("tool_call_id") String toolCallId
) {
    public Message {
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(content, "content must not be null");
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public Message(String role, String content) {
        this(role, content, List.of(), null);
    }

    public static Message user(String content) {
        return new Message("user", content);
    }

    public static Message system(String content) {
        return new Message("system", content);
    }

    public static Message assistant(String content) {
        return new Message("assistant", content);
    }

    public static Message assistant(String content, List<ToolCall> toolCalls) {
        List<Map<String, Object>> serializedCalls = toolCalls.stream()
                .map(Message::serializeToolCall)
                .toList();
        return new Message("assistant", content, serializedCalls, null);
    }

    public static Message tool(String toolCallId, String content) {
        return new Message(
                "tool",
                content,
                List.of(),
                Objects.requireNonNull(toolCallId, "toolCallId must not be null")
        );
    }

    private static Map<String, Object> serializeToolCall(ToolCall toolCall) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", toolCall.name());
        function.put("arguments", toolCall.arguments());

        Map<String, Object> serialized = new LinkedHashMap<>();
        serialized.put("id", toolCall.id());
        serialized.put("type", "function");
        serialized.put("function", function);
        return serialized;
    }
}
