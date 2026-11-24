package com.buy01.order.order.dto

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

import org.springframework.data.domain.PageImpl

import com.buy01.order.common.Status
import com.buy01.order.order.model.entity.DeliveryAddress
import com.buy01.order.order.model.entity.Order
import com.buy01.order.order.model.entity.OrderItem

import spock.lang.Specification

class OrderDtoModelsSpec extends Specification {

    private static DeliveryAddress address() {
        new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA")
    }

    private static OrderItem item() {
        new OrderItem(
                "product-1", "seller-1", "Phone", "image-1", "ELECTRONICS",
                new BigDecimal("9.99"), 2)
    }

    private static Order order(List<OrderItem> items) {
        def now = Instant.now()
        new Order(
                "order-1", "checkout-1", "buyer-1", "seller-1", Status.PENDING,
                items, new BigDecimal("19.98"), address(), now, now)
    }

    private static OrderItemResponse itemResponse() {
        new OrderItemResponse("product-1", "seller-1", "Phone", "image-1",
                new BigDecimal("9.99"), 2)
    }

    def "OrderItemResponse exposes its constructor fields"() {
        when:
        def response = itemResponse()

        then:
        response.productId() == "product-1"
        response.sellerId() == "seller-1"
        response.name() == "Phone"
        response.imageUrl() == "image-1"
        response.price() == new BigDecimal("9.99")
        response.quantity() == 2
    }

    def "OrderItemResponse.from copies an order item"() {
        when:
        def response = OrderItemResponse.from(item())

        then:
        response.productId() == "product-1"
        response.sellerId() == "seller-1"
        response.name() == "Phone"
        response.imageUrl() == "image-1"
        response.price() == new BigDecimal("9.99")
        response.quantity() == 2
    }

    def "OrderItemResponse implements equals, hashCode and toString"() {
        given:
        def first = itemResponse()
        def same = itemResponse()
        def different = new OrderItemResponse("product-2", "seller-1", "Phone", "image-1",
                new BigDecimal("9.99"), 2)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "OrderResponse exposes its constructor fields"() {
        given:
        def items = [itemResponse()]
        def now = Instant.now()

        when:
        def response = new OrderResponse("order-1", "checkout-1", "buyer-1", "seller-1",
                Status.PENDING, items, new BigDecimal("19.98"), address(), now, now)

        then:
        response.id() == "order-1"
        response.checkoutId() == "checkout-1"
        response.buyerId() == "buyer-1"
        response.sellerId() == "seller-1"
        response.status() == Status.PENDING
        response.items() == items
        response.total() == new BigDecimal("19.98")
        response.address() == address()
        response.createdAt() == now
        response.updatedAt() == now
    }

    def "OrderResponse.from maps an order with items"() {
        when:
        def response = OrderResponse.from(order([item()]))

        then:
        response.id() == "order-1"
        response.checkoutId() == "checkout-1"
        response.buyerId() == "buyer-1"
        response.sellerId() == "seller-1"
        response.status() == Status.PENDING
        response.items().size() == 1
        response.items().first().productId() == "product-1"
        response.total() == new BigDecimal("19.98")
        response.address() == address()
    }

    def "OrderResponse.from uses an empty list when order items are null"() {
        when:
        def response = OrderResponse.from(order(null))

        then:
        response.items().isEmpty()
    }

    def "OrderResponse implements equals, hashCode and toString"() {
        given:
        def items = [itemResponse()]
        def now = Instant.now()
        def first = new OrderResponse("order-1", "checkout-1", "buyer-1", "seller-1",
                Status.PENDING, items, new BigDecimal("19.98"), address(), now, now)
        def same = new OrderResponse("order-1", "checkout-1", "buyer-1", "seller-1",
                Status.PENDING, items, new BigDecimal("19.98"), address(), now, now)
        def different = new OrderResponse("order-2", "checkout-1", "buyer-1", "seller-1",
                Status.PENDING, items, new BigDecimal("19.98"), address(), now, now)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("order-1")
    }

    def "BuyNowRequest exposes fields via constructor and builder"() {
        given:
        def address = address()

        when:
        def constructed = new BuyNowRequest("product-1", 2, address)
        def built = BuyNowRequest.builder()
                .productId("product-2")
                .quantity(3)
                .address(address)
                .build()

        then:
        constructed.productId() == "product-1"
        constructed.quantity() == 2
        constructed.address() == address
        built.productId() == "product-2"
        built.quantity() == 3
        built.address() == address
    }

    def "BuyNowRequest implements equals, hashCode and toString"() {
        given:
        def address = address()
        def first = new BuyNowRequest("product-1", 2, address)
        def same = new BuyNowRequest("product-1", 2, address)
        def different = new BuyNowRequest("product-2", 2, address)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "OrderQuery exposes its constructor fields"() {
        given:
        def from = LocalDate.of(2026, 1, 1)
        def to = LocalDate.of(2026, 1, 31)

        when:
        def query = new OrderQuery("phone", Status.DELIVERED, from, to, 1, 20)

        then:
        query.search() == "phone"
        query.status() == Status.DELIVERED
        query.from() == from
        query.to() == to
        query.page() == 1
        query.size() == 20
    }

    def "OrderQuery implements equals, hashCode and toString"() {
        given:
        def from = LocalDate.of(2026, 1, 1)
        def to = LocalDate.of(2026, 1, 31)
        def first = new OrderQuery("phone", Status.DELIVERED, from, to, 1, 20)
        def same = new OrderQuery("phone", Status.DELIVERED, from, to, 1, 20)
        def different = new OrderQuery("tablet", Status.DELIVERED, from, to, 1, 20)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("phone")
    }

    def "PageResponse exposes its constructor fields"() {
        when:
        def response = new PageResponse(["a", "b"], 1, 10, 12L, 2)

        then:
        response.content() == ["a", "b"]
        response.page() == 1
        response.size() == 10
        response.totalElements() == 12L
        response.totalPages() == 2
    }

    def "PageResponse.of copies a page"() {
        given:
        def page = new PageImpl(["a", "b"], org.springframework.data.domain.PageRequest.of(1, 2), 6L)

        when:
        def response = PageResponse.of(page)

        then:
        response.content() == ["a", "b"]
        response.page() == 1
        response.size() == 2
        response.totalElements() == 6L
        response.totalPages() == 3
    }

    def "PageResponse.of applies a mapper"() {
        given:
        def page = new PageImpl(["a", "b"], org.springframework.data.domain.PageRequest.of(0, 2), 4L)

        when:
        def response = PageResponse.of(page, { String value -> value.toUpperCase() })

        then:
        response.content() == ["A", "B"]
        response.page() == 0
        response.size() == 2
        response.totalElements() == 4L
        response.totalPages() == 2
    }

    def "PageResponse implements equals, hashCode and toString"() {
        given:
        def first = new PageResponse(["a"], 0, 10, 1L, 1)
        def same = new PageResponse(["a"], 0, 10, 1L, 1)
        def different = new PageResponse(["b"], 0, 10, 1L, 1)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("a")
    }

    def "OrderItemResponse covers null and populated equality and hash branches"() {
        given:
        def empty = new OrderItemResponse(null, null, null, null, null, 0)
        def populated = itemResponse()
        def populatedCopy = itemResponse()

        expect:
        empty == new OrderItemResponse(null, null, null, null, null, 0)
        empty.hashCode() == new OrderItemResponse(null, null, null, null, null, 0).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "OrderResponse covers null and populated equality and hash branches"() {
        given:
        def empty = new OrderResponse(null, null, null, null, null, null, null, null, null, null)
        def populated = new OrderResponse("order-1", "checkout-1", "buyer-1", "seller-1",
                Status.PENDING, [itemResponse()], new BigDecimal("19.98"), address(),
                Instant.EPOCH, Instant.EPOCH)
        def populatedCopy = new OrderResponse("order-1", "checkout-1", "buyer-1", "seller-1",
                Status.PENDING, [itemResponse()], new BigDecimal("19.98"), address(),
                Instant.EPOCH, Instant.EPOCH)

        expect:
        empty == new OrderResponse(null, null, null, null, null, null, null, null, null, null)
        empty.hashCode() == new OrderResponse(null, null, null, null, null, null, null, null, null, null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "BuyNowRequest covers null and populated equality and hash branches"() {
        given:
        def empty = new BuyNowRequest(null, 0, null)
        def populated = new BuyNowRequest("product-1", 2, address())
        def populatedCopy = new BuyNowRequest("product-1", 2, address())

        expect:
        empty == new BuyNowRequest(null, 0, null)
        empty.hashCode() == new BuyNowRequest(null, 0, null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "OrderQuery covers null and populated equality and hash branches"() {
        given:
        def empty = new OrderQuery(null, null, null, null, 0, 0)
        def populated = new OrderQuery("phone", Status.DELIVERED,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1, 20)
        def populatedCopy = new OrderQuery("phone", Status.DELIVERED,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1, 20)

        expect:
        empty == new OrderQuery(null, null, null, null, 0, 0)
        empty.hashCode() == new OrderQuery(null, null, null, null, 0, 0).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "PageResponse covers null and populated equality and hash branches"() {
        given:
        def empty = new PageResponse(null, 0, 0, 0L, 0)
        def populated = new PageResponse(["a"], 1, 10, 1L, 1)
        def populatedCopy = new PageResponse(["a"], 1, 10, 1L, 1)

        expect:
        empty == new PageResponse(null, 0, 0, 0L, 0)
        empty.hashCode() == new PageResponse(null, 0, 0, 0L, 0).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
