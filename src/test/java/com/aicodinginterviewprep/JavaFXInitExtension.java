package com.aicodinginterviewprep;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JavaFXInitExtension implements BeforeAllCallback {
    private static boolean initialized = false;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!initialized) {
            new JFXPanel();
            initialized = true;
        }
    }
}