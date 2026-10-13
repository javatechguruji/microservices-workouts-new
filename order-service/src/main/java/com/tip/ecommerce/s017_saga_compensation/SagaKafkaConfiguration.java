package com.tip.ecommerce.s017_saga_compensation;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Profile("s017")
@EnableScheduling
public class SagaKafkaConfiguration {
  @Bean
  NewTopic sagaTopic(@Value("${saga.topic}") String topic) {
    return TopicBuilder.name(topic).partitions(1).replicas(1).build();
  }

  @Bean
  DefaultErrorHandler sagaErrorHandler() {
    // Keep failed work available for retry; never silently discard it after a retry limit.
    var handler = new DefaultErrorHandler(new FixedBackOff(2000L, FixedBackOff.UNLIMITED_ATTEMPTS));
    handler.setClassifications(java.util.Map.of(Exception.class, true), true);
    return handler;
  }
}
