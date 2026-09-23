package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.Difficulty;
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
    private static final int MAX_COMPLETION_TOKENS_CODING = 450;
    private static final String CONTENT_FIELD = "content";
    private static final List<String> CODING_DIFFICULTIES = List.of("Easy", "Medium", "Hard");
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 400;

    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final Map<String, String> prompts;
    private final Map<QuestionType, List<String>> topicsByType;
    private final Random random;
    // Medium keeps the existing experience as the default while allowing each screen to override it.
    private volatile Difficulty selectedDifficulty = Difficulty.MEDIUM;

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
            QuestionType.THEORY, extractTopics(prompts, "THEORY_TOPIC_"),
            QuestionType.CODING, extractTopics(prompts, "CODING_TOPIC_"));
        this.random = new Random();
    }

    public String generateQuestion(QuestionType type) throws IOException, InterruptedException {
        return generateQuestion(type, selectedDifficulty);
    }

    public String generateQuestion(QuestionType type, Difficulty difficulty) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "OPENAI_API_KEY is not set. Add it to your local .env file (see .env.example).");
        }

        IOException lastIoFailure = null;
        RuntimeException lastRuntimeFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String question = requestQuestion(type, difficulty);
                if (!question.isBlank()) {
                    return question;
                }
                lastRuntimeFailure = new IllegalStateException("OpenAI returned an empty response.");
            } catch (IOException e) {
                lastIoFailure = e;
                lastRuntimeFailure = null;
            } catch (RuntimeException e) {
                lastRuntimeFailure = e;
                lastIoFailure = null;
            }

            if (attempt < MAX_ATTEMPTS) {
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        if (lastIoFailure != null) {
            throw lastIoFailure;
        }
        throw lastRuntimeFailure;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.selectedDifficulty = difficulty;
    }

    private String requestQuestion(QuestionType type, Difficulty difficulty) throws IOException, InterruptedException {
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("max_completion_tokens", maxCompletionTokensFor(type));
        payload.put("reasoning_effort", "minimal");
        payload.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put(CONTENT_FIELD, systemPromptFor(type, difficulty)))
            .put(new JSONObject().put("role", "user").put(CONTENT_FIELD, buildUserPrompt(type, difficulty))));

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

    String systemPromptFor(QuestionType type) {
        return prompts.getOrDefault(type.name() + "_SYSTEM", prompts.get("SYSTEM"));
    }

    String systemPromptFor(QuestionType type, Difficulty difficulty) {
        return systemPromptFor(type) + " The requested difficulty is " + difficulty
            + "; enforce its expected depth and complexity strictly.";
    }

    int maxCompletionTokensFor(QuestionType type) {
        return type == QuestionType.CODING ? MAX_COMPLETION_TOKENS_CODING : MAX_COMPLETION_TOKENS;
    }

    String buildUserPrompt(QuestionType type) {
        // Retain the package-visible helper's original random coding behavior for existing callers.
        if (type != QuestionType.CODING) {
            return buildUserPrompt(type, selectedDifficulty);
        }

        List<String> topics = topicsByType.get(type);
        String topic = topics.get(random.nextInt(topics.size()));
        String template = prompts.get(type.name() + "_TEMPLATE").replace("{topic}", topic);

        if (type == QuestionType.CODING) {
            String difficulty = CODING_DIFFICULTIES.get(random.nextInt(CODING_DIFFICULTIES.size()));
            template = template.replace("{difficulty}", difficulty);
        }

        return template;
    }

    String buildUserPrompt(QuestionType type, Difficulty difficulty) {
        List<String> topics = topicsByType.get(type);
        String topic = topics.get(random.nextInt(topics.size()));
        String template = prompts.get(type.name() + "_TEMPLATE")
            .replace("{topic}", topic)
            .replace("{difficulty}", difficulty.toString());
        return template;
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
