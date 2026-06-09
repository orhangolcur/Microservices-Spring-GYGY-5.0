package com.turkcell.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.product_service.entity.OutboxEvent;
import com.turkcell.product_service.entity.OutboxStatus;
import com.turkcell.product_service.event.TestEvent;
import com.turkcell.product_service.repository.OutboxRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendTestEvent(String message) {
        UUID id = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        var event = new TestEvent(eventId, message, id);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(eventId);
        outboxEvent.setAggregateType("Product");
        outboxEvent.setAggregateId(id.toString());
        outboxEvent.setEventType("testEvent");
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setCreatedAt(Instant.now());

        outboxRepository.save(outboxEvent);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
