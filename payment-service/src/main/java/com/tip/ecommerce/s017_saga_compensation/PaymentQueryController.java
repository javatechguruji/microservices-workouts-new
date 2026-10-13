package com.tip.ecommerce.s017_saga_compensation;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("s017")
@RequestMapping("/s017/api/payments")
public class PaymentQueryController {
  private final PaymentRepository payments;

  public PaymentQueryController(PaymentRepository payments) {
    this.payments = payments;
  }

  @GetMapping("/orders/{id}")
  public PaymentRepository.PaymentView payment(@PathVariable long id) {
    return payments.find(id);
  }
}
