package com.tip.ecommerce.s030_correlation;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {
  private final JdbcTemplate db;

  public InventoryRepository(JdbcTemplate db) { this.db = db; }

  public List<Map<String, Object>> findStock(String sku) {
    return db.queryForList("SELECT available FROM stock WHERE sku=?", sku);
  }

  public int insertReservation(String tenant, long orderId, String items) {
    return db.update("INSERT INTO reservation VALUES (?,?,'RESERVED',?) ON CONFLICT DO NOTHING",
        tenant, orderId, items);
  }

  public List<Map<String, Object>> findReservationForUpdate(String tenant, long orderId) {
    return db.queryForList("SELECT * FROM reservation WHERE tenant=? AND order_id=? FOR UPDATE",
        tenant, orderId);
  }

  public int deductStock(String sku, int quantity) {
    // The condition prevents concurrent reservations from taking stock below zero.
    return db.update("UPDATE stock SET available=available-? WHERE sku=? AND available>=?",
        quantity, sku, quantity);
  }

  public void restoreStock(String sku, int quantity) {
    db.update("UPDATE stock SET available=available+? WHERE sku=?", quantity, sku);
  }

  public void updateReservationState(String tenant, long orderId, String state) {
    db.update("UPDATE reservation SET state=? WHERE tenant=? AND order_id=?", state, tenant, orderId);
  }
}
