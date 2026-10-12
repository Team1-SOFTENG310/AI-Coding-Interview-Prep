package com.aicodinginterviewprep.db;

import org.flywaydb.core.Flyway;

public final class DatabaseMigrator {

    private DatabaseMigrator() {}

    public static void migrate() {
        Flyway.configure()
                .dataSource(Database.url(), Database.user(), Database.password())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}