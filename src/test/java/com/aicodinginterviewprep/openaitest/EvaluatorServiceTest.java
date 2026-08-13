package com.aicodinginterviewprep.openaitest;

import com.aicodinginterviewprep.EvaluatorService;
import com.aicodinginterviewprep.openai.OpenAiApiClient;
import com.aicodinginterviewprep.openai.EvaluationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluatorServiceTest {

    @Mock
    private OpenAiApiClient mockApiClient;

    private EvaluatorService evaluatorService;

    @BeforeEach
    void setUp() {
        // Inject the mock client and a real ObjectMapper into the service
        evaluatorService = new EvaluatorService(mockApiClient, new ObjectMapper());
    }

    @Test
    @DisplayName("evaluateAnswerAsync should parse valid OpenAI response into Record")
    void testEvaluateAnswerAsync_Success() throws Exception {
        // Arrange
        String mockOpenAiResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\\"rating\\": 8, \\"evaluation\\": \\"Great understanding of polymorphism!\\"}"
                  }
                }
              ]
            }
            """;

        when(mockApiClient.postChatCompletionAsync(anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockOpenAiResponse));

        String question = "What is polymorphism?";
        String userAnswer = "Polymorphism allows objects to be treated as instances of their parent class.";

        // Act
        CompletableFuture<EvaluationResult> future = evaluatorService.evaluateAnswerAsync(question, userAnswer);
        EvaluationResult result = future.get(); // Blocks until completed in test execution

        // Assert
        assertNotNull(result, "Resulting record should not be null");
        assertEquals(8, result.getRating(), "Rating should be parsed correctly");
        assertEquals("Great understanding of polymorphism!", result.getEvaluation(), "Evaluation text should match");
        
        // Verify the API client was called exactly once
        verify(mockApiClient, times(1)).postChatCompletionAsync(anyString());
    }

    @Test
    @DisplayName("evaluateAnswerAsync should handle invalid OpenAI JSON response")
    void testEvaluateAnswerAsync_MalformedJsonResponse() {
        // Arrange: API returns valid outer JSON but invalid inner content payload
        String malformedResponse = """
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
        CompletableFuture<EvaluationResult> future = evaluatorService.evaluateAnswerAsync("Question", "Answer");

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertTrue(exception.getCause().getMessage().contains("Failed to parse OpenAI JSON response"));
    }

    @Test
    @DisplayName("evaluateAnswerAsync should propagate network or API client failures")
    void testEvaluateAnswerAsync_ApiClientFailure() {
        // Arrange: Network call throws an exception in the future
        CompletableFuture<String> failedFuture = CompletableFuture.failedFuture(
                new RuntimeException("HTTP 500 Internal Server Error")
        );

        when(mockApiClient.postChatCompletionAsync(anyString())).thenReturn(failedFuture);

        // Act & Assert
        CompletableFuture<EvaluationResult> future = evaluatorService.evaluateAnswerAsync("Question", "Answer");

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause().getMessage().contains("HTTP 500 Internal Server Error"));
    }

    @Test
    @DisplayName("Live API Test: Send real request to OpenAI and verify response")
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void testLiveOpenAiApiCall() throws Exception {
        // Arrange - uses the default constructor with real OpenAiApiClient
        EvaluatorService service = new EvaluatorService();
        
        String question = "What is the difference between an interface and an abstract class in Java?";
        String userAnswer = "An interface defines a contract for behavior with default/static methods, while an abstract class can hold instance state and implementation logic. A class can implement multiple interfaces but extend only one class.";

        // Act - execute real async API call
        CompletableFuture<EvaluationResult> future = service.evaluateAnswerAsync(question, userAnswer);
        
        // Wait up to 15 seconds for the network response
        EvaluationResult result = future.get(15, TimeUnit.SECONDS);

        // Assert - verify structure and values returned from OpenAI
        assertNotNull(result, "Response should not be null");
        assertTrue(result.getRating() >= 1 && result.getRating() <= 10, "Rating should be between 1 and 10");
        assertNotNull(result.getEvaluation(), "Evaluation text should not be null");
        assertFalse(result.getEvaluation().isBlank(), "Evaluation text should not be blank");

        // Print output to console so you can inspect OpenAI's evaluation
        System.out.println("==========================================");
        System.out.println("LIVE OPENAI RESPONSE RECEIVED SUCCESSFULLY");
        System.out.println("Rating: " + result.getRating() + "/10");
        System.out.println("Evaluation:\n" + result.getEvaluation());
        System.out.println("==========================================");
    }
}