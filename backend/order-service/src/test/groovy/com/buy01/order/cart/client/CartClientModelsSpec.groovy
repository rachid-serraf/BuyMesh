package com.buy01.order.cart.client

import java.math.BigDecimal

import spock.lang.Specification

class CartClientModelsSpec extends Specification {

    def "StockAdjustRequest exposes its items"() {
        given:
        def items = [new StockItemRequest("product-1", 2)]

        when:
        def request = new StockAdjustRequest(items)

        then:
        request.items() == items
    }

    def "StockAdjustRequest implements equals, hashCode and toString"() {
        given:
        def first = new StockAdjustRequest([new StockItemRequest("product-1", 2)])
        def same = new StockAdjustRequest([new StockItemRequest("product-1", 2)])
        def different = new StockAdjustRequest([new StockItemRequest("product-2", 2)])

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "ProductResponse supports all-args, legacy constructor and builder"() {
        given:
        def images = ["image-1", "image-2"]

        when:
        def allArgs = new ProductResponse("product-1", "Phone", new BigDecimal("9.99"),
                10, "seller-1", images, "ELECTRONICS")
        def legacy = new ProductResponse("product-2", "Tablet", new BigDecimal("19.99"),
                5, "seller-2", ["image-3"])
        def built = ProductResponse.builder()
                .id("product-3")
                .name("Laptop")
                .price(new BigDecimal("999.00"))
                .quantity(1)
                .userId("seller-3")
                .imagesIds(["image-4"])
                .category("HOME")
                .build()

        then:
        allArgs.id() == "product-1"
        allArgs.name() == "Phone"
        allArgs.price() == new BigDecimal("9.99")
        allArgs.quantity() == 10
        allArgs.userId() == "seller-1"
        allArgs.imagesIds() == images
        allArgs.category() == "ELECTRONICS"
        legacy.id() == "product-2"
        legacy.name() == "Tablet"
        legacy.price() == new BigDecimal("19.99")
        legacy.quantity() == 5
        legacy.userId() == "seller-2"
        legacy.imagesIds() == ["image-3"]
        legacy.category() == null
        built.id() == "product-3"
        built.name() == "Laptop"
        built.price() == new BigDecimal("999.00")
        built.quantity() == 1
        built.userId() == "seller-3"
        built.imagesIds() == ["image-4"]
        built.category() == "HOME"
    }

    def "ProductResponse implements equals, hashCode and toString"() {
        given:
        def first = new ProductResponse("product-1", "Phone", new BigDecimal("9.99"),
                10, "seller-1", ["image-1"], "ELECTRONICS")
        def same = new ProductResponse("product-1", "Phone", new BigDecimal("9.99"),
                10, "seller-1", ["image-1"], "ELECTRONICS")
        def different = new ProductResponse("product-2", "Phone", new BigDecimal("9.99"),
                10, "seller-1", ["image-1"], "ELECTRONICS")

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
        ProductResponse.builder().id("product-1").build().toString().contains("product-1")
    }

    def "StockAdjustRequest covers null and populated equality and hash branches"() {
        given:
        def empty = new StockAdjustRequest(null)
        def populated = new StockAdjustRequest([new StockItemRequest("product-1", 2)])
        def populatedCopy = new StockAdjustRequest([new StockItemRequest("product-1", 2)])

        expect:
        empty == new StockAdjustRequest(null)
        empty.hashCode() == new StockAdjustRequest(null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }

    def "ProductResponse covers null and populated equality and hash branches"() {
        given:
        def empty = new ProductResponse(null, null, null, 0, null, null, null)
        def populated = new ProductResponse("product-1", "Phone", new BigDecimal("9.99"),
                10, "seller-1", ["image-1"], "ELECTRONICS")
        def populatedCopy = new ProductResponse("product-1", "Phone", new BigDecimal("9.99"),
                10, "seller-1", ["image-1"], "ELECTRONICS")

        expect:
        empty == new ProductResponse(null, null, null, 0, null, null, null)
        empty.hashCode() == new ProductResponse(null, null, null, 0, null, null, null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
