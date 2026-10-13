package com.tip.ecommerce.s017_saga_compensation;

import com.tip.ecommerce.s030_correlation.InventoryService;
import com.tip.ecommerce.s030_correlation.Stock;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("s017")
@RequestMapping("/api/inventory")
public class InventoryQueryController {
  private final InventoryService inventory;
  private final SagaInventoryRepository reservations;

  public InventoryQueryController(
      InventoryService inventory, SagaInventoryRepository reservations) {
    this.inventory = inventory;
    this.reservations = reservations;
  }

  @GetMapping("/{sku}")
  public Stock stock(@PathVariable String sku) {
    return inventory.stock(sku);
  }

  @GetMapping("/reservations/{id}")
  public SagaInventoryRepository.ReservationView reservation(@PathVariable long id) {
    return reservations.find(id);
  }
}
