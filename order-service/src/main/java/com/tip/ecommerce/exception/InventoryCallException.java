package com.tip.ecommerce.exception;

public class InventoryCallException extends RuntimeException {
  private final long orderId;

  public InventoryCallException(long orderId, Throwable cause) {
    super("Inventory reservation failed", cause);
    this.orderId = orderId;
  }

  public long orderId() { return orderId; }
}
