package com.tip.ecommerce.s018_saga_recovery;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("s018")
public class DuplicatePublication {
  private final boolean enabled;

  public DuplicatePublication(@Value("${saga.demo.duplicate-publish:false}") boolean enabled) {
    this.enabled = enabled;
  }

  public void repeat(KafkaTemplate<String, String> kafka, String topic, String key, String payload)
      throws Exception {
    // Publish the SAME event ID again to demonstrate consumer deduplication after an uncertain
    // send.
    if (enabled) kafka.send(topic, key, payload).get(12, TimeUnit.SECONDS);
  }
}
