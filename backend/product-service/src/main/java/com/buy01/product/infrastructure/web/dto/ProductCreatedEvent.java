package com.buy01.product.infrastructure.web.dto;

import java.util.List;

public class ProductCreatedEvent {

    private final String productIdm;
    private final List<String> mediaIdsm;
    private final String sellerIdm;

    public ProductCreatedEvent(String productIdm, List<String> mediaIdsm, String sellerIdm) {
        this.productIdm = productIdm;
        this.mediaIdsm = mediaIdsm;
        this.sellerIdm = sellerIdm;
    }

    public String getProductIdm() {
        return productIdm;
    }

    public List<String> getMediaIdsm() {
        return mediaIdsm;
    }

    public String getSellerIdm() {
        return sellerIdm;
    }

    public String productIdm() {
        return productIdm;
    }

    public List<String> mediaIdsm() {
        return mediaIdsm;
    }

    public String sellerIdm() {
        return sellerIdm;
    }
}
