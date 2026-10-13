package com.tip.ecommerce.s018_saga_recovery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("s018")
public class ReleaseFailure {
  private final boolean fail;

  public ReleaseFailure(@Value("${saga.demo.fail-release:false}") boolean fail) {
    this.fail = fail;
  }

  public void beforeRelease() {
    // A controlled local failure lets the class observe unfinished compensation and retry.
    if (fail) throw new IllegalStateException("S018 demo: inventory release unavailable");
  }
}
