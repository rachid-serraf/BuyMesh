package com.buy01.order.cart.client

import spock.lang.Specification

class StockItemRequestSpec extends Specification {

    def "exposes its constructor fields"() {
        when:
        def request = new StockItemRequest("product-1", 5)

        then:
        request.productId() == "product-1"
        request.quantity() == 5
    }

    def "implements equals, hashCode and toString"() {
        given:
        def first = new StockItemRequest("product-1", 5)
        def same = new StockItemRequest("product-1", 5)
        def different = new StockItemRequest("product-1", 6)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
        first.toString().contains("5")
    }
}
