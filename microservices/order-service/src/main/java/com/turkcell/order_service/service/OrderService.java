package com.turkcell.order_service.service;

import com.turkcell.order_service.dto.CreateOrderRequest;
import com.turkcell.order_service.event.OrderCreatedEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    private final StreamBridge streamBridge;

    public OrderService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public UUID createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        var event = new OrderCreatedEvent(orderId, request.items());
        streamBridge.send("orderCreatedEvent-out-0", event);
        return orderId;
    }
}
