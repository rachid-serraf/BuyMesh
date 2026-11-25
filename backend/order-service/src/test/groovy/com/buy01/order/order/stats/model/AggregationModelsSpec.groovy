package com.buy01.order.order.stats.model

import java.math.BigDecimal

import spock.lang.Specification

class AggregationModelsSpec extends Specification {

    def "BuyerSummaryAggregation supports no-args, all-args, setters and builder"() {
        when:
        def allArgs = new BuyerSummaryAggregation(new BigDecimal("19.98"), 1L)
        def noArgs = new BuyerSummaryAggregation()
        noArgs.setTotalSpent(new BigDecimal("29.97"))
        noArgs.setOrdersCount(2L)
        def built = BuyerSummaryAggregation.builder()
                .totalSpent(new BigDecimal("39.96"))
                .ordersCount(3L)
                .build()

        then:
        allArgs.totalSpent == new BigDecimal("19.98")
        allArgs.ordersCount == 1L
        noArgs.totalSpent == new BigDecimal("29.97")
        noArgs.ordersCount == 2L
        built.totalSpent == new BigDecimal("39.96")
        built.ordersCount == 3L
    }

    def "BuyerSummaryAggregation covers null and populated equality and hash branches"() {
        given:
        def empty = new BuyerSummaryAggregation()
        def populated = new BuyerSummaryAggregation(new BigDecimal("19.98"), 1L)
        def populatedCopy = new BuyerSummaryAggregation(new BigDecimal("19.98"), 1L)

        expect:
        empty == new BuyerSummaryAggregation()
        empty.hashCode() == new BuyerSummaryAggregation().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
        populated.toString().contains("19.98")
    }

    def "CategoryAggregation supports no-args, all-args, setters and builder"() {
        when:
        def allArgs = new CategoryAggregation("ELECTRONICS", 1L, new BigDecimal("19.98"))
        def noArgs = new CategoryAggregation()
        noArgs.setCategory("BOOKS")
        noArgs.setQuantity(2L)
        noArgs.setAmountSpent(new BigDecimal("29.97"))
        def built = CategoryAggregation.builder()
                .category("HOME")
                .quantity(3L)
                .amountSpent(new BigDecimal("39.96"))
                .build()

        then:
        allArgs.category == "ELECTRONICS"
        allArgs.quantity == 1L
        allArgs.amountSpent == new BigDecimal("19.98")
        noArgs.category == "BOOKS"
        noArgs.quantity == 2L
        noArgs.amountSpent == new BigDecimal("29.97")
        built.category == "HOME"
        built.quantity == 3L
        built.amountSpent == new BigDecimal("39.96")
    }

    def "CategoryAggregation covers null and populated equality and hash branches"() {
        given:
        def empty = new CategoryAggregation()
        def populated = new CategoryAggregation("ELECTRONICS", 1L, new BigDecimal("19.98"))
        def populatedCopy = new CategoryAggregation("ELECTRONICS", 1L, new BigDecimal("19.98"))

        expect:
        empty == new CategoryAggregation()
        empty.hashCode() == new CategoryAggregation().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
        populated.toString().contains("ELECTRONICS")
    }

    def "MonthlyAggregation supports no-args, all-args, setters and builder"() {
        when:
        def allArgs = new MonthlyAggregation("2026-11", new BigDecimal("19.98"), 1L)
        def noArgs = new MonthlyAggregation()
        noArgs.setMonth("2026-12")
        noArgs.setRevenue(new BigDecimal("29.97"))
        noArgs.setUnitsSold(2L)
        def built = MonthlyAggregation.builder()
                .month("2027-01")
                .revenue(new BigDecimal("39.96"))
                .unitsSold(3L)
                .build()

        then:
        allArgs.month == "2026-11"
        allArgs.revenue == new BigDecimal("19.98")
        allArgs.unitsSold == 1L
        noArgs.month == "2026-12"
        noArgs.revenue == new BigDecimal("29.97")
        noArgs.unitsSold == 2L
        built.month == "2027-01"
        built.revenue == new BigDecimal("39.96")
        built.unitsSold == 3L
    }

    def "MonthlyAggregation covers null and populated equality and hash branches"() {
        given:
        def empty = new MonthlyAggregation()
        def populated = new MonthlyAggregation("2026-11", new BigDecimal("19.98"), 1L)
        def populatedCopy = new MonthlyAggregation("2026-11", new BigDecimal("19.98"), 1L)

        expect:
        empty == new MonthlyAggregation()
        empty.hashCode() == new MonthlyAggregation().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
        populated.toString().contains("2026-11")
    }

    def "SellerSummaryAggregation supports no-args, all-args, setters and builder"() {
        when:
        def allArgs = new SellerSummaryAggregation(new BigDecimal("19.98"), 1L, 2L)
        def noArgs = new SellerSummaryAggregation()
        noArgs.setTotalRevenue(new BigDecimal("29.97"))
        noArgs.setOrdersCount(2L)
        noArgs.setUnitsSold(3L)
        def built = SellerSummaryAggregation.builder()
                .totalRevenue(new BigDecimal("39.96"))
                .ordersCount(3L)
                .unitsSold(4L)
                .build()

        then:
        allArgs.totalRevenue == new BigDecimal("19.98")
        allArgs.ordersCount == 1L
        allArgs.unitsSold == 2L
        noArgs.totalRevenue == new BigDecimal("29.97")
        noArgs.ordersCount == 2L
        noArgs.unitsSold == 3L
        built.totalRevenue == new BigDecimal("39.96")
        built.ordersCount == 3L
        built.unitsSold == 4L
    }

    def "SellerSummaryAggregation covers null and populated equality and hash branches"() {
        given:
        def empty = new SellerSummaryAggregation()
        def populated = new SellerSummaryAggregation(new BigDecimal("19.98"), 1L, 2L)
        def populatedCopy = new SellerSummaryAggregation(new BigDecimal("19.98"), 1L, 2L)

        expect:
        empty == new SellerSummaryAggregation()
        empty.hashCode() == new SellerSummaryAggregation().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
        populated.toString().contains("19.98")
    }
}
