package com.buy01.media.infrastructure.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ImagesLinkedEventTest {

    @Test
    void supportsConstructorAndAccessors() {
        ImagesLinkedEvent event = new ImagesLinkedEvent("prod-1", List.of("img-1", "img-2"));

        assertThat(event.getProductId()).isEqualTo("prod-1");
        assertThat(event.getMediaIds()).containsExactly("img-1", "img-2");
        assertThat(event.productId()).isEqualTo("prod-1");
        assertThat(event.mediaIds()).containsExactly("img-1", "img-2");
    }

    @Test
    void supportsNoArgConstructionAndSetters() {
        ImagesLinkedEvent event = new ImagesLinkedEvent();
        event.setProductId("prod-2");
        event.setMediaIds(List.of("img-3"));

        assertThat(event.productId()).isEqualTo("prod-2");
        assertThat(event.mediaIds()).containsExactly("img-3");
    }
}
