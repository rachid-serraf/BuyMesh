package com.buy01.product.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import com.buy01.product.domain.model.StockItem;
import com.buy01.product.domain.ports.inbound.ProductUseCase;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OrderStockListenerTest {

    @Mock
    private ProductUseCase productUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    private OrderStockListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderStockListener(productUseCase);
    }

    @Test
    void reserveSubtractsStockForEachProductAndAcknowledges() {
        when(productUseCase.reserveStock(anyList())).thenReturn(Mono.empty());

        listener.onOrderStock(new OrderStockEvent(
                "order-1",
                StockAction.RESERVE,
                List.of(new OrderStockItem("prod-1", 2), new OrderStockItem("prod-2", 1))),
                acknowledgment);

        verify(productUseCase).reserveStock(
                List.of(new StockItem("prod-1", 2), new StockItem("prod-2", 1)));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void releaseAddsStockForEachProductAndAcknowledges() {
        when(productUseCase.releaseStock(anyList())).thenReturn(Mono.empty());

        listener.onOrderStock(new OrderStockEvent(
                "order-1",
                StockAction.RELEASE,
                List.of(new OrderStockItem("prod-1", 3))),
                acknowledgment);

        verify(productUseCase).releaseStock(List.of(new StockItem("prod-1", 3)));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void ignoresEventsWithoutItems() {
        listener.onOrderStock(new OrderStockEvent("order-1", StockAction.RESERVE, List.of()),
                acknowledgment);

        verify(productUseCase, never()).reserveStock(anyList());
        verify(productUseCase, never()).releaseStock(anyList());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void doesNotAcknowledgeWhenStockAdjustmentFails() {
        when(productUseCase.reserveStock(anyList()))
                .thenReturn(Mono.error(new IllegalStateException("insufficient stock")));

        assertThrows(IllegalStateException.class, () -> listener.onOrderStock(
                new OrderStockEvent(
                        "order-1",
                        StockAction.RESERVE,
                        List.of(new OrderStockItem("prod-1", 99))),
                acknowledgment));

        verify(acknowledgment, never()).acknowledge();
    }
}
