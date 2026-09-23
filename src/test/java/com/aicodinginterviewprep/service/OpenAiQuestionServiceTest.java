package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.Difficulty;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiQuestionServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void generateQuestionReturnsTrimmedContentFromA200Response() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(
            "{\"choices\":[{\"message\":{\"content\":\"  What is a hash map?  \"}}]}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(response);

        OpenAiQuestionService service = new OpenAiQuestionService(httpClient, "fake-key", "gpt-5-nano");

        assertEquals("What is a hash map?", service.generateQuestion(QuestionType.THEORY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateQuestionThrowsOnNon200Response() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(403);
        when(response.body()).thenReturn("{\"error\":\"forbidden\"}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(response);

        OpenAiQuestionService service = new OpenAiQuestionService(httpClient, "fake-key", "gpt-5-nano");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.generateQuestion(QuestionType.BEHAVIOURAL));
        assertTrue(exception.getMessage().contains("403"));
        verify(httpClient, times(3)).send(any(HttpRequest.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateQuestionRetriesOnEmptyContentThenSucceeds() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> emptyResponse = mock(HttpResponse.class);
        when(emptyResponse.statusCode()).thenReturn(200);
        when(emptyResponse.body()).thenReturn("{\"choices\":[{\"message\":{\"content\":\"\"}}]}");

        HttpResponse<String> goodResponse = mock(HttpResponse.class);
        when(goodResponse.statusCode()).thenReturn(200);
        when(goodResponse.body()).thenReturn(
            "{\"choices\":[{\"message\":{\"content\":\"What is a hash map?\"}}]}");

        when(httpClient.<String>send(any(HttpRequest.class), any()))
            .thenReturn(emptyResponse, goodResponse);

        OpenAiQuestionService service = new OpenAiQuestionService(httpClient, "fake-key", "gpt-5-nano");

        assertEquals("What is a hash map?", service.generateQuestion(QuestionType.THEORY));
        verify(httpClient, times(2)).send(any(HttpRequest.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateQuestionThrowsAfterExhaustingRetriesOnRepeatedEmptyContent() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> emptyResponse = mock(HttpResponse.class);
        when(emptyResponse.statusCode()).thenReturn(200);
        when(emptyResponse.body()).thenReturn("{\"choices\":[{\"message\":{\"content\":\"   \"}}]}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(emptyResponse);

        OpenAiQuestionService service = new OpenAiQuestionService(httpClient, "fake-key", "gpt-5-nano");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.generateQuestion(QuestionType.CODING));
        assertTrue(exception.getMessage().toLowerCase().contains("empty"));
        verify(httpClient, times(3)).send(any(HttpRequest.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateQuestionRetriesAfterTransientIOExceptionThenSucceeds() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> goodResponse = mock(HttpResponse.class);
        when(goodResponse.statusCode()).thenReturn(200);
        when(goodResponse.body()).thenReturn(
            "{\"choices\":[{\"message\":{\"content\":\"What is a hash map?\"}}]}");

        when(httpClient.<String>send(any(HttpRequest.class), any()))
            .thenThrow(new IOException("connection reset"))
            .thenReturn(goodResponse);

        OpenAiQuestionService service = new OpenAiQuestionService(httpClient, "fake-key", "gpt-5-nano");

        assertEquals("What is a hash map?", service.generateQuestion(QuestionType.THEORY));
        verify(httpClient, times(2)).send(any(HttpRequest.class), any());
    }

    @Test
    void generateQuestionThrowsWhenApiKeyIsMissing() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), null, "gpt-5-nano");

        assertThrows(IllegalStateException.class, () -> service.generateQuestion(QuestionType.THEORY));
    }

    @Test
    void generateQuestionThrowsWhenApiKeyIsBlank() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "   ", "gpt-5-nano");

        assertThrows(IllegalStateException.class, () -> service.generateQuestion(QuestionType.THEORY));
    }

    @Test
    void buildUserPromptFillsInATheoryTopicArea() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        String prompt = service.buildUserPrompt(QuestionType.THEORY);

        assertTrue(prompt.contains("Focus specifically on this topic area:"));
    }

    @Test
    void buildUserPromptRotatesAcrossMultipleBehaviouralTopics() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(service.buildUserPrompt(QuestionType.BEHAVIOURAL));
        }

        assertTrue(seen.size() > 1, "expected topic rotation to produce more than one distinct prompt");
    }

    @Test
    void buildUserPromptRotatesAcrossMultipleTheoryTopics() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(service.buildUserPrompt(QuestionType.THEORY));
        }

        assertTrue(seen.size() > 1, "expected topic rotation to produce more than one distinct prompt");
    }

    @Test
    void buildUserPromptFillsInACodingTopicArea() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        String prompt = service.buildUserPrompt(QuestionType.CODING);

        assertTrue(prompt.contains("Focus specifically on this topic area:"));
    }

    @Test
    void buildUserPromptRotatesAcrossMultipleCodingTopics() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(service.buildUserPrompt(QuestionType.CODING));
        }

        assertTrue(seen.size() > 1, "expected topic rotation to produce more than one distinct prompt");
    }

    @Test
    void buildUserPromptAlwaysFillsInACodingDifficulty() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        for (int i = 0; i < 20; i++) {
            String prompt = service.buildUserPrompt(QuestionType.CODING);
            assertFalse(prompt.contains("{difficulty}"), "difficulty placeholder should always be filled in: " + prompt);
            assertTrue(
                prompt.contains("Easy difficulty") || prompt.contains("Medium difficulty") || prompt.contains("Hard difficulty"),
                "expected an explicit difficulty in the prompt: " + prompt
            );
        }

    }

    @Test
    void buildUserPromptUsesSelectedDifficultyForBehaviouralQuestions() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        String prompt = service.buildUserPrompt(QuestionType.BEHAVIOURAL, Difficulty.HARD);

        assertTrue(prompt.contains("at exactly Hard difficulty"));
        assertFalse(prompt.contains("{difficulty}"));
    }

    @Test
    void buildUserPromptUsesSelectedDifficultyForTheoryQuestions() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        String prompt = service.buildUserPrompt(QuestionType.THEORY, Difficulty.EASY);

        assertTrue(prompt.contains("at exactly Easy difficulty"));
        assertFalse(prompt.contains("{difficulty}"));
    }

    @Test
    void buildUserPromptUsesSelectedDifficultyForCodingQuestions() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        String prompt = service.buildUserPrompt(QuestionType.CODING, Difficulty.HARD);

        assertTrue(prompt.contains("at exactly Hard difficulty"));
        assertTrue(prompt.contains("Difficulty: Hard"));
        assertFalse(prompt.contains("{difficulty}"));
    }

    @Test
    void setDifficultyChangesDefaultGenerationPromptDifficulty() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        service.setDifficulty(Difficulty.EASY);

        assertTrue(service.buildUserPrompt(QuestionType.THEORY).contains("at exactly Easy difficulty"));
    }

    @Test
    void buildUserPromptRotatesAcrossAllThreeCodingDifficulties() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        Set<String> seenDifficulties = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String prompt = service.buildUserPrompt(QuestionType.CODING);
            if (prompt.contains("Easy difficulty")) {
                seenDifficulties.add("Easy");
            } else if (prompt.contains("Medium difficulty")) {
                seenDifficulties.add("Medium");
            } else if (prompt.contains("Hard difficulty")) {
                seenDifficulties.add("Hard");
            }
        }

        assertEquals(Set.of("Easy", "Medium", "Hard"), seenDifficulties);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateQuestionForCodingReturnsFullLeetCodeStyleContent() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(
            "{\"choices\":[{\"message\":{\"content\":\"Title: Two Sum\\nDifficulty: Easy\"}}]}");
        when(httpClient.<String>send(any(HttpRequest.class), any())).thenReturn(response);

        OpenAiQuestionService service = new OpenAiQuestionService(httpClient, "fake-key", "gpt-5-nano");

        assertEquals("Title: Two Sum\nDifficulty: Easy", service.generateQuestion(QuestionType.CODING));
    }

    @Test
    void systemPromptForCodingUsesDedicatedCodingSystemPrompt() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        String codingSystemPrompt = service.systemPromptFor(QuestionType.CODING);
        String theorySystemPrompt = service.systemPromptFor(QuestionType.THEORY);

        assertTrue(codingSystemPrompt.contains("LeetCode-style"));
        assertFalse(codingSystemPrompt.equals(theorySystemPrompt), "coding should use its own system prompt");
    }

    @Test
    void systemPromptForNonCodingTypesFallsBackToSharedSystemPrompt() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        assertEquals(
            service.systemPromptFor(QuestionType.BEHAVIOURAL),
            service.systemPromptFor(QuestionType.THEORY)
        );
    }

    @Test
    void maxCompletionTokensForCodingIsLargerThanDefault() {
        OpenAiQuestionService service = new OpenAiQuestionService(mock(HttpClient.class), "key", "model");

        assertTrue(service.maxCompletionTokensFor(QuestionType.CODING)
            > service.maxCompletionTokensFor(QuestionType.THEORY));
    }
}
