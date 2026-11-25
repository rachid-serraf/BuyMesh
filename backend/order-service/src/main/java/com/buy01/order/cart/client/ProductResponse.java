package com.buy01.order.cart.client;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {

    public final String id;
    public final String name;
    public final BigDecimal price;
    public final int quantity;
    public final String userId;
    public final List<String> imagesIds;
    public final String category;

    /**
     * Backwards-compatible constructor for tests and callers without category.
     */
    public ProductResponse(
            String id,
            String name,
            BigDecimal price,
            int quantity,
            String userId,
            List<String> imagesIds) {
        this(id, name, price, quantity, userId, imagesIds, null);
    }
}
