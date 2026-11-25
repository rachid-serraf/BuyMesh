package com.buy01.order.cart.dto;

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
public class CartItemIssue {

    public final String productId;
    public final String name;
    public final CartIssueType type;
    public final String message;
}
