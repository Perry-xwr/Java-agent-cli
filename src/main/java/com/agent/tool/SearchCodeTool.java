package com.agent.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class SearchCodeTool {
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".class", ".png", ".jpg", ".jar"
    );

    public record Match(String file, int line, String content) {
    }

    public List<Match> searchCode(String keyword, Path root) throws IOException {
        List<Path> files;
        try (Stream<Path> paths = Files.walk(root)) {
            files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !isIgnored(path))
                    .sorted()
                    .toList();
        }

        List<Match> matches = new ArrayList<>();
        for (Path file : files) {
            searchFile(keyword, root, file, matches);
        }
        return matches;
    }

    private void searchFile(String keyword, Path root, Path file, List<Match> matches) {
        List<Match> fileMatches = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String content;
            int lineNumber = 0;
            while ((content = reader.readLine()) != null) {
                lineNumber++;
                if (content.contains(keyword)) {
                    fileMatches.add(new Match(
                            root.relativize(file).toString(),
                            lineNumber,
                            content.trim()
                    ));
                }
            }
            matches.addAll(fileMatches);
        } catch (IOException ignored) {
            // A single unreadable file must not prevent searching the remaining files.
        }
    }

    private boolean isIgnored(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.equals(".DS_Store")) {
            return true;
        }

        String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
        return IGNORED_EXTENSIONS.stream().anyMatch(lowerCaseName::endsWith);
    }
}
