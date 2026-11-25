package com.buy01.product.infrastructure.web.dto;

public class ProductSearchRequest {

    private final String q;
    private final String category;
    private final Double minPrice;
    private final Double maxPrice;
    private final String sort;
    private final int page;
    private final int size;

    public ProductSearchRequest(String q, String category, Double minPrice, Double maxPrice,
            String sort, int page, int size) {
        this.q = q;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sort = sort;
        this.page = page;
        this.size = size;
    }

    public String getQ() {
        return q;
    }

    public String getCategory() {
        return category;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public String getSort() {
        return sort;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String q() {
        return q;
    }

    public String category() {
        return category;
    }

    public Double minPrice() {
        return minPrice;
    }

    public Double maxPrice() {
        return maxPrice;
    }

    public String sort() {
        return sort;
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }
}
