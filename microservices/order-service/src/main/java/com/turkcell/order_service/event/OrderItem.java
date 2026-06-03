package com.turkcell.order_service.event;

import java.util.UUID;

public record OrderItem(UUID productId, int quantity) {}
