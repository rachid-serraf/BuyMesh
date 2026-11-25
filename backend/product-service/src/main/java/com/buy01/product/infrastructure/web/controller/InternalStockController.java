package com.buy01.product.infrastructure.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buy01.product.domain.model.StockItem;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.infrastructure.web.dto.StockAdjustRequest;
import com.buy01.product.infrastructure.web.dto.StockItemRequest;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/stock")
@RequiredArgsConstructor
public class InternalStockController {

    private final ProductUseCase productUseCase;

    @PostMapping("/reserve")
    public Mono<ResponseEntity<Void>> reserve(@Valid @RequestBody StockAdjustRequest request) {
        return productUseCase.reserveStock(toStockItems(request))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping("/release")
    public Mono<ResponseEntity<Void>> release(@Valid @RequestBody StockAdjustRequest request) {
        return productUseCase.releaseStock(toStockItems(request))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    private List<StockItem> toStockItems(StockAdjustRequest request) {
        return request.getItems().stream()
                .map(this::toStockItem)
                .collect(Collectors.toList());
    }

    private StockItem toStockItem(StockItemRequest item) {
        return new StockItem(item.getProductId().trim(), item.getQuantity());
    }
}
