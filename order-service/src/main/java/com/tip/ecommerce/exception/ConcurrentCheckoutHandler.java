package com.tip.ecommerce.exception;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Profile("s017")
public class ConcurrentCheckoutHandler {
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail conflict(DataIntegrityViolationException failure) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT,
        "Checkout conflicted with another write. Retry with the same Idempotency-Key and body.");
  }
}
