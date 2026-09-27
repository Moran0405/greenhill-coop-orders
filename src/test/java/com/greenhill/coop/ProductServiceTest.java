package com.greenhill.coop;

import com.greenhill.coop.model.Product;
import com.greenhill.coop.model.PricingType;
import com.greenhill.coop.repository.ProductRepository;
import com.greenhill.coop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Catalogue maintenance user story (coordinator). */
class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void coordinatorCanAddProduct() {
        Product oil = new Product("Olive Oil", "500ml", PricingType.UNIT, new BigDecimal("14.00"));

        Product saved = productService.create(oil);

        assertEquals("Olive Oil", saved.getName());
        verify(productRepository).save(oil);
    }

    @Test
    void productWithoutNameIsRejected() {
        Product bad = new Product("", null, PricingType.UNIT, new BigDecimal("1.00"));
        assertThrows(IllegalArgumentException.class, () -> productService.create(bad));
    }

    @Test
    void productWithoutPricingTypeIsRejected() {
        Product bad = new Product("Quinoa", null, null, new BigDecimal("5.00"));
        assertThrows(IllegalArgumentException.class, () -> productService.create(bad));
    }

    @Test
    void negativePriceIsRejected() {
        Product bad = new Product("Quinoa", null, PricingType.WEIGHT, new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class, () -> productService.create(bad));
    }

    @Test
    void coordinatorCanDeactivateProduct() {
        Product oil = new Product("Olive Oil", "500ml", PricingType.UNIT, new BigDecimal("14.00"));
        oil.setId(7L);
        when(productRepository.findById(7L)).thenReturn(Optional.of(oil));

        Product result = productService.deactivate(7L);

        assertFalse(result.isActive());
    }

    @Test
    void coordinatorCanUpdateProductPrice() {
        Product oil = new Product("Olive Oil", "500ml", PricingType.UNIT, new BigDecimal("14.00"));
        oil.setId(7L);
        when(productRepository.findById(7L)).thenReturn(Optional.of(oil));

        Product changes = new Product("Olive Oil", "500ml", PricingType.UNIT, new BigDecimal("15.50"));
        Product updated = productService.update(7L, changes);

        assertEquals(new BigDecimal("15.50"), updated.getPrice());
    }
}
