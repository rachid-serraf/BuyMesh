package com.buy01.order.order.dto;

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
public class ApiError {

    public final String code;
    public final String message;
    public final List<String> details;

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError of(String code, String message, List<String> details) {
        return new ApiError(code, message, details == null ? List.of() : details);
    }
}
