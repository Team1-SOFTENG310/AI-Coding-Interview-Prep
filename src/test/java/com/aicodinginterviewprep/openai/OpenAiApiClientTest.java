package com.aicodinginterviewprep.openai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAiApiClientTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    @Test
    @DisplayName("Default constructor instantiates OpenAiApiClient successfully")
    void testDefaultConstructor() {
        OpenAiApiClient client = new OpenAiApiClient();
        assertNotNull(client, "Default constructor should initialize client");
    }

    @Test
    @DisplayName("postChatCompletionAsync returns response body on HTTP 200")
    @SuppressWarnings("unchecked")
    void testPostChatCompletionAsyncSuccess() throws Exception {
        String expectedResponseBody = "{\"choices\": [{\"message\": {\"content\": \"Hello World\"}}]}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(expectedResponseBody);
        when(mockHttpClient.<String>sendAsync(any(HttpRequest.class), any()))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        OpenAiApiClient client = new OpenAiApiClient(mockHttpClient);
        CompletableFuture<String> future = client.postChatCompletionAsync("{\"model\":\"gpt-5-nano\"}");

        String actualResponseBody = future.get(5, TimeUnit.SECONDS);

        assertEquals(expectedResponseBody, actualResponseBody);
        verify(mockHttpClient, times(1)).sendAsync(any(HttpRequest.class), any());
    }

    @Test
    @DisplayName("postChatCompletionAsync constructs HttpRequest with proper URI, headers, method, and timeout")
    @SuppressWarnings("unchecked")
    void testPostChatCompletionAsyncRequestAttributes() throws Exception {
        String jsonPayload = "{\"model\":\"gpt-5-nano\",\"messages\":[]}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{}");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.<String>sendAsync(requestCaptor.capture(), any()))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        OpenAiApiClient client = new OpenAiApiClient(mockHttpClient);
        client.postChatCompletionAsync(jsonPayload).get(5, TimeUnit.SECONDS);

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest);
        assertEquals(URI.create(OpenAiConfig.API_URL), capturedRequest.uri());
        assertEquals("POST", capturedRequest.method());

        assertTrue(capturedRequest.headers().firstValue("Content-Type").isPresent());
        assertEquals("application/json", capturedRequest.headers().firstValue("Content-Type").get());

        assertTrue(capturedRequest.headers().firstValue("Authorization").isPresent());
        assertTrue(capturedRequest.headers().firstValue("Authorization").get().startsWith("Bearer "));

        assertTrue(capturedRequest.timeout().isPresent());
        assertEquals(Duration.ofSeconds(30), capturedRequest.timeout().get());
    }

    @Test
    @DisplayName("postChatCompletionAsync throws OpenAiApiException on non-200 HTTP status codes")
    @SuppressWarnings("unchecked")
    void testPostChatCompletionAsyncNon200StatusCodes() {
        List<Integer> errorStatusCodes = List.of(400, 401, 403, 404, 429, 500, 503);

        for (int statusCode : errorStatusCodes) {
            HttpClient clientHttp = mock(HttpClient.class);
            HttpResponse<String> response = mock(HttpResponse.class);

            String errorBody = "{\"error\": {\"message\": \"API error for code " + statusCode + "\"}}";
            when(response.statusCode()).thenReturn(statusCode);
            when(response.body()).thenReturn(errorBody);
            when(clientHttp.<String>sendAsync(any(HttpRequest.class), any()))
                    .thenReturn(CompletableFuture.completedFuture(response));

            OpenAiApiClient client = new OpenAiApiClient(clientHttp);
            CompletableFuture<String> future = client.postChatCompletionAsync("{\"model\":\"gpt-5-nano\"}");

            ExecutionException exception = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
            assertInstanceOf(OpenAiApiException.class, exception.getCause());

            OpenAiApiException apiException = (OpenAiApiException) exception.getCause();
            assertEquals(statusCode, apiException.getStatusCode());
            assertEquals(errorBody, apiException.getResponseBody());
            assertTrue(apiException.getMessage().contains(String.valueOf(statusCode)));
            assertTrue(apiException.getMessage().contains(errorBody));
        }
    }

    @Test
    @DisplayName("postChatCompletionAsync propagates network and IO failures")
    @SuppressWarnings("unchecked")
    void testPostChatCompletionAsyncNetworkFailure() {
        IOException networkException = new IOException("Connection reset by peer");
        when(mockHttpClient.<String>sendAsync(any(HttpRequest.class), any()))
                .thenReturn(CompletableFuture.failedFuture(networkException));

        OpenAiApiClient client = new OpenAiApiClient(mockHttpClient);
        CompletableFuture<String> future = client.postChatCompletionAsync("{\"model\":\"gpt-5-nano\"}");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(IOException.class, exception.getCause());
        assertEquals("Connection reset by peer", exception.getCause().getMessage());
    }
}
