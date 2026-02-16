package com.ys.ai.aifinancemanager.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class FlywayConfig {

  @Bean
  public Flyway flyway(DataSource dataSource) {
    log.info("Configuring Flyway for SQLite database");

    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .validateOnMigrate(true)
        .load();

    log.info("Running Flyway migrations...");
    flyway.migrate();
    log.info("Flyway migrations completed successfully");

    return flyway;
  }
}

