package com.aicodinginterviewprep.openai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.aicodinginterviewprep.EvaluatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluatorServiceTest {

  @Mock private OpenAiApiClient mockApiClient;

  private EvaluatorService evaluatorService;

  @BeforeEach
  void setUp() {
    // Inject the mock client and a real ObjectMapper into the service
    evaluatorService = new EvaluatorService(mockApiClient, new ObjectMapper());
  }

  @Test
  @DisplayName("evaluateAnswerAsync should parse valid OpenAI response into Record")
  void testEvaluateAnswerAsyncSuccess() throws Exception {
    // Arrange
    // The model response groups each rating with its matching feedback. A null rating means the
    // category cannot be evaluated for this answer, while its explanatory feedback is retained.
    String mockOpenAiResponse =
        """
        {
          "choices": [
            {
              "message": {
                "content": "{\\\"correctness\\\":{\\\"rating\\\":8,\\\"feedback\\\":\\\"This is the correctness feedback!\\\"},\\\"efficiency\\\":{\\\"rating\\\":7,\\\"feedback\\\":\\\"This is the efficiency feedback!\\\"},\\\"communication\\\":{\\\"rating\\\":9,\\\"feedback\\\":\\\"This is the communication feedback!\\\"},\\\"code_quality\\\":{\\\"rating\\\":null,\\\"feedback\\\":\\\"This is the code quality feedback!\\\"}}"
              }
            }
          ]
          }
        }
        """;

    when(mockApiClient.postChatCompletionAsync(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockOpenAiResponse));

    String question = "What is polymorphism?";
    String userAnswer =
        "Polymorphism allows objects to be treated as instances of their parent class.";

    // Act
    CompletableFuture<EvaluationResult> future =
        evaluatorService.evaluateAnswerAsync(question, userAnswer);
    EvaluationResult result = future.get(); // Blocks until completed in test execution

    // Assert
    assertNotNull(result, "Resulting record should not be null");
    assertEquals(8, result.getCorrectnessRating(), "Correctness rating should be parsed correctly");
    assertEquals(
        "This is the correctness feedback!",
        result.getCorrectnessEvaluation(),
        "Correctness feedback should match");

    assertEquals(7, result.getEfficiencyRating(), "Efficiency rating should be parsed correctly");
    assertEquals(
        "This is the efficiency feedback!",
        result.getEfficiencyEvaluation(),
        "Efficiency feedback should match");

    assertEquals(
        9, result.getCommunicationRating(), "Communication rating should be parsed correctly");
    assertEquals(
        "This is the communication feedback!",
        result.getCommunicationEvaluation(),
        "Communication feedback should match");

    assertNull(
        result.getCodeQualityRating(), "Code quality rating should be null when not applicable");
    assertEquals(
        "This is the code quality feedback!",
        result.getCodeQualityEvaluation(),
        "Code quality feedback should match");

    // Verify the API client was called exactly once
    verify(mockApiClient, times(1)).postChatCompletionAsync(anyString());
  }

  @Test
  @DisplayName("evaluateAnswerAsync should handle invalid OpenAI JSON response")
  void testEvaluateAnswerAsyncMalformedJsonResponse() {
    // Arrange: API returns valid outer JSON but invalid inner content payload
    String malformedResponse =
        """
        {
          "choices": [
            {
              "message": {
                "content": "Invalid JSON response from model"
              }
            }
          ]
        }
        """;

    when(mockApiClient.postChatCompletionAsync(anyString()))
        .thenReturn(CompletableFuture.completedFuture(malformedResponse));

    // Act & Assert
    CompletableFuture<EvaluationResult> future =
        evaluatorService.evaluateAnswerAsync("Question", "Answer");

    ExecutionException exception = assertThrows(ExecutionException.class, future::get);
    assertTrue(exception.getCause() instanceof OpenAiApiException);
    assertTrue(exception.getCause().getMessage().contains("Failed to parse OpenAI JSON response"));
  }

  @Test
  @DisplayName("evaluateAnswerAsync should propagate network or API client failures")
  void testEvaluateAnswerAsyncApiClientFailure() {
    // Arrange: Network call throws an exception in the future
    CompletableFuture<String> failedFuture =
        CompletableFuture.failedFuture(new OpenAiApiException("HTTP 500 Internal Server Error"));

    when(mockApiClient.postChatCompletionAsync(anyString())).thenReturn(failedFuture);

    // Act & Assert
    CompletableFuture<EvaluationResult> future =
        evaluatorService.evaluateAnswerAsync("Question", "Answer");

    ExecutionException exception = assertThrows(ExecutionException.class, future::get);
    assertTrue(exception.getCause() instanceof OpenAiApiException);
    assertTrue(exception.getCause().getMessage().contains("HTTP 500 Internal Server Error"));
  }

  @Test
  @DisplayName("Live API Test: Send real request to OpenAI and verify response")
  @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
  void testLiveOpenAiApiCall() throws Exception {
    // Arrange - uses the default constructor with real OpenAiApiClient
    EvaluatorService service = new EvaluatorService();

    String question = "What is the difference between an interface and an abstract class in Java?";
    String userAnswer =
        "An interface defines a contract for behavior with default/static methods, while an"
            + " abstract class can hold instance state and implementation logic. A class can"
            + " implement multiple interfaces but extend only one class.";

    // Act - execute real async API call
    CompletableFuture<EvaluationResult> future = service.evaluateAnswerAsync(question, userAnswer);

    // Wait up to 15 seconds for the network response
    EvaluationResult result = future.get(15, TimeUnit.SECONDS);

    // Assert - verify structure and values returned from OpenAI
    assertNotNull(result, "Response should not be null");

    assertTrue(
      result.getCorrectnessRating() >= 0 && result.getCorrectnessRating() <= 10,
      "Correctness rating should be between 0 and 10");
    assertNotNull(result.getCorrectnessEvaluation(), "Correctness feedback should not be null");
    assertFalse(
      result.getCorrectnessEvaluation().isBlank(), "Correctness feedback should not be blank");
  }
}
