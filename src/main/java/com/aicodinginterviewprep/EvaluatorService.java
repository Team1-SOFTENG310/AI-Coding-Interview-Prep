package com.aicodinginterviewprep;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.aicodinginterviewprep.openai.OpenAiApiClient;
import com.aicodinginterviewprep.openai.OpenAiConfig;
import com.aicodinginterviewprep.openai.EvaluationResult;

import java.util.concurrent.CompletableFuture;

public class EvaluatorService {

    private final OpenAiApiClient apiClient;
    private final ObjectMapper objectMapper;

    public EvaluatorService() {
        this(new OpenAiApiClient(), new ObjectMapper());
    }

    public EvaluatorService(OpenAiApiClient apiClient, ObjectMapper objectMapper) {
      this.apiClient = apiClient;
      this.objectMapper = objectMapper;
    }

    /**
     * Sends user answer and interview question for evaluation.
     */
    public CompletableFuture<EvaluationResult> evaluateAnswerAsync(String question, String userAnswer) {
        try {
            String jsonRequestBody = buildRequestBody(question, userAnswer);

            return apiClient.postChatCompletionAsync(jsonRequestBody)
                    .thenApply(this::parseResponseContent);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private String buildRequestBody(String question, String userAnswer) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", OpenAiConfig.DEFAULT_MODEL);
        root.put("temperature", OpenAiConfig.DEFAULT_TEMPERATURE);
        root.put("top_p", OpenAiConfig.DEFAULT_TOP_P);
        root.put("max_completion_tokens", OpenAiConfig.DEFAULT_MAX_COMPLETION_TOKENS);
        root.put("thinkingLevel", OpenAiConfig.DEFAULT_THINKING_LEVEL);

        // can be extended to include more structured response formats if needed
        ObjectNode responseFormat = root.putObject("response_format");
        responseFormat.put("type", "json_object");

        ArrayNode messages = root.putArray("messages");

        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
        You are an expert software engineering interviewer. \
        Evaluate the candidate's response and respond strictly in JSON format. \
        The JSON object MUST contain the following fields:
        - "rating": (integer) A score from 1 to 10 evaluating the candidate's answer.
        - "evaluation": (string) Concise, constructive feedback detailing strengths and weaknesses.\
        """);

        // User payload combining question and response
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", "Interview Question: " + question + "\nCandidate Answer: " + userAnswer);

        return objectMapper.writeValueAsString(root);
    }

    /**
     * Parses the OpenAI API response to extract the evaluation content.
     */
    private EvaluationResult parseResponseContent(String rawJsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawJsonResponse);
            String content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            JsonNode evalJson = objectMapper.readTree(content);
            int rating = evalJson.path("rating").asInt();
            String evaluation = evalJson.path("evaluation").asText();

            return new EvaluationResult(evaluation, rating);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI JSON response", e);
        }
    }
}