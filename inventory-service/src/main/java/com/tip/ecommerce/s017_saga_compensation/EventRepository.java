package com.tip.ecommerce.s017_saga_compensation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("s017")
public class EventRepository {
  private final JdbcTemplate db;
  private final ObjectMapper json;

  public EventRepository(JdbcTemplate db, ObjectMapper json) {
    this.db = db;
    this.json = json;
  }

  public record Pending(long sequence, String eventId, long orderId, String payload) {}

  public void append(SagaEvent event) {
    try {
      // This insert joins the business transaction. Both changes commit, or neither does.
      db.update(
          "INSERT INTO saga_outbox(event_id,order_id,payload) VALUES (?,?,?)",
          event.eventId(),
          event.orderId(),
          json.writeValueAsString(event));
    } catch (JsonProcessingException failure) {
      throw new IllegalStateException(failure);
    }
  }

  public boolean firstDelivery(String eventId) {
    // The marker rolls back with business work if processing fails, so Kafka can retry.
    return db.update("INSERT INTO saga_inbox(event_id) VALUES (?) ON CONFLICT DO NOTHING", eventId)
        == 1;
  }

  public List<Pending> pending() {
    return db.query(
        "SELECT sequence,event_id,order_id,payload FROM saga_outbox WHERE sent=FALSE ORDER BY"
            + " sequence LIMIT 20",
        (rs, row) -> new Pending(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getString(4)));
  }

  public void markSent(long sequence) {
    db.update("UPDATE saga_outbox SET sent=TRUE WHERE sequence=?", sequence);
  }
}
