package com.tip.ecommerce.s030_correlation;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@org.springframework.context.annotation.Profile("!s017 & !s026")
public class InventoryController {
  private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
  private final InventoryService inventoryService;

  public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

  @GetMapping("/{sku}")
  public Stock stock(@PathVariable String sku) {
    log.atDebug().addKeyValue("event", "stock.received").addKeyValue("sku", sku)
        .log("Stock lookup received");
    return inventoryService.stock(sku);
  }

  @PostMapping("/reservations")
  public Map<String, Object> reserve(@RequestBody InventoryService.Reservation request) throws Exception {
    log.atDebug().addKeyValue("event", "reservation.received").addKeyValue("orderId", request.orderId())
        .log("Reservation request received");
    return inventoryService.reserve(request);
  }

  @PostMapping("/reservations/{id}/{action}")
  public Map<String, Object> finish(@PathVariable long id, @PathVariable String action) throws Exception {
    return inventoryService.finish(id, action);
  }
}
