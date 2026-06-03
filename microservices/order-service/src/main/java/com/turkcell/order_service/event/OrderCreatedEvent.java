package com.turkcell.order_service.event;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, List<OrderItem> items) {}
