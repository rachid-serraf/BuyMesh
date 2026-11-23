package com.buy01.order.cart.client;

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
public class StockAdjustRequest {

    public final List<StockItemRequest> items;
}
