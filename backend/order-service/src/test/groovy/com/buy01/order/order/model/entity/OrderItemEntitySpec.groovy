package com.buy01.order.order.model.entity

import java.math.BigDecimal

import spock.lang.Specification

class OrderItemEntitySpec extends Specification {

    def "supports no-args, setters, all-args and builder"() {
        when:
        def allArgs = new OrderItem(
                "product-1", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)
        def noArgs = new OrderItem()
        noArgs.setProductId("product-2")
        noArgs.setSellerId("seller-2")
        noArgs.setName("Tablet")
        noArgs.setImageUrl("image-2")
        noArgs.setCategory("BOOKS")
        noArgs.setPrice(new BigDecimal("19.99"))
        noArgs.setQuantity(3)
        def built = OrderItem.builder()
                .productId("product-3")
                .sellerId("seller-3")
                .name("Laptop")
                .imageUrl("image-3")
                .category("HOME")
                .price(new BigDecimal("999.00"))
                .quantity(1)
                .build()

        then:
        allArgs.productId == "product-1"
        allArgs.sellerId == "seller-1"
        allArgs.name == "Phone"
        allArgs.imageUrl == "image-1"
        allArgs.category == "ELECTRONICS"
        allArgs.price == new BigDecimal("9.99")
        allArgs.quantity == 2
        noArgs.productId == "product-2"
        noArgs.sellerId == "seller-2"
        noArgs.name == "Tablet"
        noArgs.imageUrl == "image-2"
        noArgs.category == "BOOKS"
        noArgs.price == new BigDecimal("19.99")
        noArgs.quantity == 3
        built.productId == "product-3"
        built.sellerId == "seller-3"
        built.name == "Laptop"
        built.imageUrl == "image-3"
        built.category == "HOME"
        built.price == new BigDecimal("999.00")
        built.quantity == 1
    }

    def "implements equals, hashCode and toString"() {
        given:
        def first = new OrderItem(
                "product-1", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)
        def same = new OrderItem(
                "product-1", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)
        def different = new OrderItem(
                "product-2", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
        OrderItem.builder().productId("product-1").build().toString().contains("product-1")
    }

    def "covers null and populated equality and hash branches"() {
        given:
        def empty = new OrderItem()
        def populated = new OrderItem(
                "product-1", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)
        def populatedCopy = new OrderItem(
                "product-1", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)

        expect:
        empty == new OrderItem()
        empty.hashCode() == new OrderItem().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
