package com.aicodinginterviewprep.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.mysql.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base class for tests that need a real MySQL database.
 *
 * <p>All subclasses share one container, so data written by one test is visible
 * to the next. Tests must clean up after themselves or tolerate existing rows.
 */
public abstract class DatabaseTestBase {
    protected static final MySQLContainer MYSQL =
            new MySQLContainer("mysql:8.4")
            .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");

    // Started manually (not via @Container) so all test classes share one container
    // instead of starting a new one per class (makes the tests run faster).
    // Removed automatically on JVM exit.
    static {
        MYSQL.start();
    }

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    // Empties the tables before each test so tests don't see rows left behind by
    // earlier ones. All test classes share one container, so the database is not
    // reset between tests otherwise. TRUNCATE also resets AUTO_INCREMENT, so ids
    // start at 1 in every test.
    @BeforeEach
    void clearTables() throws SQLException {
        try (Connection c = connection();
             Statement s = c.createStatement()) {
            s.execute("SET FOREIGN_KEY_CHECKS = 0");
            s.execute("TRUNCATE TABLE user_account");
            s.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    protected Connection connection() throws SQLException {
        return DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

}
