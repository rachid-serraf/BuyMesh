package com.buy01.order.order.dto;

import java.math.BigDecimal;

import com.buy01.order.order.model.entity.OrderItem;

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
public class OrderItemResponse {

    public final String productId;
    public final String sellerId;
    public final String name;
    public final String imageUrl;
    public final BigDecimal price;
    public final int quantity;

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getSellerId(),
                item.getName(),
                item.getImageUrl(),
                item.getPrice(),
                item.getQuantity());
    }
}
