package com.agent.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class ReadFileTool {
    public String readFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new NoSuchFileException(path.toString());
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a regular file: " + path);
        }

        return Files.readString(path);
    }
}
