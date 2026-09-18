package com.agent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GlmClient implements LLMClient {
    private static final String API_KEY_ENV = "GLM_API_KEY";
    private static final String DEFAULT_ENDPOINT = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String DEFAULT_MODEL = "glm-4-flash";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiKey;
    private final String endpoint;
    private final String model;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GlmClient() {
        this(requireApiKey(), DEFAULT_ENDPOINT, DEFAULT_MODEL, new OkHttpClient(), new ObjectMapper());
    }

    GlmClient(String apiKey, String endpoint, String model,
              OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.apiKey = requireNonBlank(apiKey, "apiKey");
        this.endpoint = requireNonBlank(endpoint, "endpoint");
        this.model = requireNonBlank(model, "model");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public LLMResponse chat(List<Message> messages) throws IOException {
        return chat(messages, List.of());
    }

    @Override
    public LLMResponse chat(List<Message> messages, List<ToolDefinition> tools) throws IOException {
        Objects.requireNonNull(messages, "messages must not be null");
        Objects.requireNonNull(tools, "tools must not be null");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", List.copyOf(messages));
        if (!tools.isEmpty()) {
            payload.put("tools", tools.stream().map(GlmClient::serializeTool).toList());
        }

        String json = objectMapper.writeValueAsString(payload);
        Request request = new Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(json, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            String responseJson = body == null ? "" : body.string();
            if (!response.isSuccessful()) {
                throw new IOException("GLM request failed with HTTP " + response.code() + ": " + responseJson);
            }
            if (responseJson.isBlank()) {
                throw new IOException("GLM returned an empty response body");
            }
            return parseResponse(responseJson);
        }
    }

    private static Map<String, Object> serializeTool(ToolDefinition tool) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.name());
        function.put("description", tool.description());
        function.put("parameters", tool.parameters());

        Map<String, Object> serialized = new LinkedHashMap<>();
        serialized.put("type", "function");
        serialized.put("function", function);
        return serialized;
    }

    private LLMResponse parseResponse(String responseJson) throws IOException {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IOException("GLM response does not contain a choice");
        }

        JsonNode message = choices.get(0).path("message");
        String content = message.path("content").isNull()
                ? null
                : message.path("content").asText(null);

        List<ToolCall> toolCalls = new ArrayList<>();
        JsonNode calls = message.path("tool_calls");
        if (calls.isArray()) {
            for (JsonNode call : calls) {
                JsonNode function = call.path("function");
                toolCalls.add(new ToolCall(
                        call.path("id").asText(),
                        function.path("name").asText(),
                        function.path("arguments").asText()
                ));
            }
        }
        return new LLMResponse(content, toolCalls);
    }

    private static String requireApiKey() {
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Environment variable " + API_KEY_ENV + " is not set");
        }
        return apiKey;
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
