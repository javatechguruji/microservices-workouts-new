package com.tip.ecommerce.s017_saga_compensation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("s017")
public class SagaInventoryRepository {
  private final JdbcTemplate db;
  private final ObjectMapper json;

  public SagaInventoryRepository(JdbcTemplate db, ObjectMapper json) {
    this.db = db;
    this.json = json;
  }

  public String state(long orderId) {
    var rows =
        db.queryForList(
            "SELECT state FROM reservation WHERE tenant='demo' AND order_id=? FOR UPDATE",
            String.class,
            orderId);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public record ReservationView(long orderId, String state, java.util.List<SagaEvent.Line> items) {}

  public ReservationView find(long orderId) {
    var rows =
        db.query(
            "SELECT state,items FROM reservation WHERE tenant='demo' AND order_id=?",
            (rs, row) -> {
              try {
                return new ReservationView(
                    orderId,
                    rs.getString(1),
                    java.util.List.of(json.readValue(rs.getString(2), SagaEvent.Line[].class)));
              } catch (JsonProcessingException failure) {
                throw new IllegalStateException(failure);
              }
            },
            orderId);
    if (rows.isEmpty())
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.NOT_FOUND, "Reservation not found");
    return rows.get(0);
  }

  public boolean reserve(SagaEvent event) {
    // Lock in SKU order. Check every item before writing, so a rejection changes no stock.
    for (var line : event.items()) {
      var quantities =
          db.queryForList(
              "SELECT available FROM stock WHERE sku=? FOR UPDATE", Integer.class, line.sku());
      if (quantities.isEmpty() || quantities.get(0) < line.quantity()) return false;
    }
    for (var line : event.items())
      db.update("UPDATE stock SET available=available-? WHERE sku=?", line.quantity(), line.sku());
    try {
      db.update(
          "INSERT INTO reservation(tenant,order_id,state,items) VALUES ('demo',?,'RESERVED',?)",
          event.orderId(),
          json.writeValueAsString(event.items()));
    } catch (JsonProcessingException failure) {
      throw new IllegalStateException(failure);
    }
    return true;
  }

  public void finish(long orderId, String target) {
    if (target.equals("RELEASED")) {
      // Restore the stored reservation, rather than trusting quantities in a later event.
      String payload =
          db.queryForObject(
              "SELECT items FROM reservation WHERE tenant='demo' AND order_id=?",
              String.class,
              orderId);
      try {
        for (var line : json.readValue(payload, SagaEvent.Line[].class))
          db.update(
              "UPDATE stock SET available=available+? WHERE sku=?", line.quantity(), line.sku());
      } catch (JsonProcessingException failure) {
        throw new IllegalStateException(failure);
      }
    }
    db.update("UPDATE reservation SET state=? WHERE tenant='demo' AND order_id=?", target, orderId);
  }
}
