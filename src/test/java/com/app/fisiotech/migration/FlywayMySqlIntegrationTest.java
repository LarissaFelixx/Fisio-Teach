package com.app.fisiotech.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMySqlIntegrationTest {
    @Test
    @EnabledIfEnvironmentVariable(named = "MYSQL_TEST_URL", matches = ".+")
    void aplicaTodasAsMigracoesEmMySql() {
        Flyway flyway = Flyway.configure()
                .dataSource(System.getenv("MYSQL_TEST_URL"), System.getenv("MYSQL_TEST_USERNAME"),
                        System.getenv("MYSQL_TEST_PASSWORD"))
                .cleanDisabled(false).load();
        flyway.clean();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
    }
}
