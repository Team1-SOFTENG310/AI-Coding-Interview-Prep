package com.aicodinginterviewprep.db;

import org.flywaydb.core.Flyway;

import java.util.UUID;

/** Fresh in-memory H2 database (MySQL mode) with the real Flyway migrations applied. */
final class TestDatabase {

    private final String url;

    TestDatabase() {
        url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    ConnectionFactory connections() {
        return () -> java.sql.DriverManager.getConnection(url, "sa", "");
    }

    static ConnectionFactory failing() {
        return () -> {
            throw new java.sql.SQLException("boom");
        };
    }
}
