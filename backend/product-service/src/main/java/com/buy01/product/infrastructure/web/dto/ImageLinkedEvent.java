package com.buy01.product.infrastructure.web.dto;

import java.util.List;

public class ImageLinkedEvent {

    private final String productId;
    private final List<String> mediaIds;

    public ImageLinkedEvent(String productId, List<String> mediaIds) {
        this.productId = productId;
        this.mediaIds = mediaIds;
    }

    public String getProductId() {
        return productId;
    }

    public List<String> getMediaIds() {
        return mediaIds;
    }

    public String productId() {
        return productId;
    }

    public List<String> mediaIds() {
        return mediaIds;
    }
}
