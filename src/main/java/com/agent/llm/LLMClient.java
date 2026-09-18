package com.agent.llm;

import java.io.IOException;
import java.util.List;

public interface LLMClient {
    LLMResponse chat(List<Message> messages) throws IOException;
}
