package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.errors.ConfigurationException;
import com.aicodinginterviewprep.errors.NetworkException;
import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.config.EnvConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OpenAiQuestionService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-5-nano";
    private static final String PROMPTS_RESOURCE = "/prompts/promptengineering.json";
    private static final int MAX_COMPLETION_TOKENS = 100;
    private static final int MAX_COMPLETION_TOKENS_CODING = 450;
    private static final String CONTENT_FIELD = "content";
    private static final String TOPIC_SEPARATOR = " - ";
    private static final List<String> CODING_DIFFICULTIES = List.of("Easy", "Medium", "Hard");
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 400;

    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final JSONObject prompts;
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
                QuestionType.BEHAVIOURAL, extractTopics("BEHAVIOURAL_TOPICS"),
                QuestionType.THEORY, extractTopics("THEORY_TOPICS"),
                QuestionType.CODING, extractTopics("CODING_TOPICS"));
        this.random = new Random();
    }

    public String generateQuestion(QuestionType type) throws IOException, InterruptedException {
        return generateQuestion(type, selectedDifficulty);
    }

    public String generateQuestion(QuestionType type, Difficulty difficulty) throws IOException, InterruptedException {
        return generateQuestion(type, difficulty, null);
    }

    public String generateQuestion(QuestionType type, Difficulty difficulty, String topic)
            throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ConfigurationException(
                    "OPENAI_API_KEY is not set. Add it to your local .env file (see .env.example).");
        }

        IOException lastIoFailure = null;
        NetworkException lastNetworkFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String question = requestQuestion(type, difficulty, topic);
                if (!question.isBlank()) {
                    return question;
                }
                lastNetworkFailure = new NetworkException("OpenAI returned an empty response.");
            } catch (IOException e) {
                lastIoFailure = e;
                lastNetworkFailure = null;
            } catch (NetworkException e) {
                lastNetworkFailure = e;
                lastIoFailure = null;
            }

            if (attempt < MAX_ATTEMPTS) {
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        if (lastIoFailure != null) {
            throw new NetworkException("OpenAI question request failed after retries.", lastIoFailure);
        }
        throw lastNetworkFailure;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.selectedDifficulty = difficulty;
    }

    public List<String> getAvailableTopics(QuestionType type) {
        return topicsFor(type).stream()
            .map(OpenAiQuestionService::topicName)
            .toList();
    }

    private String requestQuestion(QuestionType type, Difficulty difficulty, String topic)
            throws IOException, InterruptedException {
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("max_completion_tokens", maxCompletionTokensFor(type));
        payload.put("reasoning_effort", "minimal");
        payload.put("messages", new JSONArray()
                .put(new JSONObject().put("role", "system").put(CONTENT_FIELD, systemPromptFor(type, difficulty)))
                .put(new JSONObject().put("role", "user")
                        .put(CONTENT_FIELD, buildUserPrompt(type, difficulty, topic))));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new NetworkException(
                    "OpenAI request failed (HTTP " + response.statusCode() + "): " + response.body());
        }

        try {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString(CONTENT_FIELD)
                    .trim();
        } catch (JSONException | IndexOutOfBoundsException e) {
            throw new NetworkException("OpenAI returned an invalid question response.", e);
        }
    }

    String systemPromptFor(QuestionType type) {
    return prompts.optString(
            type.name() + "_SYSTEM",
            prompts.getString("SYSTEM"));
}

    String systemPromptFor(QuestionType type, Difficulty difficulty) {
    String difficultyInstruction = prompts
            .getString("DIFFICULTY_INSTRUCTION")
            .replace("{difficulty}", difficulty.toString());

    return systemPromptFor(type) + " " + difficultyInstruction;
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
        String template = prompts.getString(type.name() + "_TEMPLATE").replace("{topic}", topic);

        if (type == QuestionType.CODING) {
            String difficulty = CODING_DIFFICULTIES.get(random.nextInt(CODING_DIFFICULTIES.size()));
            template = template.replace("{difficulty}", difficulty);
        }

        return template;
    }

    String buildUserPrompt(QuestionType type, Difficulty difficulty) {
        return buildUserPrompt(type, difficulty, null);
    }

    String buildUserPrompt(QuestionType type, Difficulty difficulty, String topic) {
        String promptTopic = resolveTopic(type, topic);
        String template = prompts.getString(type.name() + "_TEMPLATE")
                .replace("{topic}", promptTopic)
                .replace("{difficulty}", difficulty.toString());
        return template;
    }

    private String resolveTopic(QuestionType type, String topic) {
        List<String> topics = topicsFor(type);
        if (topic == null) {
            return topics.get(random.nextInt(topics.size()));
        }

        return topics.stream()
            .filter(configuredTopic -> topicName(configuredTopic).equals(topic))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown " + type.toString().toLowerCase() + " topic: " + topic));
    }

    private List<String> topicsFor(QuestionType type) {
        List<String> topics = topicsByType.get(type);
        if (topics == null || topics.isEmpty()) {
            throw new IllegalArgumentException("No topics configured for question type: " + type);
        }
        return topics;
    }

    private static String topicName(String topic) {
        int separatorIndex = topic.indexOf(TOPIC_SEPARATOR);
        return separatorIndex < 0 ? topic : topic.substring(0, separatorIndex);
    }

    private List<String> extractTopics(String key) {
        JSONArray array = prompts.getJSONArray(key);

        List<String> topics = new java.util.ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            topics.add(array.getString(i));
        }

    return topics;
}

    private static JSONObject loadPrompts() {
    try (InputStream in =
            OpenAiQuestionService.class.getResourceAsStream(PROMPTS_RESOURCE)) {

        if (in == null) {
            throw new ConfigurationException(
                    "Missing prompts resource: " + PROMPTS_RESOURCE);
        }

        String json = new String(
                in.readAllBytes(),
                StandardCharsets.UTF_8);

        return new JSONObject(json);

    } catch (IOException | JSONException e) {
        throw new ConfigurationException(
                "Failed to read prompts resource: " + PROMPTS_RESOURCE, e);
    }
}
}
