package com.buy01.order.order.dto

import java.math.BigDecimal

import com.buy01.order.common.Status
import com.buy01.order.order.model.entity.DeliveryAddress

import spock.lang.Specification

class OrderRequestDtosSpec extends Specification {

    private static DeliveryAddress address() {
        new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA")
    }

    private static OrderItemRequest item() {
        new OrderItemRequest(
                "product-1", "seller-1", "Phone", "image-1",
                new BigDecimal("9.99"), 2, "ELECTRONICS")
    }

    def "UpdateOrderStatusRequest exposes status via constructor and builder"() {
        when:
        def constructed = new UpdateOrderStatusRequest(Status.CONFIRMED)
        def built = UpdateOrderStatusRequest.builder()
                .status(Status.SHIPPED)
                .build()

        then:
        constructed.status() == Status.CONFIRMED
        built.status() == Status.SHIPPED
    }

    def "UpdateOrderStatusRequest implements equals, hashCode and toString"() {
        given:
        def first = new UpdateOrderStatusRequest(Status.CONFIRMED)
        def same = new UpdateOrderStatusRequest(Status.CONFIRMED)
        def different = new UpdateOrderStatusRequest(Status.SHIPPED)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("CONFIRMED")
    }

    def "CreateOrderRequest exposes fields via constructor and builder"() {
        given:
        def address = address()
        def items = [item()]

        when:
        def constructed = new CreateOrderRequest("checkout-1", "seller-1", items, address)
        def built = CreateOrderRequest.builder()
                .checkoutId("checkout-2")
                .sellerId("seller-2")
                .items(items)
                .address(address)
                .build()

        then:
        constructed.checkoutId() == "checkout-1"
        constructed.sellerId() == "seller-1"
        constructed.items() == items
        constructed.address() == address
        built.checkoutId() == "checkout-2"
        built.sellerId() == "seller-2"
        built.items() == items
        built.address() == address
    }

    def "CreateOrderRequest implements equals, hashCode and toString"() {
        given:
        def first = new CreateOrderRequest("checkout-1", "seller-1", [item()], address())
        def same = new CreateOrderRequest("checkout-1", "seller-1", [item()], address())
        def different = new CreateOrderRequest("checkout-2", "seller-1", [item()], address())

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("checkout-1")
    }

    def "OrderItemRequest supports legacy and full constructors"() {
        when:
        def legacy = new OrderItemRequest(
                "product-1", "seller-1", "Phone", "image-1", new BigDecimal("9.99"), 2)
        def full = item()

        then:
        legacy.productId() == "product-1"
        legacy.sellerId() == "seller-1"
        legacy.name() == "Phone"
        legacy.imageUrl() == "image-1"
        legacy.price() == new BigDecimal("9.99")
        legacy.quantity() == 2
        legacy.category() == null
        full.category() == "ELECTRONICS"
    }
}
