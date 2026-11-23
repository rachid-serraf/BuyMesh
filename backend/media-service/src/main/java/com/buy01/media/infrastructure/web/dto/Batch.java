package com.buy01.media.infrastructure.web.dto;

import java.util.List;
import java.util.Map;

public class Batch {

    public static class ImageRequest {

        private List<String> productIds;

        public ImageRequest() {
        }

        public ImageRequest(List<String> productIds) {
            this.productIds = productIds;
        }

        public List<String> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<String> productIds) {
            this.productIds = productIds;
        }

        public List<String> productIds() {
            return productIds;
        }
    }

    public static class ImageResponse {

        private final Map<String, String> images;

        public ImageResponse(Map<String, String> images) {
            this.images = images;
        }

        public Map<String, String> getImages() {
            return images;
        }

        public Map<String, String> images() {
            return images;
        }
    }

}
