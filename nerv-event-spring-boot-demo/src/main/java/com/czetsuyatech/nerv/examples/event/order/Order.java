package com.czetsuyatech.nerv.examples.event.order;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "demo_order")
public class Order {

  @Id
  private UUID id;
  private String customerId;
  private String status;
  private Instant createdAt;

  protected Order() {
  }

  Order(UUID id, String customerId, String status, Instant createdAt) {
    this.id = id;
    this.customerId = customerId;
    this.status = status;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public String getCustomerId() { return customerId; }
  public String getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
}
