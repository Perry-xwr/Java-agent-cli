package com.agent.tool;

import com.agent.llm.ToolDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ToolRegistry {
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public static ToolRegistry withFileTools(Path root) {
        Objects.requireNonNull(root, "root must not be null");

        ToolRegistry registry = new ToolRegistry();
        ObjectMapper objectMapper = new ObjectMapper();
        registry.register(new ListFilesAdapter(root, objectMapper));
        registry.register(new ReadFileAdapter(root, objectMapper));
        registry.register(new SearchCodeAdapter(root, objectMapper));
        return registry;
    }

    public void register(Tool tool) {
        Objects.requireNonNull(tool, "tool must not be null");
        String name = requireNonBlank(tool.name(), "tool name");
        if (tools.putIfAbsent(name, tool) != null) {
            throw new IllegalArgumentException("Tool is already registered: " + name);
        }
    }

    public Tool getTool(String name) {
        Tool tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        return tool;
    }

    public String execute(String name, String arguments) {
        return getTool(name).execute(arguments);
    }

    public List<ToolDefinition> definitions() {
        return tools.values().stream()
                .map(tool -> new ToolDefinition(
                        tool.name(),
                        tool.description(),
                        tool.parameters()
                ))
                .toList();
    }

    private abstract static class FileToolAdapter implements Tool {
        protected final Path root;

        private FileToolAdapter(Path root) {
            this.root = root;
        }

        protected Path resolve(String value) {
            return root.resolve(Path.of(value)).normalize();
        }

        protected IllegalStateException executionFailed(IOException exception) {
            return new IllegalStateException("Tool " + name() + " failed: " + exception.getMessage(), exception);
        }
    }

    private static final class ListFilesAdapter extends FileToolAdapter {
        private final ListFilesTool delegate = new ListFilesTool();
        private final ObjectMapper objectMapper;

        private ListFilesAdapter(Path root, ObjectMapper objectMapper) {
            super(root);
            this.objectMapper = objectMapper;
        }

        @Override
        public String name() {
            return "list_files";
        }

        @Override
        public String description() {
            return "Recursively lists regular files under a path relative to the workspace root.";
        }

        @Override
        public Map<String, Object> parameters() {
            return objectSchema(
                    Map.of("path", stringProperty("Optional path relative to the workspace root.")),
                    List.of()
            );
        }

        @Override
        public String execute(String arguments) {
            try {
                JsonNode input = parseArguments(objectMapper, arguments);
                String path = input.path("path").asText(".");
                return objectMapper.writeValueAsString(delegate.listFiles(resolve(path)));
            } catch (IOException exception) {
                throw executionFailed(exception);
            }
        }
    }

    private static final class ReadFileAdapter extends FileToolAdapter {
        private final ReadFileTool delegate = new ReadFileTool();
        private final ObjectMapper objectMapper;

        private ReadFileAdapter(Path root, ObjectMapper objectMapper) {
            super(root);
            this.objectMapper = objectMapper;
        }

        @Override
        public String name() {
            return "read_file";
        }

        @Override
        public String description() {
            return "Reads a UTF-8 text file at a path relative to the workspace root.";
        }

        @Override
        public Map<String, Object> parameters() {
            return objectSchema(
                    Map.of("path", stringProperty("File path relative to the workspace root.")),
                    List.of("path")
            );
        }

        @Override
        public String execute(String arguments) {
            try {
                JsonNode input = parseArguments(objectMapper, arguments);
                String path = requireNonBlank(input.path("path").asText(null), "path");
                return delegate.readFile(resolve(path));
            } catch (IOException exception) {
                throw executionFailed(exception);
            }
        }
    }

    private static final class SearchCodeAdapter extends FileToolAdapter {
        private final SearchCodeTool delegate = new SearchCodeTool();
        private final ObjectMapper objectMapper;

        private SearchCodeAdapter(Path root, ObjectMapper objectMapper) {
            super(root);
            this.objectMapper = objectMapper;
        }

        @Override
        public String name() {
            return "search_code";
        }

        @Override
        public String description() {
            return "Searches text files recursively for lines containing a keyword.";
        }

        @Override
        public Map<String, Object> parameters() {
            return objectSchema(
                    Map.of(
                            "keyword", stringProperty("Exact keyword to search for."),
                            "path", stringProperty("Optional path relative to the workspace root.")
                    ),
                    List.of("keyword")
            );
        }

        @Override
        public String execute(String arguments) {
            try {
                JsonNode input = parseArguments(objectMapper, arguments);
                String keyword = requireNonBlank(input.path("keyword").asText(null), "keyword");
                String path = input.path("path").asText(".");
                return objectMapper.writeValueAsString(delegate.searchCode(keyword, resolve(path)));
            } catch (IOException exception) {
                throw executionFailed(exception);
            }
        }
    }

    private static JsonNode parseArguments(ObjectMapper objectMapper, String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode parsed = objectMapper.readTree(arguments);
            if (!parsed.isObject()) {
                throw new IllegalArgumentException("Tool arguments must be a JSON object");
            }
            return parsed;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid tool arguments JSON", exception);
        }
    }

    private static Map<String, Object> objectSchema(
            Map<String, Object> properties,
            List<String> required
    ) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    private static Map<String, Object> stringProperty(String description) {
        return Map.of(
                "type", "string",
                "description", description
        );
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
