package com.aicodinginterviewprep;

import com.aicodinginterviewprep.openai.EvaluationResult;
import com.aicodinginterviewprep.openai.OpenAiApiClient;
import com.aicodinginterviewprep.openai.OpenAiApiException;
import com.aicodinginterviewprep.openai.OpenAiConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    systemMessage.put(
        "content",
        """
        You are an expert software engineering interviewer grading a candidate's interview response. \
        Respond strictly in JSON format. The JSON object MUST contain the following fields:
        - "correctness": an object containing:
            - "rating": an integer from 0 to 10, or null if there isn't enough information to evaluate it.
            - "feedback": concise feedback explaining the score, or "Not applicable: insufficient information to evaluate this category."

        - "efficiency": an object containing:
            - "rating": an integer from 0 to 10, or null if there isn't enough information to evaluate it.
            - "feedback": concise feedback explaining the score, or "Not applicable: insufficient information to evaluate this category."

        - "communication": an object containing:
            - "rating": an integer from 0 to 10, or null if there isn't enough information to evaluate it.
            - "feedback": concise feedback explaining the score, or "Not applicable: insufficient information to evaluate this category."

        - "code_quality": an object containing:
            - "rating": an integer from 0 to 10, or null if there isn't enough information to evaluate it.
            - "feedback": concise feedback explaining the score, or "Not applicable: insufficient information to evaluate this category."

        Grading rubric:
            CORRECTNESS
                - 0-1: The answer is blank, irrelevant, or fundamentally incorrect.
                - 2-4: Shows some understanding but contains major technical errors or misunderstandings.
                - 5-6: Generally correct but contains notable errors, omissions, or misunderstandings.
                - 7-8: Correct and relevant with only minor inaccuracies or missing details.
                - 9-10: Accurate, complete, and technically precise.

            EFFICIENCY
                Evaluate whether the candidate's proposed solution uses appropriate time and space complexity and avoids unnecessary work.
                - 0-1: No valid solution or extremely inefficient approach.
                - 2-4: Major inefficiencies or inappropriate algorithm/data structure choices.
                - 5-6: Reasonable approach but has notable unnecessary work or complexity issues.
                - 7-8: Efficient approach with minor opportunities for improvement.
                - 9-10: Highly efficient and uses appropriate algorithms and data structures.

            COMMUNICATION
                Evaluate how clearly and logically the candidate explains their reasoning.
                - 0-1: No explanation, incoherent, or impossible to follow.
                - 2-4: Difficult to follow and lacks clear reasoning.
                - 5-6: Understandable but vague, incomplete, or poorly structured.
                - 7-8: Clear and logically structured with minor gaps.
                - 9-10: Clear, concise, well-structured, and demonstrates strong technical reasoning.

            CODE QUALITY
                If the candidate provides code:
                - Evaluate readability, naming, structure, maintainability, and appropriate use of language features.
                - 0-1: Code is fundamentally unusable.
                - 2-4: Code has major readability, structure, or maintainability problems.
                - 5-6: Code is functional but has noticeable quality issues.
                - 7-8: Code is clean and maintainable with minor issues.
                - 9-10: Code is clean, well-structured, readable, maintainable, and follows good software engineering practices.

        IMPORTANT:
        - Judge each category independently.
        - Do not give credit simply because the candidate wrote a long answer.
        - Base scores only on evidence present in the candidate's response.
        - Do not invent information that the candidate did not provide.
        - Provide specific, constructive feedback for each category.
        - If a category is not applicable to the candidate's response, set its "rating" to null and its "feedback" to "Not applicable: insufficient information to evaluate this category."
        - Do not assign a score of 0 simply because a category is not applicable.
        - Only assign a rating from 0 to 10 when there is sufficient evidence to evaluate that category.
        """);

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
    } catch (Exception e) {
      throw new OpenAiApiException("Failed to parse OpenAI JSON response", e);
    }
  }

  private Integer nullableInt(JsonNode node) {
    // Preserve an explicit JSON null instead of converting it to the primitive value 0.
    return node.isNull() || node.isMissingNode() ? null : node.asInt();
  }
}
