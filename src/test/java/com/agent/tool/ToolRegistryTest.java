package com.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRegistryTest {
    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registersAndFindsAllFileTools() {
        ToolRegistry registry = ToolRegistry.withFileTools(tempDir);

        assertEquals("list_files", registry.getTool("list_files").name());
        assertEquals("read_file", registry.getTool("read_file").name());
        assertEquals("search_code", registry.getTool("search_code").name());
        assertEquals(3, registry.definitions().size());
        assertTrue(registry.definitions().stream().anyMatch(
                definition -> definition.name().equals("read_file")
                        && definition.parameters().toString().contains("path")
        ));
    }

    @Test
    void executesAllFileTools() throws IOException {
        Files.writeString(tempDir.resolve("Example.java"), "class Example { // needle\n}\n");
        ToolRegistry registry = ToolRegistry.withFileTools(tempDir);

        JsonNode listedFiles = objectMapper.readTree(registry.execute("list_files", "{}"));
        assertEquals("Example.java", listedFiles.get(0).asText());

        assertEquals(
                "class Example { // needle\n}\n",
                registry.execute("read_file", "{\"path\":\"Example.java\"}")
        );

        JsonNode matches = objectMapper.readTree(registry.execute(
                "search_code",
                "{\"keyword\":\"needle\"}"
        ));
        assertEquals("Example.java", matches.get(0).path("file").asText());
        assertEquals(1, matches.get(0).path("line").asInt());
        assertEquals("class Example { // needle", matches.get(0).path("content").asText());
    }

    @Test
    void registersAndExecutesCustomTool() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new Tool() {
            @Override
            public String name() {
                return "echo";
            }

            @Override
            public String description() {
                return "Echoes its arguments.";
            }

            @Override
            public String execute(String arguments) {
                return arguments;
            }
        });

        assertEquals("hello", registry.execute("echo", "hello"));
    }

    @Test
    void rejectsUnknownTool() {
        ToolRegistry registry = new ToolRegistry();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registry.execute("missing", "{}")
        );
        assertTrue(exception.getMessage().contains("missing"));
    }
}
