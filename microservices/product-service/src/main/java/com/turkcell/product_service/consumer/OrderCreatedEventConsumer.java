package com.turkcell.product_service.consumer;

import com.turkcell.product_service.event.OrderCreatedEvent;
import com.turkcell.product_service.event.OrderItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrderCreatedEventConsumer {

    @Bean
    public Consumer<OrderCreatedEvent> consumeOrderCreatedEvent() {
        return event -> {
            System.out.println("OrderCreatedEvent alındı. Order ID: " + event.orderId());
            for (OrderItem item : event.items()) {
                System.out.println("Stok güncelleniyor -> Product ID: " + item.productId() + ", Miktar: " + item.quantity());
            }
        };
    }
}
