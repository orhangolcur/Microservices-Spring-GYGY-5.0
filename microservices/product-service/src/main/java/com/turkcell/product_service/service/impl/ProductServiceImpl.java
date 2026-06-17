package com.turkcell.product_service.service.impl;

import com.turkcell.product_service.dto.request.CreateProductRequest;
import com.turkcell.product_service.dto.request.UpdateProductRequest;
import com.turkcell.product_service.dto.response.ProductResponse;
import com.turkcell.product_service.entity.Product;
import com.turkcell.product_service.exception.ProductNotFoundException;
import com.turkcell.product_service.mapper.ProductMapper;
import com.turkcell.product_service.repository.ProductRepository;
import com.turkcell.product_service.service.ProductService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    // READ (liste): sonuç "productList" cache'inde sayfa+boyut+sıralama kombinasyonuna
    // göre saklanır. Aynı sayfa tekrar istenirse DB'ye gidilmez.
    @Override
    @Cacheable(value = "productList",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toResponse);
    }

    // READ (tekil): sonuç "products" cache'inde id anahtarıyla saklanır (parametrik).
    // İkinci çağrıda DB'ye gidilmeden cache'ten döner.
    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toResponse(product);
    }

    // CREATE: yeni ürünü doğrudan "products" cache'ine yazar (#result.id() = oluşan id),
    // ayrıca liste cache'ini tamamen temizler çünkü artık eski liste eksik/yanlış.
    @Override
    @Caching(
            put = @CachePut(value = "products", key = "#result.id()"),
            evict = @CacheEvict(value = "productList", allEntries = true)
    )
    public ProductResponse create(CreateProductRequest request) {
        Product product = productMapper.toEntity(request);
        return productMapper.toResponse(productRepository.save(product));
    }

    // UPDATE: güncel veriyi "products" cache'inde id anahtarının üzerine yazar,
    // liste cache'ini temizler.
    @Override
    @Caching(
            put = @CachePut(value = "products", key = "#id"),
            evict = @CacheEvict(value = "productList", allEntries = true)
    )
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productMapper.updateEntity(product, request);
        return productMapper.toResponse(productRepository.save(product));
    }

    // DELETE: hem tekil ürün cache'ini (id anahtarı) hem de liste cache'ini kırar.
    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
