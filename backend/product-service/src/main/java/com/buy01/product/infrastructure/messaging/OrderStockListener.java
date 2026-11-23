package com.buy01.product.infrastructure.messaging;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.buy01.product.domain.model.StockItem;
import com.buy01.product.domain.ports.inbound.ProductUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStockListener {

    private static final Duration PROCESSING_TIMEOUT = Duration.ofSeconds(30);

    private final ProductUseCase productUseCase;

    @KafkaListener(
            topics = "${kafka.topics.order-stock:order-stock-events}",
            groupId = "product-service-group",
            containerFactory = "orderStockKafkaListenerContainerFactory")
    public void onOrderStock(OrderStockEvent event, Acknowledgment acknowledgment) {
        if (!isValid(event)) {
            acknowledgment.acknowledge();
            return;
        }

        List<StockItem> items = event.items().stream()
                .filter(item -> item.productId() != null && !item.productId().isBlank()
                        && item.quantity() > 0)
                .map(item -> new StockItem(item.productId(), item.quantity()))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            if (event.action() == StockAction.RESERVE) {
                productUseCase.reserveStock(items).block(PROCESSING_TIMEOUT);
            } else {
                productUseCase.releaseStock(items).block(PROCESSING_TIMEOUT);
            }
            acknowledgment.acknowledge();
            log.info("Kafka: {} stock processed for order {}", event.action(), event.orderId());
        } catch (RuntimeException error) {
            log.error("Kafka: failed to {} stock for order {}",
                    event.action(), event.orderId(), error);
            throw error;
        }
    }

    private boolean isValid(OrderStockEvent event) {
        return event != null && event.action() != null
                && event.items() != null && !event.items().isEmpty();
    }
}
