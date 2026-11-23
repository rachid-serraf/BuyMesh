package com.buy01.product.infrastructure.web.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

public class StockAdjustRequest {

    @NotEmpty(message = "At least one stock item is required")
    @Valid
    private List<StockItemRequest> items;

    public StockAdjustRequest() {
    }

    public StockAdjustRequest(List<StockItemRequest> items) {
        this.items = items;
    }

    public List<StockItemRequest> getItems() {
        return items;
    }

    public void setItems(List<StockItemRequest> items) {
        this.items = items;
    }

    public List<StockItemRequest> items() {
        return items;
    }
}
