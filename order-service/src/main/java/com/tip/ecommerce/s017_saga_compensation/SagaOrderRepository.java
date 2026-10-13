package com.tip.ecommerce.s017_saga_compensation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaOrderRepository extends JpaRepository<SagaOrder, Long> {
  Optional<SagaOrder> findByRequestKey(String requestKey);
}
