package com.aicodinginterviewprep;

import com.aicodinginterviewprep.errors.ConfigurationException;
import com.aicodinginterviewprep.openai.EvaluationResult;
import com.aicodinginterviewprep.openai.OpenAiApiClient;
import com.aicodinginterviewprep.openai.OpenAiApiException;
import com.aicodinginterviewprep.openai.OpenAiConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class EvaluatorService {

  private final OpenAiApiClient apiClient;
  private final ObjectMapper objectMapper;
  private final String evaluationPrompt;

  public EvaluatorService() {
    this(new OpenAiApiClient(), new ObjectMapper());
  }

  public EvaluatorService(OpenAiApiClient apiClient, ObjectMapper objectMapper) {
    this.apiClient = apiClient;
    this.objectMapper = objectMapper;
    this.evaluationPrompt = loadEvaluationPrompt();
  }

  /** Sends user answer and interview question for evaluation. */
  public CompletableFuture<EvaluationResult> evaluateAnswerAsync(
      String question, String userAnswer) {
    try {
      String jsonRequestBody = buildRequestBody(question, userAnswer);

      return apiClient
          .postChatCompletionAsync(jsonRequestBody)
          .thenApply(this::parseResponseContent);
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  private String buildRequestBody(String question, String userAnswer)
      throws JsonProcessingException {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("model", OpenAiConfig.DEFAULT_MODEL);
    root.put("temperature", OpenAiConfig.DEFAULT_TEMPERATURE);
    root.put("top_p", OpenAiConfig.DEFAULT_TOP_P);
    root.put("max_completion_tokens", OpenAiConfig.DEFAULT_MAX_COMPLETION_TOKENS);

    // can be extended to include more structured response formats if needed
    ObjectNode responseFormat = root.putObject("response_format");
    responseFormat.put("type", "json_object");

    ArrayNode messages = root.putArray("messages");

    ObjectNode systemMessage = messages.addObject();
    systemMessage.put("role", "system");
    systemMessage.put("content", evaluationPrompt);

    // User payload combining question and response
    ObjectNode userMessage = messages.addObject();
    userMessage.put("role", "user");
    userMessage.put(
        "content", "Interview Question: " + question + "\nCandidate Answer: " + userAnswer);

    return objectMapper.writeValueAsString(root);
  }

  /** Parses the OpenAI API response to extract the evaluation content. */
  private EvaluationResult parseResponseContent(String rawJsonResponse) {
    try {
      JsonNode root = objectMapper.readTree(rawJsonResponse);
      // OpenAI wraps the model's JSON feedback inside choices[0].message.content.
      String content = root.path("choices").get(0).path("message").path("content").asText();

      JsonNode evalJson = objectMapper.readTree(content);
      // Each feedback category now contains its rating and feedback together.
      JsonNode correctness = evalJson.path("correctness");
      JsonNode efficiency = evalJson.path("efficiency");
      JsonNode communication = evalJson.path("communication");
      JsonNode codeQuality = evalJson.path("code_quality");

      Integer correctness_rating = nullableInt(correctness.path("rating"));
      Integer efficiency_rating = nullableInt(efficiency.path("rating"));
      Integer communication_rating = nullableInt(communication.path("rating"));
      Integer code_quality_rating = nullableInt(codeQuality.path("rating"));

      String correctness_evaluation = correctness.path("feedback").asText();
      String efficiency_evaluation = efficiency.path("feedback").asText();
      String communication_evaluation = communication.path("feedback").asText();
      String code_quality_evaluation = codeQuality.path("feedback").asText();

      return new EvaluationResult(
          correctness_rating,
          efficiency_rating,
          communication_rating,
          code_quality_rating,
          correctness_evaluation,
          efficiency_evaluation,
          communication_evaluation,
          code_quality_evaluation);
    } catch (JsonProcessingException | IndexOutOfBoundsException | NullPointerException e) {
      throw new OpenAiApiException("Failed to parse OpenAI JSON response", e);
    }
  }

  private Integer nullableInt(JsonNode node) {
    // Preserve an explicit JSON null instead of converting it to the primitive value 0.
    return node.isNull() || node.isMissingNode() ? null : node.asInt();
  }

  private String loadEvaluationPrompt() {
    try (InputStream inputStream =
            getClass().getResourceAsStream("/prompts/evaluation.json")) {

        if (inputStream == null) {
            throw new ConfigurationException("evaluation.json not found");
        }

        JsonNode root = objectMapper.readTree(inputStream);
        ArrayNode lines = (ArrayNode) root.get("EVALUATION_SYSTEM");

        StringBuilder prompt = new StringBuilder();

        for (JsonNode line : lines) {
            prompt.append(line.asText()).append("\n");
        }

        return prompt.toString();

    } catch (IOException e) {
        throw new ConfigurationException("Failed to load evaluation.json", e);
    }
}
}
