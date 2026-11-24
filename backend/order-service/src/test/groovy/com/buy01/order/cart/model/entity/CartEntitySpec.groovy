package com.buy01.order.cart.model.entity

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

import spock.lang.Specification

class CartEntitySpec extends Specification {

    private static CartItem item() {
        new CartItem("product-1", "seller-1", "Phone", new BigDecimal("9.99"),
                "image-1", 2, "ELECTRONICS")
    }

    def "Cart supports no-args and setters"() {
        given:
        def userId = UUID.randomUUID()
        def cart = new Cart()
        cart.setId("cart-1")
        cart.setUserId(userId)
        cart.setItems([item()])
        cart.setCreatedAt(Instant.EPOCH)
        cart.setUpdatedAt(Instant.EPOCH)

        expect:
        cart.id == "cart-1"
        cart.userId == userId
        cart.items.size() == 1
        cart.createdAt == Instant.EPOCH
        cart.updatedAt == Instant.EPOCH
    }

    def "Cart covers null and populated equality and hash branches"() {
        given:
        def empty = new Cart()
        def item = item()
        def populated = new Cart(
                id: "cart-1", userId: UUID.randomUUID(), items: [item],
                createdAt: Instant.EPOCH, updatedAt: Instant.EPOCH)
        def populatedCopy = new Cart(
                id: populated.id, userId: populated.userId, items: [item],
                createdAt: Instant.EPOCH, updatedAt: Instant.EPOCH)

        expect:
        empty == new Cart()
        empty.hashCode() == new Cart().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
        populated.toString().contains("cart-1")
    }
}
