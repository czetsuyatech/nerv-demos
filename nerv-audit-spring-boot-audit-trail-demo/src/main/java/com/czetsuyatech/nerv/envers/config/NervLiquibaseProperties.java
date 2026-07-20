package com.czetsuyatech.nerv.envers.config;

import java.util.Map;
import lombok.Data;

/**
 * Custom properties class for Spring Boot 4.
 */
@Data
public class NervLiquibaseProperties {

  private String changeLog = "classpath:db/changelog/db.changelog-master.yaml";
  private String contexts = "default,dev,prod";
  private String defaultSchema = "nervaudit";
  private boolean enabled = true;
  private boolean dropFirst = false;
  private Map<String, String> parameters;
}
