package com.buy01.order.order.dto;

import java.time.LocalDate;

import com.buy01.order.common.Status;

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
public class OrderQuery {

    public final String search;
    public final Status status;
    public final LocalDate from;
    public final LocalDate to;
    public final int page;
    public final int size;
}
