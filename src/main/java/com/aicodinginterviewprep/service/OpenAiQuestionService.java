package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.config.EnvConfig;
import com.aicodinginterviewprep.config.KeyValueFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OpenAiQuestionService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-5-nano";
    private static final String PROMPTS_RESOURCE = "/prompts/promptengineering.txt";
    private static final int MAX_COMPLETION_TOKENS = 100;
    private static final String CONTENT_FIELD = "content";

    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final Map<String, String> prompts;
    private final Map<QuestionType, List<String>> topicsByType;
    private final Random random;

    public OpenAiQuestionService() {
        this(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
            EnvConfig.get("OPENAI_API_KEY"),
            EnvConfig.get("OPENAI_MODEL", DEFAULT_MODEL));
    }

    OpenAiQuestionService(HttpClient httpClient, String apiKey, String model) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.model = model;
        this.prompts = loadPrompts();
        this.topicsByType = Map.of(
            QuestionType.BEHAVIOURAL, extractTopics(prompts, "BEHAVIOURAL_TOPIC_"),
            QuestionType.THEORY, extractTopics(prompts, "THEORY_TOPIC_"));
        this.random = new Random();
    }

    public String generateQuestion(QuestionType type) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "OPENAI_API_KEY is not set. Add it to your local .env file (see .env.example).");
        }

        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("max_completion_tokens", MAX_COMPLETION_TOKENS);
        payload.put("reasoning_effort", "minimal");
        payload.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put(CONTENT_FIELD, prompts.get("SYSTEM")))
            .put(new JSONObject().put("role", "user").put(CONTENT_FIELD, buildUserPrompt(type))));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .timeout(Duration.ofSeconds(30))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                "OpenAI request failed (HTTP " + response.statusCode() + "): " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString(CONTENT_FIELD)
            .trim();
    }

    String buildUserPrompt(QuestionType type) {
        List<String> topics = topicsByType.get(type);
        String topic = topics.get(random.nextInt(topics.size()));
        return prompts.get(type.name() + "_TEMPLATE").replace("{topic}", topic);
    }

    private static List<String> extractTopics(Map<String, String> prompts, String prefix) {
        return prompts.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(prefix))
            .sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().substring(prefix.length()))))
            .map(Map.Entry::getValue)
            .toList();
    }

    private static Map<String, String> loadPrompts() {
        try (InputStream in = OpenAiQuestionService.class.getResourceAsStream(PROMPTS_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing prompts resource: " + PROMPTS_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return KeyValueFile.parse(reader.lines());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read prompts resource: " + PROMPTS_RESOURCE, e);
        }
    }
}
