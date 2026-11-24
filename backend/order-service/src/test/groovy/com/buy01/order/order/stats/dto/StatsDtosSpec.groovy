package com.buy01.order.order.stats.dto

import java.math.BigDecimal

import spock.lang.Specification

class StatsDtosSpec extends Specification {

    def "BestSellingProductResponse supports all args, no args, setters and builder"() {
        when:
        def allArgs = new BestSellingProductResponse(
                "product-1", "Phone", "image-1", 6L, new BigDecimal("99.90"))
        def noArgs = new BestSellingProductResponse()
        noArgs.setProductId("product-2")
        noArgs.setName("Tablet")
        noArgs.setImageUrl("image-2")
        noArgs.setUnitsSold(2L)
        noArgs.setRevenue(new BigDecimal("49.95"))
        def built = BestSellingProductResponse.builder()
                .productId("product-3")
                .name("Laptop")
                .imageUrl("image-3")
                .unitsSold(1L)
                .revenue(new BigDecimal("999.00"))
                .build()

        then:
        allArgs.productId == "product-1"
        allArgs.name == "Phone"
        allArgs.imageUrl == "image-1"
        allArgs.unitsSold == 6L
        allArgs.revenue == new BigDecimal("99.90")
        noArgs.productId == "product-2"
        noArgs.name == "Tablet"
        noArgs.imageUrl == "image-2"
        noArgs.unitsSold == 2L
        noArgs.revenue == new BigDecimal("49.95")
        built.productId == "product-3"
        built.name == "Laptop"
        built.imageUrl == "image-3"
        built.unitsSold == 1L
        built.revenue == new BigDecimal("999.00")
    }

    def "BestSellingProductResponse implements equals, hashCode and toString"() {
        given:
        def first = new BestSellingProductResponse("product-1", "Phone", "image-1", 6L, new BigDecimal("99.90"))
        def same = new BestSellingProductResponse("product-1", "Phone", "image-1", 6L, new BigDecimal("99.90"))
        def different = new BestSellingProductResponse("product-2", "Phone", "image-1", 6L, new BigDecimal("99.90"))

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "ProductStatsResponse supports all args, no args, setters and builder"() {
        when:
        def allArgs = new ProductStatsResponse(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
        def noArgs = new ProductStatsResponse()
        noArgs.setProductId("product-2")
        noArgs.setName("Tablet")
        noArgs.setImageUrl("image-2")
        noArgs.setQuantity(3L)
        noArgs.setAmountSpent(new BigDecimal("29.97"))
        def built = ProductStatsResponse.builder()
                .productId("product-3")
                .name("Laptop")
                .imageUrl("image-3")
                .quantity(1L)
                .amountSpent(new BigDecimal("999.00"))
                .build()

        then:
        allArgs.productId == "product-1"
        allArgs.name == "Phone"
        allArgs.imageUrl == "image-1"
        allArgs.quantity == 2L
        allArgs.amountSpent == new BigDecimal("19.98")
        noArgs.productId == "product-2"
        noArgs.name == "Tablet"
        noArgs.imageUrl == "image-2"
        noArgs.quantity == 3L
        noArgs.amountSpent == new BigDecimal("29.97")
        built.productId == "product-3"
        built.name == "Laptop"
        built.imageUrl == "image-3"
        built.quantity == 1L
        built.amountSpent == new BigDecimal("999.00")
    }

    def "ProductStatsResponse implements equals, hashCode and toString"() {
        given:
        def first = new ProductStatsResponse("product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
        def same = new ProductStatsResponse("product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
        def different = new ProductStatsResponse("product-1", "Phone", "image-1", 3L, new BigDecimal("19.98"))

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "CategoryStatsResponse supports all args, no args, setters and builder"() {
        when:
        def allArgs = new CategoryStatsResponse("ELECTRONICS", 2L, new BigDecimal("19.98"))
        def noArgs = new CategoryStatsResponse()
        noArgs.setCategory("BOOKS")
        noArgs.setQuantity(3L)
        noArgs.setAmountSpent(new BigDecimal("29.97"))
        def built = CategoryStatsResponse.builder()
                .category("HOME")
                .quantity(1L)
                .amountSpent(new BigDecimal("999.00"))
                .build()

        then:
        allArgs.category == "ELECTRONICS"
        allArgs.quantity == 2L
        allArgs.amountSpent == new BigDecimal("19.98")
        noArgs.category == "BOOKS"
        noArgs.quantity == 3L
        noArgs.amountSpent == new BigDecimal("29.97")
        built.category == "HOME"
        built.quantity == 1L
        built.amountSpent == new BigDecimal("999.00")
    }

    def "CategoryStatsResponse implements equals, hashCode and toString"() {
        given:
        def first = new CategoryStatsResponse("ELECTRONICS", 2L, new BigDecimal("19.98"))
        def same = new CategoryStatsResponse("ELECTRONICS", 2L, new BigDecimal("19.98"))
        def different = new CategoryStatsResponse("BOOKS", 2L, new BigDecimal("19.98"))

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("ELECTRONICS")
    }
}
