package com.buy01.order.order.dto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.buy01.order.order.model.entity.DeliveryAddress;

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
public class BuyNowRequest {

    @NotBlank(message = "productId is required")
    public final String productId;

    @Min(value = 1, message = "quantity must be at least 1")
    public final int quantity;

    @NotNull(message = "Delivery address is required")
    @Valid
    public final DeliveryAddress address;
}
