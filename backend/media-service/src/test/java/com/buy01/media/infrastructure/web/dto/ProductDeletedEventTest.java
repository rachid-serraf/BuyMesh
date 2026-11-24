package com.buy01.media.infrastructure.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductDeletedEventTest {

    @Test
    void supportsConstructorAndAccessors() {
        ProductDeletedEvent event = new ProductDeletedEvent("prod-1");

        assertThat(event.getProductId()).isEqualTo("prod-1");
        assertThat(event.productId()).isEqualTo("prod-1");
    }

    @Test
    void supportsNoArgConstructionAndSetter() {
        ProductDeletedEvent event = new ProductDeletedEvent();
        event.setProductId("prod-2");

        assertThat(event.productId()).isEqualTo("prod-2");
    }
}
