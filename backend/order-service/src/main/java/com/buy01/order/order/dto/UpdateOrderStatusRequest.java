package com.buy01.order.order.dto;

import javax.validation.constraints.NotNull;

import com.buy01.order.common.Status;

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
public class UpdateOrderStatusRequest {

    @NotNull(message = "status is required")
    public final Status status;
}
