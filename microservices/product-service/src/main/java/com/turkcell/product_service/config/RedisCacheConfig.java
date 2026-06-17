package com.turkcell.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis tabanlı cache'in davranışını ayarlayan konfigürasyon sınıfı.
 *
 * Spring Boot, ortamda bu tipte ({@link RedisCacheConfiguration}) bir bean
 * bulduğunda otomatik oluşturduğu RedisCacheManager'a bu ayarları uygular.
 */
@Configuration
public class RedisCacheConfig {

    /**
     * Tüm cache bölgeleri (products, productList) için geçerli varsayılan ayarlar:
     * - TTL 10 dk: her girdi 10 dakika sonra otomatik silinir (bayat veri tutulmaz).
     * - null değerler cache'lenmez.
     * - Anahtarlar düz okunabilir String (redis-cli'de "products::<id>" şeklinde görünür).
     * - Değerler: Spring'in varsayılan JDK serileştirmesi ile saklanır; bu sayede hem
     *   ProductResponse hem de sayfalı Page sonuçları ekstra serializer'a gerek kalmadan
     *   sorunsuz şekilde geri okunabilir.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()));
    }
}
