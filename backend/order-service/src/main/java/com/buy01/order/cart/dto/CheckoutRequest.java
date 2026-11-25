package com.buy01.order.cart.dto;

import javax.validation.Valid;
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
public class CheckoutRequest {

    @NotNull(message = "Delivery address is required")
    @Valid
    public final DeliveryAddress address;
}
