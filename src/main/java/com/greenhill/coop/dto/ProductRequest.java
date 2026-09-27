package com.greenhill.coop.dto;

import com.greenhill.coop.model.PricingType;

import java.math.BigDecimal;

/** Payload for creating or updating a catalogue product (coordinator only). */
public class ProductRequest {

    private String name;
    private String description;
    private PricingType pricingType;
    private BigDecimal price;
    private Boolean active;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PricingType getPricingType() { return pricingType; }
    public void setPricingType(PricingType pricingType) { this.pricingType = pricingType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
