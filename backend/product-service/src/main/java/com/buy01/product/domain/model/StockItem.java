package com.buy01.product.domain.model;

import java.util.Objects;

public class StockItem {

    private final String productId;
    private final int quantity;

    public StockItem(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String productId() {
        return productId;
    }

    public int quantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StockItem)) {
            return false;
        }
        StockItem stockItem = (StockItem) o;
        return quantity == stockItem.quantity && Objects.equals(productId, stockItem.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity);
    }

    @Override
    public String toString() {
        return "StockItem{productId='" + productId + "', quantity=" + quantity + "}";
    }
}
