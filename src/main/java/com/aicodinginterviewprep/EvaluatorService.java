package com.aicodinginterviewprep;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.aicodinginterviewprep.openai.*;

import java.util.concurrent.CompletableFuture;

public class EvaluatorService {

    private final OpenAiApiClient apiClient;
    private final ObjectMapper objectMapper;

    public EvaluatorService() {
        this.apiClient = new OpenAiApiClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends user answer and interview question for evaluation.
     */
    public CompletableFuture<String> evaluateAnswerAsync(String question, String userAnswer) {
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

        ArrayNode messages = root.putArray("messages");

        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an expert software engineering interviewer. " +
                "Evaluate the candidate's response to the given question concisely. " +
                "Provide constructive feedback, highlight strengths/weaknesses, and grade out of 10.");

        // User payload combining question and response
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", "Interview Question: " + question + "\nCandidate Answer: " + userAnswer);

        return objectMapper.writeValueAsString(root);
    }

    /**
     * Parses the OpenAI API response to extract the evaluation content.
     */
    private String parseResponseContent(String rawJsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawJsonResponse);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI JSON response", e);
        }
    }
}