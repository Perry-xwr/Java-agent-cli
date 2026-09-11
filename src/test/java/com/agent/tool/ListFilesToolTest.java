package com.agent.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListFilesToolTest {
    @TempDir
    Path tempDir;

    private final ListFilesTool tool = new ListFilesTool();

    @Test
    void listsFilesRecursivelyAsSortedRelativePaths() throws IOException {
        Path subdirectory = Files.createDirectory(tempDir.resolve("nested"));
        Files.createFile(tempDir.resolve("root.txt"));
        Files.createFile(subdirectory.resolve("child.txt"));

        List<String> files = tool.listFiles(tempDir);

        assertEquals(List.of(
                Path.of("nested", "child.txt").toString(),
                Path.of("root.txt").toString()
        ), files);
        assertFalse(files.contains(Path.of("nested").toString()));
        assertTrue(files.stream().map(Path::of).noneMatch(Path::isAbsolute));
    }
}
