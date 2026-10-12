package com.tip.ecommerce.s030_correlation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
  private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
  private final InventoryRepository inventoryRepository;
  private final ObjectMapper json;

  public InventoryService(InventoryRepository inventoryRepository, ObjectMapper json) {
    this.inventoryRepository = inventoryRepository;
    this.json = json;
  }
  public record Line(String sku, int quantity) {}
  public record Reservation(long orderId, List<Line> items) {}

  public Stock stock(String sku) {
    var rows = inventoryRepository.findStock(sku);
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU not found");
    return new Stock(sku, ((Number) rows.get(0).get("available")).intValue());
  }

  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> reserve(Reservation req) throws Exception {
    log.atInfo().addKeyValue("event", "reservation.started").addKeyValue("orderId", req.orderId())
        .log("Reserving stock");
    // Keep the reservation and stock deduction in one transaction.
    if (req.orderId() < 1
        || req.items() == null
        || req.items().isEmpty()
        || req.items().size() > 50
        || req.items().stream()
            .anyMatch(i -> i == null || i.sku() == null || i.quantity() < 1 || i.quantity() > 99)
        || req.items().stream().map(Line::sku).distinct().count() != req.items().size())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reservation");
    String tenant = "demo";
    var sorted = req.items().stream().sorted(Comparator.comparing(Line::sku)).toList();
    String payload = json.writeValueAsString(sorted);
    int inserted = inventoryRepository.insertReservation(tenant, req.orderId(), payload);
    if (inserted == 0) {
      var existing = inventoryRepository.findReservationForUpdate(tenant, req.orderId()).get(0);
      if (!payload.equals(existing.get("items")) || existing.get("state").equals("RELEASED"))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation mismatch");
      log.atInfo().addKeyValue("event", "reservation.reused").addKeyValue("orderId", req.orderId())
          .addKeyValue("state", existing.get("state")).log("Existing reservation returned");
      return Map.of("state", existing.get("state"));
    }
    for (var line : sorted)
      if (inventoryRepository.deductStock(line.sku(), line.quantity()) != 1)
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock: " + line.sku());
    logAfterCommit(req.orderId(), "RESERVED");
    return Map.of("state", "RESERVED");
  }


  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> finish(long id, String action) throws Exception {
    if (!List.of("commit", "release").contains(action))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    String tenant = "demo";
    var rows = inventoryRepository.findReservationForUpdate(tenant, id);
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    var row = rows.get(0);
    String target = action.equals("commit") ? "COMMITTED" : "RELEASED";
    String state = (String) row.get("state");
    if (state.equals(target)) return Map.of("state", state);
    if (!state.equals("RESERVED"))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation already finalized");
    if (action.equals("release"))
      for (var line : json.readTree((String) row.get("items")))
        inventoryRepository.restoreStock(line.path("sku").asText(), line.path("quantity").asInt());
    inventoryRepository.updateReservationState(tenant, id, target);
    logAfterCommit(id, target);
    return Map.of("state", target);
  }

  private void logAfterCommit(long orderId, String state) {
    // A success log must describe committed data, not an update that can still roll back.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        log.atInfo().addKeyValue("event", "reservation.committed").addKeyValue("orderId", orderId)
            .addKeyValue("state", state).addKeyValue("outcome", "success")
            .log("Reservation change committed");
      }
    });
  }
}
