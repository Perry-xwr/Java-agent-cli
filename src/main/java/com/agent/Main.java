package com.agent;

import com.agent.llm.GlmClient;
import com.agent.tool.ToolRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        com.agent.agent.Agent agent = new com.agent.agent.Agent(
                new GlmClient(),
                ToolRegistry.withFileTools(Path.of("."))
        );

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("clear")) {
                    agent.clearHistory();
                    System.out.println("History cleared.");
                    continue;
                }
                System.out.println(agent.run(line));
            }
        }
    }
}
