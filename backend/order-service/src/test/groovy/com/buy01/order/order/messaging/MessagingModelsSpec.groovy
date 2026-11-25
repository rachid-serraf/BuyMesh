package com.buy01.order.order.messaging

import spock.lang.Specification

class MessagingModelsSpec extends Specification {

    def "OrderStockEvent exposes its fields"() {
        given:
        def items = [new OrderStockItem("product-1", 2)]

        when:
        def event = new OrderStockEvent("order-1", StockAction.RESERVE, items)

        then:
        event.orderId() == "order-1"
        event.action() == StockAction.RESERVE
        event.items() == items
    }

    def "OrderStockEvent implements equals, hashCode and toString"() {
        given:
        def items = [new OrderStockItem("product-1", 2)]
        def first = new OrderStockEvent("order-1", StockAction.RESERVE, items)
        def same = new OrderStockEvent("order-1", StockAction.RESERVE, items)
        def different = new OrderStockEvent("order-2", StockAction.RESERVE, items)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("order-1")
    }

    def "OrderStockEvent covers null and populated equality and hash branches"() {
        given:
        def empty = new OrderStockEvent(null, null, null)
        def populated = new OrderStockEvent("order-1", StockAction.RESERVE,
                [new OrderStockItem("product-1", 2)])
        def populatedCopy = new OrderStockEvent("order-1", StockAction.RESERVE,
                [new OrderStockItem("product-1", 2)])

        expect:
        empty == new OrderStockEvent(null, null, null)
        empty.hashCode() == new OrderStockEvent(null, null, null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "OrderStockItem exposes its fields"() {
        when:
        def item = new OrderStockItem("product-1", 3)

        then:
        item.productId() == "product-1"
        item.quantity() == 3
    }

    def "StockAction exposes reserve and release"() {
        expect:
        StockAction.values() == [StockAction.RESERVE, StockAction.RELEASE] as StockAction[]
        StockAction.valueOf("RESERVE") == StockAction.RESERVE
        StockAction.valueOf("RELEASE") == StockAction.RELEASE
    }
}
