package com.tip.ecommerce.s032_incident_diagnosis;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Implements the original revision guide's slow-inventory experiment on its existing API. */
@Component
@Profile("s032-slow-inventory")
public class SlowInventoryFilter extends OncePerRequestFilter {
  private final long delayMs;

  public SlowInventoryFilter(@Value("${observability.s032.delay-ms:1200}") long delayMs) {
    if (delayMs < 0 || delayMs > 2000) {
      throw new IllegalArgumentException("S032 delay must be between 0 and 2000 ms");
    }
    this.delayMs = delayMs;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"POST".equals(request.getMethod())
        || !"/api/inventory/reservations".equals(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    LoggerFactory.getLogger(getClass()).atWarn().addKeyValue("event", "inventory.delayed")
        .addKeyValue("delayMs", delayMs).log("Demo inventory delay applied");
    try {
      Thread.sleep(delayMs);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new ServletException("S032 delay interrupted", interrupted);
    }
    chain.doFilter(request, response);
  }
}
