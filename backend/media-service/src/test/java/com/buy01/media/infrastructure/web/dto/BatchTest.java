package com.buy01.media.infrastructure.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BatchTest {

    @Test
    void batchCanBeInstantiated() {
        assertThat(new Batch()).isNotNull();
    }

    @Test
    void imageRequestSupportsConstructorAndAccessors() {
        Batch.ImageRequest request = new Batch.ImageRequest(List.of("prod-1"));

        assertThat(request.getProductIds()).containsExactly("prod-1");
        assertThat(request.productIds()).containsExactly("prod-1");

        Batch.ImageRequest empty = new Batch.ImageRequest();
        empty.setProductIds(List.of("prod-2"));
        assertThat(empty.productIds()).containsExactly("prod-2");
    }

    @Test
    void imageResponseSupportsConstructorAndAccessors() {
        Batch.ImageResponse response = new Batch.ImageResponse(Map.of("prod-1", "/media/img-1"));

        assertThat(response.getImages()).containsEntry("prod-1", "/media/img-1");
        assertThat(response.images()).containsEntry("prod-1", "/media/img-1");
    }
}
