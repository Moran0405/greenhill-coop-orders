package com.greenhill.coop.dto;

import com.greenhill.coop.model.OrderItem;
import com.greenhill.coop.model.PricingType;

import java.math.BigDecimal;

/** One order line as exposed to the front end. */
public class OrderItemView {

    private Long productId;
    private String productName;
    private PricingType pricingType;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal lineTotal;

    public static OrderItemView from(OrderItem item) {
        OrderItemView v = new OrderItemView();
        v.productId = item.getProduct().getId();
        v.productName = item.getProduct().getName();
        v.pricingType = item.getProduct().getPricingType();
        v.unitPrice = item.getProduct().getPrice();
        v.quantity = item.getQuantity();
        v.lineTotal = item.getProduct().getPrice().multiply(item.getQuantity());
        return v;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public PricingType getPricingType() { return pricingType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
