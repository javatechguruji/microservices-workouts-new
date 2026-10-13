package com.tip.ecommerce.s041_transaction_boundary;

import com.tip.ecommerce.client.InventoryServiceClient;
import com.tip.ecommerce.exception.CheckoutRolledBackException;
import com.tip.ecommerce.s030_correlation.Order;
import com.tip.ecommerce.s030_correlation.OrderRepository;
import com.tip.ecommerce.s030_correlation.OrderService.Checkout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile("s041 & !s017")
public class BoundaryService {
    private final OrderRepository orders;
    private final InventoryServiceClient inventory;
    private final boolean failAfterReservation;

    public BoundaryService(
            OrderRepository orders,
            InventoryServiceClient inventory,
            @Value("${s041.fail-after-reservation:true}") boolean failAfterReservation) {
        this.orders = orders;
        this.inventory = inventory;
        this.failAfterReservation = failAfterReservation;
    }

    public record Accepted(long orderId, String status, String state) {
    }

    @Transactional
    public Accepted checkout(Checkout request) {
        if (request.customerId() == null
                || request.expectedAmount() == null
                || request.items() == null
                || request.items().isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Customer, amount and items are required");
        var order = orders.saveAndFlush(new Order(request.customerId(), request.expectedAmount()));
        inventory.reserve(order.getId(), request.items());
        // Inventory has committed in another process. This exception rolls back ONLY the order DB.
        if (failAfterReservation) throw new CheckoutRolledBackException(order.getId());
        return new Accepted(order.getId(), "PENDING", "RESERVED");
    }

    public boolean orderExists(long id) {
        return orders.existsById(id);
    }
}
