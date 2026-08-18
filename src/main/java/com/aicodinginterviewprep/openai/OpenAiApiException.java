package com.aicodinginterviewprep.openai;

/**
 * Custom exception representing errors occurring during OpenAI API calls or response processing.
 */
public class OpenAiApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public OpenAiApiException(String message) {
        super(message);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public OpenAiApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public OpenAiApiException(int statusCode, String responseBody) {
        super("OpenAI API call failed with status " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public OpenAiApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public OpenAiApiException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
