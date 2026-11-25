package com.buy01.product.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.buy01.product.domain.model.Category;
import com.buy01.product.domain.model.Product;

public class ProductSearchResponse {

    private final String id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final int quantity;
    private final Category category;
    private final String userId;
    private final Instant createdAt;
    private final Instant updatedAt;

    public ProductSearchResponse(String id, String name, String description, BigDecimal price,
            int quantity, Category category, String userId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductSearchResponse from(Product p) {
        return new ProductSearchResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getQuantity(), p.getCategory(), p.getUserId(),
                p.getCreatedAt(), p.getUpdatedAt());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public Category getCategory() {
        return category;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
