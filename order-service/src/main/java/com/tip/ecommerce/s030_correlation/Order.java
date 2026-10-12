package com.tip.ecommerce.s030_correlation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String customerId;
  private String tenant;
  private BigDecimal amount;
  private String status;
  private LocalDateTime createdAt;

  protected Order() {}

  public Order(String customerId, BigDecimal amount) {
    this.customerId = customerId;
    this.amount = amount;
    this.tenant = "demo";
    this.status = "PENDING";
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
}
