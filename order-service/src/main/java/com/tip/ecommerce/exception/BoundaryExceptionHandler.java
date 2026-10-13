package com.tip.ecommerce.exception;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Profile("s041 & !s017")
public class BoundaryExceptionHandler {
  @ExceptionHandler(CheckoutRolledBackException.class)
  public ProblemDetail rolledBack(CheckoutRolledBackException failure) {
    var body =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Demo failure after inventory committed; local order rolled back");
    body.setProperty("orderId", failure.orderId());
    return body;
  }
}
