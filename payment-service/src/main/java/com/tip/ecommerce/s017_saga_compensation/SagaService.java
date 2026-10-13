package com.tip.ecommerce.s017_saga_compensation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("s017")
public class SagaService {
  public enum DemoResult {
    SUCCESS,
    DECLINE
  }

  private final PaymentRepository payments;
  private final EventRepository events;
  private final DemoResult result;

  public SagaService(
      PaymentRepository payments,
      EventRepository events,
      @Value("${saga.demo.payment-result:SUCCESS}") DemoResult result) {
    this.payments = payments;
    this.events = events;
    this.result = result;
  }

  @Transactional
  public void onEvent(SagaEvent event) {
    if (!"INVENTORY_RESERVED".equals(event.type())) return;
    if (!events.firstDelivery(event.eventId()) || payments.exists(event.orderId())) return;
    // The demo records a simulated payment outcome; no bank or real payment provider is called.
    boolean success = result == DemoResult.SUCCESS;
    payments.record(event, success ? "SUCCESS" : "FAILED");
    events.append(
        event.next(
            success ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED", success ? null : "Payment declined"));
  }
}
