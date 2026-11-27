package specs

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import spock.lang.Shared
import support.AuthHelper
import support.E2EConfig
import support.TestUsers

import java.util.Base64

class BuyerFlowSpec extends BaseE2ESpec {

    private static final String PIXEL_PNG =
            'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wl2nS8AAAAASUVORK5CYII='

    @Shared String productName = "E2E Buyer Target ${UUID.randomUUID().toString().take(8)}"
    @Shared String mediaId
    @Shared String productId
    @Shared String sellerId

    def setupSpec() {
        AuthHelper.ensureUsersCreated()

        byte[] image = Base64.decoder.decode(PIXEL_PNG)
        def upload = asSeller()
                .contentType(ContentType.MULTIPART)
                .multiPart('files', 'e2e-pixel.png', image, 'image/png')
                .post('/media')
        assert upload.statusCode() == 201
        mediaId = upload.jsonPath().getString('[0].imagesId')

        def body = [
                name       : productName,
                description: 'A product created for the E2E buyer flow',
                price      : 19.99,
                quantity   : 5,
                imagesIds  : [mediaId],
                category   : 'ELECTRONICS'
        ]
        def created = asSeller().body(body).post('/products')
        assert created.statusCode() == 201
        productId = created.jsonPath().getString('id')
        sellerId = created.jsonPath().getString('userId')
    }

    def "buyer discovers the product in the catalog and search"() {
        when:
        def catalog = anonymous().get('/products')
        def search = anonymous()
                .queryParam('q', productName)
                .get('/products/search')

        then:
        catalog.statusCode() == 200
        catalog.jsonPath().getList('name').contains(productName)

        search.statusCode() == 200
        search.jsonPath().getLong('totalElements') >= 1
        search.jsonPath().getList('content.name').contains(productName)
    }

    def "buyer opens the full product details and image"() {
        when:
        def detail = anonymous().get("/products/${productId}")
        def image = anonymous().get("/media/${mediaId}")

        then:
        detail.statusCode() == 200
        detail.jsonPath().getString('id') == productId
        detail.jsonPath().getString('name') == productName
        detail.jsonPath().getString('userId') == sellerId
        detail.jsonPath().getString('category') == 'ELECTRONICS'

        image.statusCode() == 200
        image.contentType().startsWith('image/')
    }

    def "buyer adds the product to cart, checks out and sees the order"() {
        given:
        def buyer = freshBuyer()
        def address = [
                fullName  : 'E2E Buyer',
                phone     : '0600000000',
                line1     : '1 Test Street',
                city      : 'Oujda',
                state     : 'Oujda',
                postalCode: '60000',
                country   : 'MA'
        ]

        when:
        def add = authed(buyer.token)
                .body([productId: productId, quantity: 2])
                .post('/cart/items')
        def cart = authed(buyer.token).get('/cart/items')
        def checkout = authed(buyer.token)
                .body([address: address])
                .post('/cart/checkout')
        def orderId = checkout.jsonPath().getString('orders[0].id')
        def myOrders = authed(buyer.token).get('/orders/me')
        def order = authed(buyer.token).get("/orders/${orderId}")

        then:
        add.statusCode() == 200
        add.jsonPath().getString('userId') == buyer.id
        add.jsonPath().getList('items.productId').contains(productId)

        cart.statusCode() == 200
        cart.jsonPath().getList('items.productId').contains(productId)

        checkout.statusCode() == 200
        checkout.jsonPath().getBoolean('valid') == true
        orderId

        myOrders.statusCode() == 200
        myOrders.jsonPath().getList('content.id').contains(orderId)

        order.statusCode() == 200
        order.jsonPath().getString('status') == 'PENDING'
        order.jsonPath().getString('sellerId') == sellerId
    }

    private Map freshBuyer() {
        def user = TestUsers.newBuyer()
        def created = AuthHelper.register(user)
        def session = AuthHelper.login(user)
        return [id: created.id, email: user.email, token: session.token]
    }

    private RequestSpecification authed(String token) {
        RestAssured.given().baseUri(E2EConfig.BASE_URL).relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .header('Authorization', "Bearer ${token}")
    }
}
