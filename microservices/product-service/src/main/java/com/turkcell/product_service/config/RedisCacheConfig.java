package com.turkcell.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis tabanlı cache'in davranışını ayarlayan konfigürasyon sınıfı.
 *
 * Spring Boot, ortamda bu tipte ({@link RedisCacheConfiguration}) bir bean
 * bulduğunda otomatik oluşturduğu RedisCacheManager'a bu ayarları uygular.
 */
@Configuration
public class RedisCacheConfig {

    /**
     * Tüm cache bölgeleri (products, productList) için varsayılan ayarlar:
     * - TTL 10 dk: her girdi 10 dakika sonra otomatik düşer (bayat veri tutulmaz).
     * - null değerler cache'lenmez.
     * - Anahtarlar düz okunabilir String (redis-cli'de "products::<id>" gibi görünür).
     * - Değerler JSON olarak saklanır (Jackson 3). Default typing açık olduğu için
     *   final tipler (record'lar dahil) ve PageImpl için @class bilgisi yazılır,
     *   böylece geri okumada doğru tip kurulur. JSON kullanmak ayrıca DevTools'un
     *   RestartClassLoader'ı ile JDK serileştirmesinde yaşanan ClassNotFound sorununu da önler.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping() // @class tip bilgisini yazar -> polimorfik deserialization
                // PageImpl'in parametresiz constructor'ı olmadığından Jackson onu tek başına kuramaz;
                // aşağıdaki modül ile PageImpl'i nasıl inşa edeceğini öğretiyoruz.
                .customize(builder -> builder.addModule(pageImplModule()))
                .build();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer));
    }

    /** PageImpl için özel deserializer'ı taşıyan Jackson modülü. */
    private SimpleModule pageImplModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new PageImplJsonDeserializer());
        return module;
    }

    /**
     * Sayfalı (Page) sonuçları Redis'ten geri okumak için custom deserializer.
     *
     * PageImpl'in Jackson'ın kullanabileceği bir kurucusu olmadığından, JSON ağacını
     * elle okuyup gerekli alanları (content, number, size, totalElements) çıkarır ve
     * yeni bir PageImpl üretir. content elemanları kendi @class bilgilerini taşıdığı için
     * doğru tipe (ProductResponse) çözülür.
     */
    static class PageImplJsonDeserializer extends ValueDeserializer<PageImpl<?>> {

        @Override
        @SuppressWarnings("unchecked")
        public PageImpl<?> deserialize(JsonParser parser, DeserializationContext ctxt) {
            JsonNode node = ctxt.readTree(parser);

            // content default typing nedeniyle ["java.util.ArrayList", [...]] sarmalında gelir;
            // List olarak okutmak bu sarmalı ve eleman tiplerini otomatik çözer.
            List<Object> content = node.has("content")
                    ? (List<Object>) ctxt.readTreeAsValue(node.get("content"), List.class)
                    : new ArrayList<>();

            long total = node.has("totalElements") ? node.get("totalElements").asLong() : content.size();
            int number = node.has("number") ? node.get("number").asInt() : 0;
            int size = node.has("size") ? node.get("size").asInt() : (content.isEmpty() ? 1 : content.size());

            Pageable pageable = size > 0 ? PageRequest.of(number, size) : Pageable.unpaged();
            return new PageImpl<>(content, pageable, total);
        }
    }
}
