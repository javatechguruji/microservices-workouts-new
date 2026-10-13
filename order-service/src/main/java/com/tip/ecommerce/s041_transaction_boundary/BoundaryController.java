package com.tip.ecommerce.s041_transaction_boundary;

import com.tip.ecommerce.s030_correlation.OrderService.Checkout;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("s041 & !s017")
@RequestMapping("/s041/api/orders")
public class BoundaryController {
  private final BoundaryService service;

  public BoundaryController(BoundaryService service) {
    this.service = service;
  }

  @PostMapping("/checkout")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public BoundaryService.Accepted checkout(@RequestBody Checkout request) {
    return service.checkout(request);
  }

  @GetMapping("/{id}")
  public Map<String, Object> evidence(@PathVariable long id) {
    return Map.of("orderId", id, "orderExists", service.orderExists(id));
  }
}
