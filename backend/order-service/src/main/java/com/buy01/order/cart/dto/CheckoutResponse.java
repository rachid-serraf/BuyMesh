package com.buy01.order.cart.dto;

import java.util.List;

import com.buy01.order.cart.model.entity.Cart;
import com.buy01.order.order.dto.OrderResponse;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class CheckoutResponse {

    public final boolean valid;
    public final List<CartItemIssue> issues;
    public final Cart cart;
    public final List<OrderResponse> orders;
}
