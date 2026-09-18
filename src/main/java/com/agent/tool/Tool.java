package com.agent.tool;

import java.util.Map;

public interface Tool {
    String name();

    String description();

    default Map<String, Object> parameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(),
                "additionalProperties", false
        );
    }

    String execute(String arguments);
}
