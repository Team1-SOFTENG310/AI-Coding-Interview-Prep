package com.aicodinginterviewprep.openai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.aicodinginterviewprep.config.EnvConfig;

public class OpenAiApiClient {

    private final HttpClient httpClient;

    public OpenAiApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends raw JSON asynchronously to the OpenAI Chat Completions API.
     */
    public CompletableFuture<String> postChatCompletionAsync(String jsonPayload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OpenAiConfig.API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + EnvConfig.get("OPENAI_API_KEY"))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new OpenAiApiException(response.statusCode(), response.body());
                    }
                    return response.body();
                });
    }
}