package com.turkcell.order_service.dto;

import com.turkcell.order_service.event.OrderItem;
import java.util.List;

public record CreateOrderRequest(List<OrderItem> items) {}
