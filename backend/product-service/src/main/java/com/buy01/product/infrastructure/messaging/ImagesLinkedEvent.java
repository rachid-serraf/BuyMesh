package com.buy01.product.infrastructure.messaging;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImagesLinkedEvent)) {
            return false;
        }
        ImagesLinkedEvent that = (ImagesLinkedEvent) o;
        return java.util.Objects.equals(productId, that.productId)
                && java.util.Objects.equals(mediaIds, that.mediaIds);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(productId, mediaIds);
    }

    @Override
    public String toString() {
        return "ImagesLinkedEvent{productId='" + productId + "', mediaIds=" + mediaIds + "}";
    }
}
