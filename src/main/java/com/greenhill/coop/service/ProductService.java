package com.greenhill.coop.service;

import com.greenhill.coop.model.Product;
import com.greenhill.coop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Maintains the catalogue of products the co-operative can buy each week. */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> listActive() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Product> listAll() {
        return productRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Product create(Product product) {
        validate(product);
        product.setId(null);
        return productRepository.save(product);
    }

    public Product update(Long id, Product changes) {
        Product existing = get(id);
        validate(changes);
        existing.setName(changes.getName());
        existing.setDescription(changes.getDescription());
        existing.setPricingType(changes.getPricingType());
        existing.setPrice(changes.getPrice());
        existing.setActive(changes.isActive());
        return productRepository.save(existing);
    }

    /** Deactivate a product so it no longer appears in new orders. */
    public Product deactivate(Long id) {
        Product existing = get(id);
        existing.setActive(false);
        return productRepository.save(existing);
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (product.getPricingType() == null) {
            throw new IllegalArgumentException("Pricing type is required.");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or positive.");
        }
    }
}
