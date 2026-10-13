package com.tip.ecommerce.s017_saga_compensation;

import com.tip.ecommerce.s018_saga_recovery.ReleaseFailure;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("s017")
public class SagaService {
  private final SagaInventoryRepository inventory;
  private final EventRepository events;
  private final ObjectProvider<ReleaseFailure> releaseFailure;

  public SagaService(
      SagaInventoryRepository inventory,
      EventRepository events,
      ObjectProvider<ReleaseFailure> releaseFailure) {
    this.inventory = inventory;
    this.events = events;
    this.releaseFailure = releaseFailure;
  }

  @Transactional
  public void onEvent(SagaEvent event) {
    if (!List.of("ORDER_CREATED", "PAYMENT_COMPLETED", "PAYMENT_FAILED").contains(event.type()))
      return;
    if (!events.firstDelivery(event.eventId())) return;
    String state = inventory.state(event.orderId());
    if (event.type().equals("ORDER_CREATED")) {
      if (state != null)
        return; // A reservation is a business-level guard as well as event deduplication.
      boolean reserved = inventory.reserve(event);
      events.append(
          event.next(
              reserved ? "INVENTORY_RESERVED" : "INVENTORY_REJECTED",
              reserved ? null : "Insufficient stock"));
      return;
    }
    if (state == null) throw new IllegalStateException("Reservation missing for payment outcome");
    String target = event.type().equals("PAYMENT_FAILED") ? "RELEASED" : "COMMITTED";
    if (target.equals(state)) return;
    if (!"RESERVED".equals(state))
      throw new IllegalStateException("Reservation already finalized: " + state);
    if (target.equals("RELEASED")) releaseFailure.ifAvailable(ReleaseFailure::beforeRelease);
    // Compensation is a NEW transaction: restore stock and record an outgoing confirmation.
    inventory.finish(event.orderId(), target);
    events.append(
        event.next(
            target.equals("RELEASED") ? "INVENTORY_RELEASED" : "INVENTORY_COMMITTED",
            event.reason()));
  }
}
