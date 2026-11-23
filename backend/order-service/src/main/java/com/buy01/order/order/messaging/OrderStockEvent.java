package com.buy01.order.order.messaging;

import java.util.List;

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
public class OrderStockEvent {

    public final String orderId;
    public final StockAction action;
    public final List<OrderStockItem> items;
}
