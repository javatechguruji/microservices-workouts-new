package com.tip.ecommerce.s017_saga_compensation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile("s017")
public class SagaService {
    private final SagaOrderRepository orders;
    private final EventRepository events;
    private final ObjectMapper json;

    public SagaService(SagaOrderRepository orders, EventRepository events, ObjectMapper json) {
        this.orders = orders;
        this.events = events;
        this.json = json;
    }

    public record Checkout(
            String customerId, BigDecimal expectedAmount, List<SagaEvent.Line> items) {
    }

    public record OrderView(long orderId, String status, String reason) {
    }

    @Transactional
    public OrderView checkout(String requestKey, Checkout request) {
        validate(requestKey, request);
        var normalized =
                new Checkout(
                        request.customerId(),
                        request.expectedAmount().setScale(2),
                        request.items().stream().sorted(Comparator.comparing(SagaEvent.Line::sku)).toList());
        String payload;
        try {
            payload = json.writeValueAsString(normalized);
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException(failure);
        }
        var existing = orders.findByRequestKey(requestKey);
        if (existing.isPresent()) {
            if (!existing.get().getRequestPayload().equals(payload))
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Idempotency-Key already used for another checkout");
            return view(existing.get());
        }
        var order =
                orders.save(
                        new SagaOrder(request.customerId(), normalized.expectedAmount(), requestKey, payload));
        // The order and its outgoing event commit together in this service's H2 database.
        events.append(
                new SagaEvent(
                        UUID.randomUUID().toString(),
                        "ORDER_CREATED",
                        order.getId(),
                        request.customerId(),
                        normalized.expectedAmount(),
                        normalized.items(),
                        null));
        return view(order); // HTTP 202 means accepted; other services still have work to do.
    }

    @Transactional(readOnly = true)
    public OrderView find(long id) {
        return view(
                orders
                        .findById(id)
                        .orElseThrow(
                                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));
    }

    @Transactional
    public void onEvent(SagaEvent event) {
        if (!List.of(
                        "INVENTORY_REJECTED", "PAYMENT_FAILED", "INVENTORY_RELEASED", "INVENTORY_COMMITTED")
                .contains(event.type())) return;
        if (!events.firstDelivery(event.eventId())) return;
        var order =
                orders
                        .findById(event.orderId())
                        .orElseThrow(() -> new IllegalStateException("Order missing"));
        if (List.of("CONFIRMED", "CANCELLED").contains(order.getStatus())) return;
        // Cancellation is complete only after inventory confirms that compensation committed.
        switch (event.type()) {
            case "PAYMENT_FAILED" -> order.transition("COMPENSATING", event.reason());
            case "INVENTORY_RELEASED", "INVENTORY_REJECTED" -> order.transition("CANCELLED", event.reason());
            case "INVENTORY_COMMITTED" -> order.transition("CONFIRMED", null);
            default -> throw new IllegalStateException("Unexpected event");
        }
        // JPA writes the changed status when this transaction commits.
    }

    private OrderView view(SagaOrder order) {
        return new OrderView(order.getId(), order.getStatus(), order.getReason());
    }

    private void validate(String key, Checkout request) {
        if (key == null
                || !key.matches("[A-Za-z0-9_-]{1,80}")
                || request.customerId() == null
                || request.customerId().isBlank()
                || request.customerId().length() > 200
                || request.expectedAmount() == null
                || request.expectedAmount().signum() <= 0
                || request.expectedAmount().compareTo(new BigDecimal("9999999999.99")) > 0
                || request.expectedAmount().stripTrailingZeros().scale() > 2
                || request.items() == null
                || request.items().isEmpty()
                || request.items().size() > 50
                || request.items().stream()
                .anyMatch(
                        line ->
                                line == null
                                        || line.sku() == null
                                        || line.sku().isBlank()
                                        || line.sku().length() > 50
                                        || line.quantity() < 1
                                        || line.quantity() > 99)
                || request.items().stream().map(SagaEvent.Line::sku).distinct().count()
                != request.items().size())
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid Idempotency-Key, customer, amount and unique items are required");
    }
}
