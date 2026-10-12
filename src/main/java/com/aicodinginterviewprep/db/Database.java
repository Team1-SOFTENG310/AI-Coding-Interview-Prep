package com.aicodinginterviewprep.db;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static final Dotenv ENV = Dotenv.configure()
            .ignoreIfMissing()   // lets real environment variables work (e.g. in CI)
            .load();

    private Database() {}

    public static String url() {
        return "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false"
                .formatted(require("DB_HOST"), require("DB_PORT"), require("DB_NAME"));
    }

    public static String user() {
        return require("DB_USER");
    }

    public static String password() {
        return require("DB_PASSWORD");
    }

    public static Connection open() throws SQLException {
        return DriverManager.getConnection(url(), user(), password());
    }

    private static String require(String key) {
        String value = ENV.get(key);   // checks real env vars and .env
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing config: " + key + " (copy .env.example to .env)");
        }
        return value;
    }
}
