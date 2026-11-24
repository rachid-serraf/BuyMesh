package com.buy01.order.cart.dto

import com.buy01.order.order.model.entity.DeliveryAddress

import spock.lang.Specification

class CheckoutRequestSpec extends Specification {

    def "exposes the delivery address"() {
        given:
        def address = new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA")

        when:
        def request = new CheckoutRequest(address)

        then:
        request.address() == address
        request.address().fullName == "Ada Lovelace"
    }

    def "can be created through the builder"() {
        given:
        def address = new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA")

        when:
        def request = CheckoutRequest.builder()
                .address(address)
                .build()

        then:
        request.address() == address
    }

    def "implements equals, hashCode and toString"() {
        given:
        def address = new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA")
        def first = new CheckoutRequest(address)
        def same = new CheckoutRequest(address)
        def different = new CheckoutRequest(null)

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("address")
    }

    def "covers null and populated equality and hash branches"() {
        given:
        def empty = new CheckoutRequest(null)
        def populated = new CheckoutRequest(new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA"))
        def populatedCopy = new CheckoutRequest(new DeliveryAddress(
                "Ada Lovelace", "+1 555 0100", "1 Main St", "Apt 2",
                "Springfield", "IL", "62701", "USA"))

        expect:
        empty == new CheckoutRequest(null)
        empty.hashCode() == new CheckoutRequest(null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
