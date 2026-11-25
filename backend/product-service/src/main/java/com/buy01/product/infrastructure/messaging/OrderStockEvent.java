package com.buy01.product.infrastructure.messaging;

import java.util.List;

public class OrderStockEvent {

    private String orderId;
    private StockAction action;
    private List<OrderStockItem> items;

    public OrderStockEvent() {
    }

    public OrderStockEvent(String orderId, StockAction action, List<OrderStockItem> items) {
        this.orderId = orderId;
        this.action = action;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public StockAction getAction() {
        return action;
    }

    public void setAction(StockAction action) {
        this.action = action;
    }

    public List<OrderStockItem> getItems() {
        return items;
    }

    public void setItems(List<OrderStockItem> items) {
        this.items = items;
    }

    public String orderId() {
        return orderId;
    }

    public StockAction action() {
        return action;
    }

    public List<OrderStockItem> items() {
        return items;
    }
}
