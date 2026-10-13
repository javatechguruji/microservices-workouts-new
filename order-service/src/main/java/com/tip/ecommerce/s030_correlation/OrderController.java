package com.tip.ecommerce.s030_correlation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/s030/api/orders")
@org.springframework.context.annotation.Profile("!s017 & !s041")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderService.Accepted create(@RequestBody OrderService.Checkout request) {
        // HTTP entry details stay at DEBUG; the service records the business event.
        log.atDebug().addKeyValue("event", "checkout.received").log("Checkout request received");
        return orderService.create(request);
    }
}
