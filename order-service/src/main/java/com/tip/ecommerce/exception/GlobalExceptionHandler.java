package com.tip.ecommerce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail handleRejectedRequest(ResponseStatusException failure) {
    log.atWarn().addKeyValue("event", "checkout.rejected")
        .addKeyValue("httpStatus", failure.getStatusCode().value())
        .addKeyValue("outcome", "rejected").log("Checkout request rejected");
    return ProblemDetail.forStatusAndDetail(failure.getStatusCode(), failure.getReason());
  }

  @ExceptionHandler(InventoryCallException.class)
  public ProblemDetail handleInventoryFailure(InventoryCallException failure) {
    // Log the failed downstream call once, with its order and trace context.
    log.atError().addKeyValue("event", "inventory.failed").addKeyValue("orderId", failure.orderId())
        .addKeyValue("dependency", "inventory-service").addKeyValue("outcome", "failure")
        .addKeyValue("errorType", failure.getCause().getClass().getSimpleName())
        .addKeyValue("httpStatus", 503).setCause(failure)
        .log("Stock reservation failed");
    var problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.SERVICE_UNAVAILABLE, "Inventory reservation failed");
    problem.setProperty("orderId", failure.orderId());
    return problem;
  }
}
