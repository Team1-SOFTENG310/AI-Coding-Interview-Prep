package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.QuestionType;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
}
