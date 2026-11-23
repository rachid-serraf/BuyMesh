package com.buy01.media.infrastructure.web.dto;

public class ProductDeletedEvent {

    private String productId;

    public ProductDeletedEvent() {
    }

    public ProductDeletedEvent(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String productId() {
        return productId;
    }
}
