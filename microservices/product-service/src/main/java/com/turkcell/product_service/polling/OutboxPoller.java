package com.turkcell.product_service.polling;

import java.time.Instant;
import java.util.List;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import com.turkcell.product_service.entity.OutboxEvent;
import com.turkcell.product_service.entity.OutboxStatus;
import com.turkcell.product_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;

@Component
public class OutboxPoller {

    private final OutboxRepository outboxRepository;
    private final StreamBridge streamBridge;

    public OutboxPoller(OutboxRepository outboxRepository, StreamBridge streamBridge) {
        this.outboxRepository = outboxRepository;
        this.streamBridge = streamBridge;
    }

    // ÖDEV: Burayı CDC ile (Debezium) değiştir.
    // ÇÖZÜM: OutboxPoller devre dışı bırakıldı.
    // Outbox polling sorumluluğu Debezium Kafka Connect connector'ına devredildi.
    // Debezium, PostgreSQL WAL'ını izleyerek outbox tablosundaki her INSERT'i
    // anında "outbox.event.Product" Kafka topic'ine yazar.
    // Bkz: debezium-connector.json
    //@Scheduled(fixedDelay = 20000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findPublishable(100);

        for (OutboxEvent event : events) {
            try {
                streamBridge.send(event.getEventType() + "-out-0", event.getPayload());
                event.setStatus(OutboxStatus.SENT);
            } catch (Exception e) {
                if (event.getRetryCount() >= 3) 
                    event.setStatus(OutboxStatus.FAILED);
                else 
                    event.setRetryCount(event.getRetryCount() + 1);
            }
            event.setProcessedAt(Instant.now());
            outboxRepository.save(event);
        }
    }
}
