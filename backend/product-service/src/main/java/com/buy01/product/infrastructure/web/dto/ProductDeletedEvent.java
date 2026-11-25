package com.buy01.product.infrastructure.web.dto;

public class ProductDeletedEvent {

    private final String productId;

    public ProductDeletedEvent(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public String productId() {
        return productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductDeletedEvent)) {
            return false;
        }
        ProductDeletedEvent that = (ProductDeletedEvent) o;
        return java.util.Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "ProductDeletedEvent{productId='" + productId + "'}";
    }
}
