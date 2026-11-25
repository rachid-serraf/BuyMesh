package com.buy01.order.order.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Builder
@Jacksonized
public class OrderItemRequest {

    @NotBlank(message = "productId is required")
    public final String productId;

    public final String sellerId;

    @NotBlank(message = "name is required")
    public final String name;

    public final String imageUrl;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", message = "price must be zero or positive")
    public final BigDecimal price;

    @Min(value = 1, message = "quantity must be at least 1")
    public final int quantity;

    public final String category;

    /**
     * Backwards-compatible constructor for callers that do not have a category.
     */
    public OrderItemRequest(
            String productId,
            String sellerId,
            String name,
            String imageUrl,
            BigDecimal price,
            int quantity) {
        this(productId, sellerId, name, imageUrl, price, quantity, null);
    }
}
