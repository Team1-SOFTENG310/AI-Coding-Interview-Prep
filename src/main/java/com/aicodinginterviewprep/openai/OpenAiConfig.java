package com.aicodinginterviewprep.openai;

/**
 * Configuration class for OpenAI API settings.
 */
public class OpenAiConfig {
    public static final String API_URL = "https://api.openai.com/v1/chat/completions";
    public static final String DEFAULT_MODEL = "gpt-4o-mini";
    public static final double DEFAULT_TEMPERATURE = 0.7;

    public static String getApiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set.");
        }
        return key;
    }
}