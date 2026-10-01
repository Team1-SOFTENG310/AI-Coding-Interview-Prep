package com.aicodinginterviewprep.db;

import com.aicodinginterviewprep.errors.ConfigurationException;
import com.aicodinginterviewprep.errors.PersistenceException;
import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

public final class DatabaseMigrator {

    private static final Dotenv ENV = Dotenv.configure()
            .ignoreIfMissing()   // lets real environment variables work (e.g. in CI)
            .load();

    private DatabaseMigrator() {}

    public static void migrate() {
        String url = "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false"
                .formatted(require("DB_HOST"), require("DB_PORT"), require("DB_NAME"));

        try {
            Flyway.configure()
                    .dataSource(url, require("DB_USER"), require("DB_PASSWORD"))
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
        } catch (FlywayException exception) {
            throw new PersistenceException("Database migration failed", exception);
        }
    }

    private static String require(String key) {
        String value = ENV.get(key);   // checks real env vars and .env
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(
                    "Missing config: " + key + " (copy .env.example to .env)");
        }
        return value;
    }
}