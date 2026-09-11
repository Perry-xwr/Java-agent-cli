package com.agent.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchCodeToolTest {
    @TempDir
    Path tempDir;

    private final SearchCodeTool tool = new SearchCodeTool();

    @Test
    void findsAllMatchesInRootFileWithLineNumbersAndTrimmedContent() throws IOException {
        Path file = tempDir.resolve("Example.java");
        Files.writeString(file, "first line\n  keyword first  \nno match\nkeyword second\n");

        List<SearchCodeTool.Match> matches = tool.searchCode("keyword", tempDir);

        assertEquals(List.of(
                new SearchCodeTool.Match(Path.of("Example.java").toString(), 2, "keyword first"),
                new SearchCodeTool.Match(Path.of("Example.java").toString(), 4, "keyword second")
        ), matches);
    }

    @Test
    void searchesSubdirectoriesAndReturnsRelativeFilePath() throws IOException {
        Path nested = Files.createDirectories(tempDir.resolve("src").resolve("nested"));
        Files.writeString(nested.resolve("Nested.java"), "find-me\n");

        List<SearchCodeTool.Match> matches = tool.searchCode("find-me", tempDir);

        assertEquals(List.of(new SearchCodeTool.Match(
                Path.of("src", "nested", "Nested.java").toString(),
                1,
                "find-me"
        )), matches);
    }

    @Test
    void skipsKnownBinaryAndNonCodeFiles() throws IOException {
        for (String fileName : List.of("Example.class", "image.png", "photo.jpg", "library.jar", ".DS_Store")) {
            Files.writeString(tempDir.resolve(fileName), "hidden-keyword");
        }

        assertTrue(tool.searchCode("hidden-keyword", tempDir).isEmpty());
    }

    @Test
    void returnsEmptyListWhenKeywordIsNotFound() throws IOException {
        Files.writeString(tempDir.resolve("Example.java"), "class Example {}\n");

        assertTrue(tool.searchCode("missing-keyword", tempDir).isEmpty());
    }

    @Test
    void skipsUnreadableTextFileAndContinuesSearching() throws IOException {
        Files.write(tempDir.resolve("invalid.txt"), new byte[]{(byte) 0xC3, (byte) 0x28});
        Files.writeString(tempDir.resolve("valid.txt"), "target\n");

        assertEquals(List.of(new SearchCodeTool.Match(
                Path.of("valid.txt").toString(),
                1,
                "target"
        )), tool.searchCode("target", tempDir));
    }
}
