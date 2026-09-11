package com.agent.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadFileToolTest {
    @TempDir
    Path tempDir;

    private final ReadFileTool tool = new ReadFileTool();

    @Test
    void readsTextFileWithContentUnchanged() throws IOException {
        String expected = "Hello, Agent!\n你好，Agent！";
        Path file = tempDir.resolve("message.txt");
        Files.writeString(file, expected, StandardCharsets.UTF_8);

        String actual = tool.readFile(file);

        assertEquals(expected, actual);
    }

    @Test
    void throwsNoSuchFileExceptionWhenFileDoesNotExist() {
        Path missing = tempDir.resolve("missing.txt");

        assertThrows(NoSuchFileException.class, () -> tool.readFile(missing));
    }

    @Test
    void throwsIOExceptionWhenPathIsDirectory() {
        assertThrows(IOException.class, () -> tool.readFile(tempDir));
    }
}
