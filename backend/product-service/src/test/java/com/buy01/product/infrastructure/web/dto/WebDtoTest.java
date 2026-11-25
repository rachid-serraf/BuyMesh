package com.buy01.product.infrastructure.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.buy01.product.domain.model.Category;
import org.junit.jupiter.api.Test;

import com.buy01.product.domain.model.Product;

class WebDtoTest {

    @Test
    void imageLinkedEventExposesFields() {
        ImageLinkedEvent event = new ImageLinkedEvent("prod-1", List.of("img-1"));

        assertThat(event.productId()).isEqualTo("prod-1");
        assertThat(event.mediaIds()).containsExactly("img-1");
    }

    @Test
    void productCreatedEventExposesFields() {
        ProductCreatedEvent event = new ProductCreatedEvent("prod-1", List.of("img-1"), "seller-1");

        assertThat(event.productIdm()).isEqualTo("prod-1");
        assertThat(event.mediaIdsm()).containsExactly("img-1");
        assertThat(event.sellerIdm()).isEqualTo("seller-1");
    }

    @Test
    void productDeletedEventExposesFields() {
        ProductDeletedEvent event = new ProductDeletedEvent("prod-1");

        assertThat(event.productId()).isEqualTo("prod-1");
    }

    @Test
    void productCreateRequestExposesFields() {
        ProductCreateRequest request = new ProductCreateRequest(
                "Product", "A product description", BigDecimal.valueOf(10.0), 2, List.of("img-1"), Category.OTHER);

        assertThat(request.name()).isEqualTo("Product");
        assertThat(request.description()).isEqualTo("A product description");
        assertThat(request.price()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(request.quantity()).isEqualTo(2);
        assertThat(request.imagesIds()).containsExactly("img-1");
        assertThat(request.category()).isEqualTo(Category.OTHER);
    }

    @Test
    void productListExposesAndMutatesFields() {
        Instant createdAt = Instant.parse("2026-03-25T10:00:00Z");
        ProductList list = new ProductList("prod-1", "Product", BigDecimal.valueOf(10.0), createdAt, "url-1", Category.ELECTRONICS);

        assertThat(list.getId()).isEqualTo("prod-1");
        assertThat(list.getName()).isEqualTo("Product");
        assertThat(list.getPrice()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(list.getCreatedAt()).isEqualTo(createdAt);
        assertThat(list.getImage()).isEqualTo("url-1");
        assertThat(list.getCategory()).isEqualTo(Category.ELECTRONICS);

        list.setId("prod-2");
        list.setName("Updated");
        list.setPrice(BigDecimal.valueOf(20.0));
        list.setCreatedAt(createdAt.plusSeconds(60));
        list.setImage("url-2");
        list.setCategory(Category.CLOTHING);

        assertThat(list.getId()).isEqualTo("prod-2");
        assertThat(list.getName()).isEqualTo("Updated");
        assertThat(list.getPrice()).isEqualTo(BigDecimal.valueOf(20.0));
        assertThat(list.getCreatedAt()).isEqualTo(createdAt.plusSeconds(60));
        assertThat(list.getImage()).isEqualTo("url-2");
        assertThat(list.getCategory()).isEqualTo(Category.CLOTHING);
    }

    @Test
    void productResponseFullConstructorExposesFields() {
        Instant created = Instant.parse("2026-03-25T10:00:00Z");
        Instant updated = Instant.parse("2026-03-25T11:00:00Z");
        ProductResponse response = new ProductResponse(
                "prod-1", "Product", "A product description", BigDecimal.valueOf(10.0), 2,
                "seller-1", List.of("img-1"),Category.OTHER, created, updated);

        assertThat(response.getId()).isEqualTo("prod-1");
        assertThat(response.getName()).isEqualTo("Product");
        assertThat(response.getDescription()).isEqualTo("A product description");
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getUserId()).isEqualTo("seller-1");
        assertThat(response.getImagesIds()).containsExactly("img-1");
        assertThat(response.getCategory()).isEqualTo(Category.OTHER);
        assertThat(response.getCreatedAt()).isEqualTo(created);
        assertThat(response.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void productResponseCopiesDomainProduct() {
        Product product = Product.builder()
                .id("prod-1")
                .name("Product")
                .description("A product description")
                .price(BigDecimal.valueOf(10.0))
                .quantity(2)
                .userId("seller-1")
                .category(Category.ELECTRONICS)
                .createdAt(Instant.parse("2026-03-25T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-25T11:00:00Z"))
                .build();

        ProductResponse response = new ProductResponse(product, List.of("img-1"));

        assertThat(response.getId()).isEqualTo("prod-1");
        assertThat(response.getName()).isEqualTo("Product");
        assertThat(response.getDescription()).isEqualTo("A product description");
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getUserId()).isEqualTo("seller-1");
        assertThat(response.getImagesIds()).containsExactly("img-1");
        assertThat(response.getCategory()).isEqualTo(Category.ELECTRONICS);
        assertThat(response.getCreatedAt()).isEqualTo(product.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void productSearchResponseExposesFields() {
        Instant created = Instant.parse("2026-03-25T10:00:00Z");
        Instant updated = Instant.parse("2026-03-25T11:00:00Z");
        ProductSearchResponse response = new ProductSearchResponse(
                "prod-1", "Product", "A product description", BigDecimal.valueOf(10.0), 2,
                Category.ELECTRONICS, "seller-1", created, updated);

        assertThat(response.getId()).isEqualTo("prod-1");
        assertThat(response.getName()).isEqualTo("Product");
        assertThat(response.getDescription()).isEqualTo("A product description");
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getCategory()).isEqualTo(Category.ELECTRONICS);
        assertThat(response.getUserId()).isEqualTo("seller-1");
        assertThat(response.getCreatedAt()).isEqualTo(created);
        assertThat(response.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void productSearchResponseMapsDomainProduct() {
        Instant created = Instant.parse("2026-03-25T10:00:00Z");
        Instant updated = Instant.parse("2026-03-25T11:00:00Z");
        Product product = Product.builder()
                .id("prod-1")
                .name("Product")
                .description("A product description")
                .price(BigDecimal.valueOf(10.0))
                .quantity(2)
                .userId("seller-1")
                .category(Category.CLOTHING)
                .createdAt(created)
                .updatedAt(updated)
                .build();

        ProductSearchResponse response = ProductSearchResponse.from(product);

        assertThat(response.getId()).isEqualTo(product.getId());
        assertThat(response.getName()).isEqualTo(product.getName());
        assertThat(response.getDescription()).isEqualTo(product.getDescription());
        assertThat(response.getPrice()).isEqualTo(product.getPrice());
        assertThat(response.getQuantity()).isEqualTo(product.getQuantity());
        assertThat(response.getCategory()).isEqualTo(product.getCategory());
        assertThat(response.getUserId()).isEqualTo(product.getUserId());
        assertThat(response.getCreatedAt()).isEqualTo(product.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void stockItemRequestSupportsConstructorAndAccessors() {
        StockItemRequest request = new StockItemRequest("prod-1", 3);

        assertThat(request.getProductId()).isEqualTo("prod-1");
        assertThat(request.getQuantity()).isEqualTo(3);

        request.setProductId("prod-2");
        request.setQuantity(5);

        assertThat(request.getProductId()).isEqualTo("prod-2");
        assertThat(request.getQuantity()).isEqualTo(5);
    }

    @Test
    void stockItemRequestSupportsNoArgConstructor() {
        StockItemRequest request = new StockItemRequest();

        request.setProductId("prod-1");
        request.setQuantity(1);

        assertThat(request.getProductId()).isEqualTo("prod-1");
        assertThat(request.getQuantity()).isEqualTo(1);
    }

    @Test
    void stockAdjustRequestSupportsConstructorAndAccessors() {
        List<StockItemRequest> items = List.of(new StockItemRequest("prod-1", 3));
        StockAdjustRequest request = new StockAdjustRequest(items);

        assertThat(request.getItems()).containsExactlyElementsOf(items);

        List<StockItemRequest> updated = List.of(new StockItemRequest("prod-2", 1));
        request.setItems(updated);

        assertThat(request.getItems()).containsExactlyElementsOf(updated);
    }

    @Test
    void stockAdjustRequestSupportsNoArgConstructor() {
        StockAdjustRequest request = new StockAdjustRequest();
        List<StockItemRequest> items = List.of(new StockItemRequest("prod-1", 2));

        request.setItems(items);

        assertThat(request.getItems()).containsExactlyElementsOf(items);
    }

    @Test
    void productResponseSupportsBuilderSettersAndValueSemantics() {
        Instant created = Instant.parse("2026-03-25T10:00:00Z");
        ProductResponse response = ProductResponse.builder()
                .id("prod-1")
                .name("Product")
                .description("A product description")
                .price(BigDecimal.valueOf(10.0))
                .quantity(2)
                .userId("seller-1")
                .imagesIds(List.of("img-1"))
                .category(Category.ELECTRONICS)
                .createdAt(created)
                .updatedAt(created)
                .build();

        ProductResponse copy = ProductResponse.builder()
                .id("prod-1")
                .name("Product")
                .description("A product description")
                .price(BigDecimal.valueOf(10.0))
                .quantity(2)
                .userId("seller-1")
                .imagesIds(List.of("img-1"))
                .category(Category.ELECTRONICS)
                .createdAt(created)
                .updatedAt(created)
                .build();

        assertThat(response).isEqualTo(copy);
        assertThat(response).hasSameHashCodeAs(copy);
        assertThat(response.toString()).contains("prod-1");

        response.setId("prod-2");
        response.setName("Updated");
        response.setDescription("Updated description");
        response.setPrice(BigDecimal.valueOf(20.0));
        response.setQuantity(3);
        response.setUserId("seller-2");
        response.setImagesIds(List.of("img-2"));
        response.setCategory(Category.CLOTHING);
        response.setCreatedAt(created.plusSeconds(60));
        response.setUpdatedAt(created.plusSeconds(120));

        assertThat(response.getId()).isEqualTo("prod-2");
        assertThat(response.getName()).isEqualTo("Updated");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(20.0));
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getUserId()).isEqualTo("seller-2");
        assertThat(response.getImagesIds()).containsExactly("img-2");
        assertThat(response.getCategory()).isEqualTo(Category.CLOTHING);
        assertThat(response.getCreatedAt()).isEqualTo(created.plusSeconds(60));
        assertThat(response.getUpdatedAt()).isEqualTo(created.plusSeconds(120));
    }
}
