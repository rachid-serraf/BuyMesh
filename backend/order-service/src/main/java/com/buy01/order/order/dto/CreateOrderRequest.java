package com.buy01.order.order.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

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
public class CreateOrderRequest {

    public final String checkoutId;
    public final String sellerId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    public final List<OrderItemRequest> items;

    @Valid
    public final DeliveryAddress address;
}
