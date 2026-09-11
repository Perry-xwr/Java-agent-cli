package com.agent.tool;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FileToolsDemo {
    public static void main(String[] args) throws IOException {
        Path root = Path.of(".");

        ListFilesTool listFilesTool = new ListFilesTool();
        ReadFileTool readFileTool = new ReadFileTool();
        SearchCodeTool searchCodeTool = new SearchCodeTool();

        System.out.println("== list_files ==");
        listFilesTool.listFiles(root).stream()
                .limit(20)
                .forEach(System.out::println);

        if (args.length > 0) {
            System.out.println("== read_file " + args[0] + " ==");
            System.out.println(readFileTool.readFile(Path.of(args[0])));
        }

        String keyword = args.length > 1 ? args[1] : "TODO";
        System.out.println("== search_code " + keyword + " ==");
        List<SearchCodeTool.Match> matches = searchCodeTool.searchCode(keyword, root);
        for (SearchCodeTool.Match match : matches) {
            System.out.printf("%s:%d: %s%n", match.file(), match.line(), match.content());
        }
    }
}
