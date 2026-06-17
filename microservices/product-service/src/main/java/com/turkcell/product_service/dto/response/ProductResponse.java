package com.turkcell.product_service.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

// Serializable: Redis cache'inde JDK serileştirmesi ile saklanabilmesi için gerekli
public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        int stock
) implements Serializable {}
