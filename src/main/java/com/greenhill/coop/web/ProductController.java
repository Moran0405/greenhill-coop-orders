package com.greenhill.coop.web;

import com.greenhill.coop.dto.ProductRequest;
import com.greenhill.coop.dto.ProductView;
import com.greenhill.coop.model.Product;
import com.greenhill.coop.service.ProductService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

/** Catalogue browsing for everyone signed in, maintenance for the coordinator. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** Active catalogue for ordering. */
    @GetMapping
    public List<ProductView> list(@RequestParam(defaultValue = "true") boolean activeOnly,
                                  HttpSession session) {
        SessionAuth.requireSignedIn(session);
        List<Product> products = activeOnly ? productService.listActive() : productService.listAll();
        return products.stream().map(ProductView::from).collect(Collectors.toList());
    }

    @PostMapping
    public ProductView create(@RequestBody ProductRequest request, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return ProductView.from(productService.create(fromRequest(null, request)));
    }

    @PutMapping("/{id}")
    public ProductView update(@PathVariable Long id, @RequestBody ProductRequest request,
                              HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return ProductView.from(productService.update(id, fromRequest(id, request)));
    }

    @DeleteMapping("/{id}")
    public ProductView deactivate(@PathVariable Long id, HttpSession session) {
        SessionAuth.requireCoordinator(session);
        return ProductView.from(productService.deactivate(id));
    }

    private Product fromRequest(Long id, ProductRequest request) {
        Product p = new Product();
        p.setId(id);
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setPricingType(request.getPricingType());
        p.setPrice(request.getPrice());
        p.setActive(request.getActive() == null || request.getActive());
        return p;
    }
}
