package com.buy01.order.cart.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

import com.buy01.order.cart.model.entity.Cart
import com.buy01.order.cart.model.entity.CartItem
import com.buy01.order.common.Status
import com.buy01.order.order.dto.OrderResponse
import com.buy01.order.order.model.entity.DeliveryAddress

import spock.lang.Specification

class CartDtosSpec extends Specification {

    private static DeliveryAddress address() {
        new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA")
    }

    private static Cart cart() {
        new Cart(
                id: "cart-1",
                userId: UUID.randomUUID(),
                items: [new CartItem("product-1", "seller-1", "Phone",
                        new BigDecimal("9.99"), "image-1", 2, "ELECTRONICS")])
    }

    private static OrderResponse orderResponse() {
        new OrderResponse(
                "order-1", "checkout-1", "buyer-1", "seller-1", Status.PENDING,
                [], new BigDecimal("19.98"), address(), Instant.now(), Instant.now())
    }

    def "CartItemIssue exposes constructor fields and fluent accessors"() {
        when:
        def issue = new CartItemIssue("product-1", "Phone", CartIssueType.INSUFFICIENT_STOCK, "Only 1 left")

        then:
        issue.productId() == "product-1"
        issue.name() == "Phone"
        issue.type() == CartIssueType.INSUFFICIENT_STOCK
        issue.message() == "Only 1 left"
    }

    def "CartItemIssue implements equals, hashCode and toString"() {
        given:
        def first = new CartItemIssue("product-1", "Phone", CartIssueType.PRICE_CHANGED, "Price changed")
        def same = new CartItemIssue("product-1", "Phone", CartIssueType.PRICE_CHANGED, "Price changed")
        def different = new CartItemIssue("product-2", "Phone", CartIssueType.PRICE_CHANGED, "Price changed")

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "CartItemDTO can be instantiated and builder renders a string"() {
        expect:
        new CartItemDTO() != null
        CartItemDTO.AddRequest.builder().productId("product-1").quantity(2).build().toString().contains("product-1")
        CartItemDTO.UpdateRequest.builder().quantity(3).build().toString().contains("3")
    }

    def "CheckoutResponse exposes its constructor fields"() {
        given:
        def cart = cart()
        def issues = [new CartItemIssue("product-1", "Phone", CartIssueType.PRICE_CHANGED, "Price changed")]
        def orders = [orderResponse()]

        when:
        def response = new CheckoutResponse(false, issues, cart, orders)

        then:
        !response.valid()
        response.issues() == issues
        response.cart() == cart
        response.orders() == orders
    }

    def "CheckoutResponse implements equals, hashCode and toString"() {
        given:
        def cart = cart()
        def first = new CheckoutResponse(true, [], cart, [])
        def same = new CheckoutResponse(true, [], cart, [])
        def different = new CheckoutResponse(false, [], cart, [])

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("valid")
    }

    def "CartItemDTO.AddRequest exposes fields via constructor and builder"() {
        when:
        def constructed = new CartItemDTO.AddRequest("product-1", 2)
        def built = CartItemDTO.AddRequest.builder()
                .productId("product-1")
                .quantity(3)
                .build()

        then:
        constructed.productId() == "product-1"
        constructed.quantity() == 2
        built.productId() == "product-1"
        built.quantity() == 3
    }

    def "CartItemDTO.AddRequest implements equals, hashCode and toString"() {
        given:
        def first = new CartItemDTO.AddRequest("product-1", 2)
        def same = new CartItemDTO.AddRequest("product-1", 2)
        def different = new CartItemDTO.AddRequest("product-1", 3)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "CartItemDTO.UpdateRequest exposes fields via constructor and builder"() {
        when:
        def constructed = new CartItemDTO.UpdateRequest(4)
        def built = CartItemDTO.UpdateRequest.builder()
                .quantity(5)
                .build()

        then:
        constructed.quantity() == 4
        built.quantity() == 5
    }

    def "CartItemDTO.UpdateRequest implements equals, hashCode and toString"() {
        given:
        def first = new CartItemDTO.UpdateRequest(4)
        def same = new CartItemDTO.UpdateRequest(4)
        def different = new CartItemDTO.UpdateRequest(5)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("4")
    }
}
