package com.tip.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class)
public class OrderServiceApplication {
  public static void main(String[] args) { SpringApplication.run(OrderServiceApplication.class, args); }
}
