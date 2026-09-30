package com.app.fisiotech.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationTest {
    @Test
    void criaBancoVazioEReexecutaSemAlterarVersao() {
        DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:h2:mem:flyway-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1")
                .username("sa").password("").build();
        Flyway flyway = Flyway.configure().dataSource(dataSource).load();

        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(3);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("3");
    }
}
