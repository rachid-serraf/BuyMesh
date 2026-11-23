package com.buy01.product.infrastructure.web.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class StockItemRequest {

    @NotBlank(message = "productId is required")
    private String productId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    public StockItemRequest() {
    }

    public StockItemRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String productId() {
        return productId;
    }

    public int quantity() {
        return quantity;
    }
}
