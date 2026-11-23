package com.buy01.order.cart.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

public class CartItemDTO {

    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    @Builder
    @Jacksonized
    public static class AddRequest {

        @NotBlank
        public final String productId;

        @Min(1)
        public final int quantity;
    }

    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    @Builder
    @Jacksonized
    public static class UpdateRequest {

        @Min(1)
        public final int quantity;
    }
}
