package com.buy01.order.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.buy01.order.common.Status;
import com.buy01.order.order.model.entity.DeliveryAddress;
import com.buy01.order.order.model.entity.Order;

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
public class OrderResponse {

    public final String id;
    public final String checkoutId;
    public final String buyerId;
    public final String sellerId;
    public final Status status;
    public final List<OrderItemResponse> items;
    public final BigDecimal total;
    public final DeliveryAddress address;
    public final Instant createdAt;
    public final Instant updatedAt;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCheckoutId(),
                order.getBuyerId(),
                order.getSellerId(),
                order.getStatus(),
                items,
                order.getTotal(),
                order.getAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
