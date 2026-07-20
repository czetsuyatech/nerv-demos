package com.czetsuyatech.nerv.envers.persistence.config;

import com.czetsuyatech.nerv.envers.persistence.entity.EntityConfig;
import com.czetsuyatech.nerv.envers.persistence.repository.RepositoryConfig;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackageClasses = RepositoryConfig.class)
@EntityScan(basePackageClasses = EntityConfig.class)
@EnableTransactionManagement
public class PersistenceConfig {

}
