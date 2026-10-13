package com.tip.ecommerce.s017_saga_compensation;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("s017")
public class PaymentRepository {
  private final JdbcTemplate db;

  public PaymentRepository(JdbcTemplate db) {
    this.db = db;
  }

  public boolean exists(long orderId) {
    return db.queryForObject("SELECT COUNT(*) FROM payment WHERE order_id=?", Long.class, orderId)
        > 0;
  }

  public record PaymentView(
      long paymentId, long orderId, java.math.BigDecimal amount, String status) {}

  public PaymentView find(long orderId) {
    var rows =
        db.query(
            "SELECT id,order_id,amount,status FROM payment WHERE order_id=?",
            (rs, row) ->
                new PaymentView(rs.getLong(1), rs.getLong(2), rs.getBigDecimal(3), rs.getString(4)),
            orderId);
    if (rows.isEmpty())
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.NOT_FOUND, "Payment not found");
    return rows.get(0);
  }

  public void record(SagaEvent event, String status) {
    db.update(
        "INSERT INTO payment(order_id,amount,status) VALUES (?,?,?)",
        event.orderId(),
        event.amount(),
        status);
  }
}
