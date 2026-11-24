package com.buy01.product.infrastructure.web.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

    @NotEmpty(message = "At least one stock item is required")
    @Valid
    private List<StockItemRequest> items;
}
