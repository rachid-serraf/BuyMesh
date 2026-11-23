package com.buy01.order.order.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

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
public class PageResponse<T> {

    public final List<T> content;
    public final int page;
    public final int size;
    public final long totalElements;
    public final int totalPages;

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).collect(Collectors.toList()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
