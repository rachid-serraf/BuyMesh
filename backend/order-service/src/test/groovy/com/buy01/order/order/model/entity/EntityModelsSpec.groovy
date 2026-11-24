package com.buy01.order.order.model.entity

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

import com.buy01.order.common.Status

import spock.lang.Specification

class EntityModelsSpec extends Specification {

    private static DeliveryAddress address() {
        new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA")
    }

    private static OrderItem item() {
        new OrderItem("product-1", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)
    }

    private static Order order() {
        new Order("order-1", "checkout-1", "buyer-1", "seller-1", Status.PENDING,
                [item()], new BigDecimal("19.98"), address(), Instant.EPOCH, Instant.EPOCH)
    }

    def "Order supports no-args, all-args and builder"() {
        when:
        def allArgs = order()
        def noArgs = new Order()
        noArgs.setId("order-2")
        noArgs.setCheckoutId("checkout-2")
        noArgs.setBuyerId("buyer-2")
        noArgs.setSellerId("seller-2")
        noArgs.setStatus(Status.CONFIRMED)
        noArgs.setItems([item()])
        noArgs.setTotal(new BigDecimal("29.97"))
        noArgs.setAddress(address())
        noArgs.setCreatedAt(Instant.EPOCH)
        noArgs.setUpdatedAt(Instant.EPOCH)
        def built = Order.builder()
                .id("order-3")
                .checkoutId("checkout-3")
                .buyerId("buyer-3")
                .sellerId("seller-3")
                .status(Status.SHIPPED)
                .items([item()])
                .total(new BigDecimal("39.96"))
                .address(address())
                .createdAt(Instant.EPOCH)
                .updatedAt(Instant.EPOCH)
                .build()

        then:
        allArgs.id == "order-1"
        allArgs.checkoutId == "checkout-1"
        allArgs.buyerId == "buyer-1"
        allArgs.sellerId == "seller-1"
        allArgs.status == Status.PENDING
        allArgs.items.size() == 1
        allArgs.total == new BigDecimal("19.98")
        allArgs.address == address()
        allArgs.createdAt == Instant.EPOCH
        allArgs.updatedAt == Instant.EPOCH
        noArgs.id == "order-2"
        noArgs.checkoutId == "checkout-2"
        noArgs.buyerId == "buyer-2"
        noArgs.sellerId == "seller-2"
        noArgs.status == Status.CONFIRMED
        noArgs.items.size() == 1
        noArgs.total == new BigDecimal("29.97")
        noArgs.address == address()
        noArgs.createdAt == Instant.EPOCH
        noArgs.updatedAt == Instant.EPOCH
        built.id == "order-3"
        built.checkoutId == "checkout-3"
        built.buyerId == "buyer-3"
        built.sellerId == "seller-3"
        built.status == Status.SHIPPED
        built.items.size() == 1
        built.total == new BigDecimal("39.96")
        built.address == address()
        built.createdAt == Instant.EPOCH
        built.updatedAt == Instant.EPOCH
    }

    def "Order covers null and populated equality and hash branches"() {
        given:
        def empty = new Order()
        def populated = order()
        def populatedCopy = order()

        expect:
        empty == new Order()
        empty.hashCode() == new Order().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
        populated.toString().contains("order-1")
        Order.builder().id("order-1").build().toString().contains("order-1")
    }

    def "DeliveryAddress supports no-args, all-args and builder"() {
        when:
        def allArgs = address()
        def noArgs = new DeliveryAddress()
        noArgs.setFullName("Ada Lovelace")
        noArgs.setPhone("+1 555 0100")
        noArgs.setLine1("1 Main St")
        noArgs.setLine2("Apt 2")
        noArgs.setCity("Springfield")
        noArgs.setState("IL")
        noArgs.setPostalCode("62701")
        noArgs.setCountry("USA")
        def built = DeliveryAddress.builder()
                .fullName("Ada Lovelace")
                .phone("+1 555 0100")
                .line1("1 Main St")
                .line2("Apt 2")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .country("USA")
                .build()

        then:
        allArgs.fullName == "Ada Lovelace"
        allArgs.phone == "+1 555 0100"
        allArgs.line1 == "1 Main St"
        allArgs.line2 == "Apt 2"
        allArgs.city == "Springfield"
        allArgs.state == "IL"
        allArgs.postalCode == "62701"
        allArgs.country == "USA"
        noArgs == allArgs
        built == allArgs
    }

    def "DeliveryAddress covers null and populated equality and hash branches"() {
        given:
        def empty = new DeliveryAddress()
        def populated = address()
        def populatedCopy = address()

        expect:
        empty == new DeliveryAddress()
        empty.hashCode() == new DeliveryAddress().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
        populated.toString().contains("Ada Lovelace")
        DeliveryAddress.builder().fullName("Ada Lovelace").build().toString().contains("Ada Lovelace")
    }
}
