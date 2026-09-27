package com.greenhill.coop.dto;

import com.greenhill.coop.model.PricingType;
import com.greenhill.coop.model.Product;

import java.math.BigDecimal;

/** Product details as exposed to the front end. */
public class ProductView {

    private Long id;
    private String name;
    private String description;
    private PricingType pricingType;
    private BigDecimal price;
    private boolean active;

    public static ProductView from(Product p) {
        ProductView v = new ProductView();
        v.id = p.getId();
        v.name = p.getName();
        v.description = p.getDescription();
        v.pricingType = p.getPricingType();
        v.price = p.getPrice();
        v.active = p.isActive();
        return v;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public PricingType getPricingType() { return pricingType; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
}
