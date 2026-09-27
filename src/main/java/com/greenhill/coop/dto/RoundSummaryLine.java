package com.greenhill.coop.dto;

import com.greenhill.coop.model.PricingType;

import java.math.BigDecimal;

/** Aggregated purchase requirement for one product across a whole round. */
public class RoundSummaryLine {

    private Long productId;
    private String productName;
    private PricingType pricingType;
    private BigDecimal unitPrice;
    private BigDecimal totalQuantity;
    private BigDecimal estimatedCost;

    public RoundSummaryLine(Long productId, String productName, PricingType pricingType,
                            BigDecimal unitPrice, BigDecimal totalQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.pricingType = pricingType;
        this.unitPrice = unitPrice;
        this.totalQuantity = totalQuantity;
        this.estimatedCost = unitPrice.multiply(totalQuantity);
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public PricingType getPricingType() { return pricingType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
}
