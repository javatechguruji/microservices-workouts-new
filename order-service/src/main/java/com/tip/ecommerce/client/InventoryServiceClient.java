package com.tip.ecommerce.client;

import com.tip.ecommerce.exception.InventoryCallException;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryServiceClient {
  private final WebClient webClient;

  public InventoryServiceClient(WebClient.Builder builder,
      @Value("${inventory.base-url}") String inventoryUrl) {
    this.webClient = builder.baseUrl(inventoryUrl).build();
  }

  public record Line(String sku, int quantity) {}
  public record Reservation(long orderId, List<Line> items) {}
  public record ReservationResult(String state) {}

  public void reserve(long orderId, List<Line> items) {
    // The Java agent carries the active trace context on this outgoing HTTP call.
    webClient.post().uri("/api/inventory/reservations")
        .bodyValue(new Reservation(orderId, items))
        .retrieve().bodyToMono(ReservationResult.class)
        .filter(result -> "RESERVED".equals(result.state()))
        .switchIfEmpty(Mono.error(new IllegalStateException("Inventory did not confirm reservation")))
        .timeout(Duration.ofSeconds(3))
        // The shared handler logs the failure and builds the HTTP error response.
        .onErrorMap(failure -> new InventoryCallException(orderId, failure))
        .block();
  }
}
