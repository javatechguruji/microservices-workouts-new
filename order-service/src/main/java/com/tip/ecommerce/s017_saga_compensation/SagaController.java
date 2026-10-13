package com.tip.ecommerce.s017_saga_compensation;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("s017")
@RequestMapping("/s017/api/orders")
public class SagaController {
  private final SagaService service;

  public SagaController(SagaService service) {
    this.service = service;
  }

  @PostMapping("/checkout")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public SagaService.OrderView checkout(
      @RequestHeader(value = "Idempotency-Key", required = false) String key,
      @RequestBody SagaService.Checkout request) {
    return service.checkout(key, request);
  }

  @GetMapping("/{id}")
  public SagaService.OrderView find(@PathVariable long id) {
    return service.find(id);
  }
}
