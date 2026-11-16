package com.buy01.product.infrastructure.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record StockAdjustRequest(
        @NotEmpty(message = "At least one stock item is required")
        @Valid List<StockItemRequest> items) {
}
