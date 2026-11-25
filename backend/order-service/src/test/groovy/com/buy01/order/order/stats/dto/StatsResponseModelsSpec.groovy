package com.buy01.order.order.stats.dto

import java.math.BigDecimal

import com.buy01.order.order.stats.model.ProductAggregation

import spock.lang.Specification

class StatsResponseModelsSpec extends Specification {

    private static ProductStatsResponse product() {
        new ProductStatsResponse("product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
    }

    private static CategoryStatsResponse category() {
        new CategoryStatsResponse("ELECTRONICS", 2L, new BigDecimal("19.98"))
    }

    def "BuyerStatsResponse supports all-args, no-args, setters and builder"() {
        given:
        def products = [product()]
        def categories = [category()]

        when:
        def allArgs = new BuyerStatsResponse(
                new BigDecimal("19.98"), 1L, products, products, categories)
        def noArgs = new BuyerStatsResponse()
        noArgs.setTotalSpent(new BigDecimal("29.97"))
        noArgs.setOrdersCount(2L)
        noArgs.setMostBoughtProducts(products)
        noArgs.setBestProducts(products)
        noArgs.setTopCategories(categories)
        def built = BuyerStatsResponse.builder()
                .totalSpent(new BigDecimal("39.96"))
                .ordersCount(3L)
                .mostBoughtProducts(products)
                .bestProducts(products)
                .topCategories(categories)
                .build()

        then:
        allArgs.totalSpent == new BigDecimal("19.98")
        allArgs.ordersCount == 1L
        allArgs.mostBoughtProducts == products
        allArgs.bestProducts == products
        allArgs.topCategories == categories
        noArgs.totalSpent == new BigDecimal("29.97")
        noArgs.ordersCount == 2L
        noArgs.mostBoughtProducts == products
        noArgs.bestProducts == products
        noArgs.topCategories == categories
        built.totalSpent == new BigDecimal("39.96")
        built.ordersCount == 3L
        built.mostBoughtProducts == products
        built.bestProducts == products
        built.topCategories == categories
    }

    def "BuyerStatsResponse implements equals, hashCode and toString"() {
        given:
        def products = [product()]
        def first = new BuyerStatsResponse(new BigDecimal("19.98"), 1L, products, products, [category()])
        def same = new BuyerStatsResponse(new BigDecimal("19.98"), 1L, products, products, [category()])
        def different = new BuyerStatsResponse(new BigDecimal("29.97"), 1L, products, products, [category()])

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("19.98")
    }

    def "SellerStatsResponse supports all-args, no-args, setters and builder"() {
        given:
        def products = [new BestSellingProductResponse(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))]
        def months = [new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)]

        when:
        def allArgs = new SellerStatsResponse(
                new BigDecimal("19.98"), 1L, 2L, products, months)
        def noArgs = new SellerStatsResponse()
        noArgs.setTotalRevenue(new BigDecimal("29.97"))
        noArgs.setOrdersCount(2L)
        noArgs.setUnitsSold(3L)
        noArgs.setBestSellingProducts(products)
        noArgs.setRevenueByMonth(months)
        def built = SellerStatsResponse.builder()
                .totalRevenue(new BigDecimal("39.96"))
                .ordersCount(3L)
                .unitsSold(4L)
                .bestSellingProducts(products)
                .revenueByMonth(months)
                .build()

        then:
        allArgs.totalRevenue == new BigDecimal("19.98")
        allArgs.ordersCount == 1L
        allArgs.unitsSold == 2L
        allArgs.bestSellingProducts == products
        allArgs.revenueByMonth == months
        noArgs.totalRevenue == new BigDecimal("29.97")
        noArgs.ordersCount == 2L
        noArgs.unitsSold == 3L
        noArgs.bestSellingProducts == products
        noArgs.revenueByMonth == months
        built.totalRevenue == new BigDecimal("39.96")
        built.ordersCount == 3L
        built.unitsSold == 4L
        built.bestSellingProducts == products
        built.revenueByMonth == months
    }

    def "SellerStatsResponse implements equals, hashCode and toString"() {
        given:
        def products = [new BestSellingProductResponse(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))]
        def months = [new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)]
        def first = new SellerStatsResponse(new BigDecimal("19.98"), 1L, 2L, products, months)
        def same = new SellerStatsResponse(new BigDecimal("19.98"), 1L, 2L, products, months)
        def different = new SellerStatsResponse(new BigDecimal("29.97"), 1L, 2L, products, months)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("19.98")
    }

    def "MonthlyRevenueResponse supports all-args, no-args, setters and builder"() {
        when:
        def allArgs = new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)
        def noArgs = new MonthlyRevenueResponse()
        noArgs.setMonth("2026-12")
        noArgs.setRevenue(new BigDecimal("29.97"))
        noArgs.setUnitsSold(3L)
        def built = MonthlyRevenueResponse.builder()
                .month("2027-01")
                .revenue(new BigDecimal("39.96"))
                .unitsSold(4L)
                .build()

        then:
        allArgs.month == "2026-11"
        allArgs.revenue == new BigDecimal("19.98")
        allArgs.unitsSold == 2L
        noArgs.month == "2026-12"
        noArgs.revenue == new BigDecimal("29.97")
        noArgs.unitsSold == 3L
        built.month == "2027-01"
        built.revenue == new BigDecimal("39.96")
        built.unitsSold == 4L
    }

    def "MonthlyRevenueResponse implements equals, hashCode and toString"() {
        given:
        def first = new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)
        def same = new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)
        def different = new MonthlyRevenueResponse("2026-12", new BigDecimal("19.98"), 2L)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("2026-11")
    }

    def "ProductAggregation supports all-args, no-args, setters and builder"() {
        when:
        def allArgs = new ProductAggregation(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
        def noArgs = new ProductAggregation()
        noArgs.setProductId("product-2")
        noArgs.setName("Tablet")
        noArgs.setImageUrl("image-2")
        noArgs.setQuantity(3L)
        noArgs.setAmountSpent(new BigDecimal("29.97"))
        def built = ProductAggregation.builder()
                .productId("product-3")
                .name("Laptop")
                .imageUrl("image-3")
                .quantity(4L)
                .amountSpent(new BigDecimal("39.96"))
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
        built.quantity == 4L
        built.amountSpent == new BigDecimal("39.96")
    }

    def "ProductAggregation implements equals, hashCode and toString"() {
        given:
        def first = new ProductAggregation(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
        def same = new ProductAggregation(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
        def different = new ProductAggregation(
                "product-2", "Phone", "image-1", 2L, new BigDecimal("19.98"))

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "BuyerStatsResponse covers null and populated equality and hash branches"() {
        given:
        def empty = new BuyerStatsResponse()
        def populated = new BuyerStatsResponse(new BigDecimal("19.98"), 1L,
                [product()], [product()], [category()])
        def populatedCopy = new BuyerStatsResponse(new BigDecimal("19.98"), 1L,
                [product()], [product()], [category()])

        expect:
        empty == new BuyerStatsResponse()
        empty.hashCode() == new BuyerStatsResponse().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "SellerStatsResponse covers null and populated equality and hash branches"() {
        given:
        def products = [new BestSellingProductResponse(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))]
        def months = [new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)]
        def empty = new SellerStatsResponse()
        def populated = new SellerStatsResponse(new BigDecimal("19.98"), 1L, 2L, products, months)
        def populatedCopy = new SellerStatsResponse(new BigDecimal("19.98"), 1L, 2L, products, months)

        expect:
        empty == new SellerStatsResponse()
        empty.hashCode() == new SellerStatsResponse().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "MonthlyRevenueResponse covers null and populated equality and hash branches"() {
        given:
        def empty = new MonthlyRevenueResponse()
        def populated = new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)
        def populatedCopy = new MonthlyRevenueResponse("2026-11", new BigDecimal("19.98"), 2L)

        expect:
        empty == new MonthlyRevenueResponse()
        empty.hashCode() == new MonthlyRevenueResponse().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "ProductAggregation covers null and populated equality and hash branches"() {
        given:
        def empty = new ProductAggregation()
        def populated = new ProductAggregation(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))
        def populatedCopy = new ProductAggregation(
                "product-1", "Phone", "image-1", 2L, new BigDecimal("19.98"))

        expect:
        empty == new ProductAggregation()
        empty.hashCode() == new ProductAggregation().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
