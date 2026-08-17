package com.aicodinginterviewprep.openai;

/**
 * Configuration class for OpenAI API settings.
 */
public class OpenAiConfig {
    private OpenAiConfig() {
        /* This utility class should not be instantiated */
    }

    public static final String API_URL = "https://api.openai.com/v1/chat/completions";
    public static final String DEFAULT_MODEL = "gpt-5-nano";
    public static final double DEFAULT_TEMPERATURE = 1;
    public static final double DEFAULT_TOP_P = 1.0;
    public static final int DEFAULT_MAX_COMPLETION_TOKENS = 4000; // Limits response length for fast evaluation
    public static final int REQUEST_TIMEOUT_SECONDS = 30;

    // /**
    //  * Retrieves the OpenAI API key from environment variables.
    //  * 
    //  * @return String OpenAI API Key
    //  * @throws IllegalStateException if OPENAI_API_KEY is missing or empty
    //  */
    // public static String getApiKey() {
    //     String key = System.getenv("OPENAI_API_KEY");
    //     if (key == null || key.isBlank()) {
    //         throw new IllegalStateException("OPENAI_API_KEY environment variable is not set.");
    //     }
    //     return key;
    // }
}