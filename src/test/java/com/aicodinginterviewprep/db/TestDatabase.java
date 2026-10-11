package com.aicodinginterviewprep.db;

import org.flywaydb.core.Flyway;

import java.util.UUID;

/** Fresh in-memory H2 database (MySQL mode) with the real Flyway migrations applied. */
public final class TestDatabase {

    private final String url;

    public TestDatabase() {
        url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    public ConnectionFactory connections() {
        return () -> java.sql.DriverManager.getConnection(url, "sa", "");
    }

    public static ConnectionFactory failing() {
        return () -> {
            throw new java.sql.SQLException("boom");
        };
    }
}
