package com.tip.ecommerce.s017_saga_compensation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class SagaOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String customerId;
  private String tenant;
  private BigDecimal amount;
  private String status;
  private LocalDateTime createdAt;
  private String requestKey;

  @Column(columnDefinition = "text")
  private String requestPayload;

  private String reason;

  protected SagaOrder() {}

  public SagaOrder(String customerId, BigDecimal amount, String requestKey, String requestPayload) {
    this.customerId = customerId;
    this.amount = amount;
    this.requestKey = requestKey;
    this.requestPayload = requestPayload;
    tenant = "demo";
    status = "PENDING";
    createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getStatus() {
    return status;
  }

  public String getReason() {
    return reason;
  }

  public String getRequestPayload() {
    return requestPayload;
  }

  public void transition(String status, String reason) {
    this.status = status;
    this.reason = reason;
  }
}
