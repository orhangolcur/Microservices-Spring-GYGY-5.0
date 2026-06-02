package com.turkcell.product_service.web.controller;

import java.util.UUID;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.turkcell.product_service.event.TestEvent;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final StreamBridge streamBridge;

    public ProductController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello Product-Service";
    }

    @GetMapping
    public String sendTestEvent(@PathVariable String message) {
        var event = new TestEvent(message, UUID.fromString(message));
        streamBridge.send("test-event-out-0", event);
        return "Başarılı";
    }
}
