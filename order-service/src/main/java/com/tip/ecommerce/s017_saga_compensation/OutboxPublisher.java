package com.tip.ecommerce.s017_saga_compensation;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("s017")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "saga.publisher.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final EventRepository events;
    private final KafkaTemplate<String, String> kafka;
    private final String topic;
    private final org.springframework.beans.factory.ObjectProvider<
            com.tip.ecommerce.s018_saga_recovery.DuplicatePublication>
            duplicate;

    public OutboxPublisher(
            EventRepository events,
            KafkaTemplate<String, String> kafka,
            @Value("${saga.topic}") String topic,
            org.springframework.beans.factory.ObjectProvider<
                    com.tip.ecommerce.s018_saga_recovery.DuplicatePublication>
                    duplicate) {
        this.events = events;
        this.kafka = kafka;
        this.topic = topic;
        this.duplicate = duplicate;
    }

    @Scheduled(fixedDelayString = "${saga.publish-delay-ms:1000}")
    public void publish() {
        for (var event : events.pending()) {
            try {
                // Wait for the broker acknowledgement before marking the durable row as sent.
                kafka
                        .send(topic, Long.toString(event.orderId()), event.payload())
                        .get(12, TimeUnit.SECONDS);
                if (duplicate.getIfAvailable() != null)
                    duplicate
                            .getObject()
                            .repeat(kafka, topic, Long.toString(event.orderId()), event.payload());
                events.markSent(event.sequence());
                log.info("Event published eventId={} orderId={}", event.eventId(), event.orderId());
            } catch (Exception failure) {
                if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
                // A lost acknowledgement can cause a duplicate. Keep its original event ID on retry.
                log.warn(
                        "Event remains pending eventId={} orderId={} cause={}",
                        event.eventId(),
                        event.orderId(),
                        failure.toString());
                return; // Keep this publisher's events in sequence; retry the pending row next time.
            }
        }
    }
}
