package com.buy01.order.cart.client;

import java.util.List;

public record StockAdjustRequest(List<StockItemRequest> items) {
}
