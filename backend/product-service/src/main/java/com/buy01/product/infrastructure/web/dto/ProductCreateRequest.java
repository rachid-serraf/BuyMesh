package com.buy01.product.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;

import com.buy01.product.domain.model.Category;
import javax.validation.constraints.*;

public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @Min(value = 1, message = "Price must be at least 1.0")
    @Max(value = 10000, message = "Price cannot exceed 10,000.0")
    @Digits(integer = 5, fraction = 2, message = "Price must have at most 2 decimal places")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 1000, message = "Quantity cannot exceed 1,000 units")
    private int quantity;

    @NotEmpty(message = "At least one media ID is required")
    @Size(min = 1, max = 10, message = "You can upload between 1 and 10 images")
    private List<String> imagesIds;

    @NotNull(message = "Category is required")
    private Category category;

    public ProductCreateRequest() {
    }

    public ProductCreateRequest(String name, String description, BigDecimal price, int quantity,
            List<String> imagesIds, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imagesIds = imagesIds;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<String> getImagesIds() {
        return imagesIds;
    }

    public void setImagesIds(List<String> imagesIds) {
        this.imagesIds = imagesIds;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public BigDecimal price() {
        return price;
    }

    public int quantity() {
        return quantity;
    }

    public List<String> imagesIds() {
        return imagesIds;
    }

    public Category category() {
        return category;
    }
}
