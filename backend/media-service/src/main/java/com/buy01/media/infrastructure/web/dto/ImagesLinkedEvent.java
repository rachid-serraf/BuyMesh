package com.buy01.media.infrastructure.web.dto;

import java.util.List;

public class ImagesLinkedEvent {

    private String productId;
    private List<String> mediaIds;

    public ImagesLinkedEvent() {
    }

    public ImagesLinkedEvent(String productId, List<String> mediaIds) {
        this.productId = productId;
        this.mediaIds = mediaIds;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public List<String> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(List<String> mediaIds) {
        this.mediaIds = mediaIds;
    }

    public String productId() {
        return productId;
    }

    public List<String> mediaIds() {
        return mediaIds;
    }
}
