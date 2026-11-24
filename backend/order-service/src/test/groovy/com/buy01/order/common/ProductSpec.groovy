package com.buy01.order.common

import spock.lang.Specification

class ProductSpec extends Specification {

    def "supports no-args, all-args and setters"() {
        when:
        def allArgs = new Product("product-1", "Phone", 9.99d)
        def noArgs = new Product()
        noArgs.setId("product-2")
        noArgs.setName("Tablet")
        noArgs.setPrice(19.99d)

        then:
        allArgs.id == "product-1"
        allArgs.name == "Phone"
        allArgs.price == 9.99d
        noArgs.id == "product-2"
        noArgs.name == "Tablet"
        noArgs.price == 19.99d
    }

    def "implements equals, hashCode and toString"() {
        given:
        def first = new Product("product-1", "Phone", 9.99d)
        def same = new Product("product-1", "Phone", 9.99d)
        def different = new Product("product-2", "Phone", 9.99d)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("product-1")
    }

    def "covers null and populated equality and hash branches"() {
        given:
        def empty = new Product()
        def populated = new Product("product-1", "Phone", 9.99d)
        def populatedCopy = new Product("product-1", "Phone", 9.99d)

        expect:
        empty == new Product()
        empty.hashCode() == new Product().hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
