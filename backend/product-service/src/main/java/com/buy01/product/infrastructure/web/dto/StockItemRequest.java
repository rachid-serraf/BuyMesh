package com.buy01.product.infrastructure.web.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockItemRequest {

    @NotBlank(message = "productId is required")
    private String productId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;
}
