package com.greenhill.coop.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;

/** Payload for saving a member's order for one round. */
public class OrderRequest {

    @NotBlank
    private List<OrderLine> items;

    public List<OrderLine> getItems() { return items; }
    public void setItems(List<OrderLine> items) { this.items = items; }

    public static class OrderLine {
        private Long productId;
        private String quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        /** Accepts "2" or "1.5" - validated against the product pricing type. */
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
    }
}
