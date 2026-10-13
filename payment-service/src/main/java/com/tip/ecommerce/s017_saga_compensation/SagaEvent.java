package com.tip.ecommerce.s017_saga_compensation;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SagaEvent(
    String eventId,
    String type,
    long orderId,
    String customerId,
    BigDecimal amount,
    List<Line> items,
    String reason) {
  public record Line(String sku, int quantity) {}

  public SagaEvent next(String nextType, String nextReason) {
    return new SagaEvent(
        UUID.randomUUID().toString(), nextType, orderId, customerId, amount, items, nextReason);
  }
}
