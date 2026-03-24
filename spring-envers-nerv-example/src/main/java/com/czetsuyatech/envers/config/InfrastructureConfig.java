package com.czetsuyatech.envers.config;

import com.czetsuyatech.audit.config.EnableNervAudit;
import com.czetsuyatech.envers.persistence.entity.EntityConfig;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@EnableNervAudit
public class InfrastructureConfig {

  /**
   * Liquibase configuration bean. Automatically runs on application startup.
   */
  @Bean
  @ConditionalOnMissingBean
  public SpringLiquibase liquibase(DataSource dataSource, NervLiquibaseProperties props) {

    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(props.getChangeLog());
    liquibase.setContexts(props.getContexts());
    liquibase.setDefaultSchema(props.getDefaultSchema());
    liquibase.setShouldRun(props.isEnabled());
    liquibase.setDropFirst(props.isDropFirst());
    liquibase.setChangeLogParameters(props.getParameters());

    return liquibase;
  }

  @Bean
  @DependsOn("liquibase")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder emfBuilder,
      DataSource dataSource
  ) {

    return emfBuilder
        .dataSource(dataSource)
        .packages(EntityConfig.class)
        .build();
  }

  /**
   * Custom Liquibase properties class to replace the removed Spring Boot 4 LiquibaseProperties.
   */
  @Bean
  @ConfigurationProperties(prefix = "spring.liquibase")
  public NervLiquibaseProperties liquibaseProperties() {
    return new NervLiquibaseProperties();
  }
}
