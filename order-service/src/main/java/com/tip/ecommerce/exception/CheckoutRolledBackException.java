package com.tip.ecommerce.exception;

public class CheckoutRolledBackException extends RuntimeException {
  private final long orderId;

  public CheckoutRolledBackException(long orderId) {
    super("Demo failure after inventory committed");
    this.orderId = orderId;
  }

  public long orderId() {
    return orderId;
  }
}
