package com.buy01.product.infrastructure.messaging;

public class OrderStockItem {

    private String productId;
    private int quantity;

    public OrderStockItem() {
    }

    public OrderStockItem(String productId, int quantity) {
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
