package com.tip.ecommerce.s030_correlation;

import com.tip.ecommerce.client.InventoryServiceClient;
import com.tip.ecommerce.client.InventoryServiceClient.Line;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderService(OrderRepository orderRepository, InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public record Checkout(String customerId, BigDecimal expectedAmount, List<Line> items) {
    }

    public record Accepted(long orderId, String status, String state) {
    }

    public Accepted create(Checkout request) {
        if (request.customerId() == null || request.expectedAmount() == null
                || request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer, amount and items are required");
        }
        // Persist the order through the repository; the database generates its ID.
        Order order = orderRepository.save(new Order(request.customerId(), request.expectedAmount()));
        long orderId = order.getId();
        log.atInfo().addKeyValue("event", "order.saved").addKeyValue("orderId", orderId)
                .addKeyValue("outcome", "success").log("Order saved");

        log.atInfo().addKeyValue("event", "inventory.requested").addKeyValue("orderId", orderId)
                .addKeyValue("dependency", "inventory-service").log("Requesting stock reservation");
        long started = System.nanoTime();
        // Delegate the HTTP details to the reusable inventory client.
        inventoryServiceClient.reserve(orderId, request.items());

        log.atInfo().addKeyValue("event", "inventory.confirmed").addKeyValue("orderId", orderId)
                .addKeyValue("dependency", "inventory-service").addKeyValue("outcome", "success")
                .addKeyValue("durationMs", (System.nanoTime() - started) / 1_000_000)
                .log("Stock reserved");
        return new Accepted(orderId, "PENDING", "RESERVED");
    }

}
