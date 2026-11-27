package specs

import io.restassured.http.ContentType
import spock.lang.Shared
import support.AuthHelper

import java.util.Base64

class SellerFlowSpec extends BaseE2ESpec {

    private static final String PIXEL_PNG =
            'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wl2nS8AAAAASUVORK5CYII='

    @Shared String productName = "E2E Seller Product ${UUID.randomUUID().toString().take(8)}"
    @Shared String mediaId
    @Shared String productId

    def setupSpec() {
        AuthHelper.ensureUsersCreated()
    }

    def "seller uploads a product image"() {
        given:
        byte[] image = Base64.decoder.decode(PIXEL_PNG)

        when:
        def response = asSeller()
                .contentType(ContentType.MULTIPART)
                .multiPart('files', 'e2e-pixel.png', image, 'image/png')
                .post('/media')
        mediaId = response.jsonPath().getString('[0].imagesId')

        then:
        response.statusCode() == 201
        mediaId
    }

    def "seller creates a product"() {
        given:
        def body = [
                name       : productName,
                description: 'A product created by the E2E seller flow',
                price      : 19.99,
                quantity   : 5,
                imagesIds  : [mediaId],
                category   : 'ELECTRONICS'
        ]

        when:
        def response = asSeller().body(body).post('/products')
        productId = response.jsonPath().getString('id')

        then:
        response.statusCode() == 201
        productId
        response.jsonPath().getString('name') == productName
        response.jsonPath().getString('userId') == AuthHelper.idFor('SELLER')
    }

    def "seller sees the new product in the dashboard"() {
        when:
        def response = asSeller().get('/products/user')

        then:
        response.statusCode() == 200
        response.jsonPath().getList('id').contains(productId)
        response.jsonPath().getList('name').contains(productName)
    }

    def "seller can open the orders dashboard"() {
        when:
        def response = asSeller().get('/orders/seller')

        then:
        response.statusCode() == 200
        response.jsonPath().getList('content') != null
    }

    def "seller can read their analytics"() {
        when:
        def response = asSeller().get('/orders/stats/seller')

        then:
        response.statusCode() == 200
        response.jsonPath().get('totalRevenue') != null
        response.jsonPath().getLong('ordersCount') >= 0
        response.jsonPath().getLong('unitsSold') >= 0
        response.jsonPath().getList('bestSellingProducts') != null
        response.jsonPath().getList('revenueByMonth').size() == 6
    }
}
