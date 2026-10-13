package com.tip.ecommerce.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

public class PersistenceConfiguration {
  @Configuration
  @Profile("!s017")
  @EntityScan("com.tip.ecommerce.s030_correlation")
  @EnableJpaRepositories("com.tip.ecommerce.s030_correlation")
  public static class ObservabilityPersistence {}

  @Configuration
  @Profile("s017")
  @EntityScan("com.tip.ecommerce.s017_saga_compensation")
  @EnableJpaRepositories("com.tip.ecommerce.s017_saga_compensation")
  public static class SagaPersistence {}
}
