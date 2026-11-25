package com.buy01.product.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.buy01.product.domain.model.StockItem;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.infrastructure.web.dto.StockAdjustRequest;
import com.buy01.product.infrastructure.web.dto.StockItemRequest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class InternalStockControllerTest {

    @Mock
    private ProductUseCase productUseCase;

    private InternalStockController controller;

    @BeforeEach
    void setUp() {
        controller = new InternalStockController(productUseCase);
    }

    @Test
    void reserveMapsRequestToStockItemsAndReturnsNoContent() {
        StockAdjustRequest request = new StockAdjustRequest(
                List.of(new StockItemRequest(" prod-1 ", 2), new StockItemRequest("prod-2", 1)));
        when(productUseCase.reserveStock(List.of(
                new StockItem("prod-1", 2), new StockItem("prod-2", 1))))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.reserve(request))
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();

        verify(productUseCase).reserveStock(List.of(
                new StockItem("prod-1", 2), new StockItem("prod-2", 1)));
    }

    @Test
    void releaseMapsRequestToStockItemsAndReturnsNoContent() {
        StockAdjustRequest request = new StockAdjustRequest(
                List.of(new StockItemRequest("prod-1", 3)));
        when(productUseCase.releaseStock(List.of(new StockItem("prod-1", 3))))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.release(request))
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();

        verify(productUseCase).releaseStock(List.of(new StockItem("prod-1", 3)));
    }
}
