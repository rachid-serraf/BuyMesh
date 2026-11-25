package com.buy01.order.order.dto

import spock.lang.Specification

class ApiErrorSpec extends Specification {

    def "of without details creates an empty details list"() {
        when:
        def error = ApiError.of("VALIDATION", "Invalid request")

        then:
        error.code() == "VALIDATION"
        error.message() == "Invalid request"
        error.details().isEmpty()
    }

    def "of with details keeps the supplied details"() {
        when:
        def error = ApiError.of("VALIDATION", "Invalid request", ["field is required"])

        then:
        error.code() == "VALIDATION"
        error.message() == "Invalid request"
        error.details() == ["field is required"]
    }

    def "of with null details falls back to an empty list"() {
        when:
        def error = ApiError.of("VALIDATION", "Invalid request", null)

        then:
        error.details() != null
        error.details().isEmpty()
    }

    def "exposes constructor fields and implements equals, hashCode and toString"() {
        given:
        def first = new ApiError("VALIDATION", "Invalid request", ["field is required"])
        def same = new ApiError("VALIDATION", "Invalid request", ["field is required"])
        def different = new ApiError("NOT_FOUND", "Invalid request", ["field is required"])

        expect:
        first.code() == "VALIDATION"
        first.message() == "Invalid request"
        first.details() == ["field is required"]
        first == same
        first.hashCode() == same.hashCode()
        first != different
        !first.equals(null)
        !first.equals("other")
        first.toString().contains("VALIDATION")
    }

    def "covers null and populated equality and hash branches"() {
        given:
        def empty = new ApiError(null, null, null)
        def populated = new ApiError("VALIDATION", "Invalid request", ["field is required"])
        def populatedCopy = new ApiError("VALIDATION", "Invalid request", ["field is required"])

        expect:
        empty == new ApiError(null, null, null)
        empty.hashCode() == new ApiError(null, null, null).hashCode()
        empty != populated
        populated != empty
        populated == populatedCopy
        populated.hashCode() == populatedCopy.hashCode()
    }
}
