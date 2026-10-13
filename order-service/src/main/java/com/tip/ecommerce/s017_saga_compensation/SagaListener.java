package com.tip.ecommerce.s017_saga_compensation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("s017")
public class SagaListener {
  private final ObjectMapper json;
  private final SagaService service;

  public SagaListener(ObjectMapper json, SagaService service) {
    this.json = json;
    this.service = service;
  }

  @KafkaListener(topics = "${saga.topic}", groupId = "${spring.application.name}-${saga.run-id}")
  public void receive(String payload) throws Exception {
    var event = json.readValue(payload, SagaEvent.class);
    service.onEvent(event); // The proxied service commits its H2 transaction before we return.
    LoggerFactory.getLogger(SagaListener.class)
        .info(
            "Event handled type={} orderId={} eventId={}",
            event.type(),
            event.orderId(),
            event.eventId());
    // RECORD acknowledgement advances the Kafka offset only after this method succeeds.
  }
}
