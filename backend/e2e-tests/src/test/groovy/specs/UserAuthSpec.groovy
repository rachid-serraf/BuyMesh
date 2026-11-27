package specs

import io.restassured.RestAssured
import support.AuthHelper
import support.E2EConfig
import support.TestUsers

class UserAuthSpec extends BaseE2ESpec {

    def "creates a new buyer"() {
        given:
        def user = TestUsers.newBuyer()

        when:
        def created = AuthHelper.register(user)

        then:
        created.id
        created.email == user.email
        created.role == TestUsers.BUYER_ROLE
    }

    def "logs in a registered user and returns a token"() {
        given:
        def user = TestUsers.newSeller()
        AuthHelper.register(user)

        when:
        def session = AuthHelper.login(user)

        then:
        session.token
        !session.token.isBlank()
    }

    def "rejects invalid credentials"() {
        given:
        def user = TestUsers.newBuyer()
        AuthHelper.register(user)

        when:
        def response = anonymous()
                .body([email: user.email, password: "wrong-password"])
                .post("/users/auth/login")

        then:
        response.statusCode() in [400, 401]
    }

    def "the issued token grants access to a protected endpoint"() {
        given:
        def user = TestUsers.newBuyer()
        AuthHelper.register(user)
        def token = AuthHelper.login(user).token

        when:
        def response = RestAssured.given()
                .baseUri(E2EConfig.BASE_URL).relaxedHTTPSValidation()
                .header("Authorization", "Bearer ${token}")
                .get("/users/me")

        then:
        response.statusCode() == 200
        response.path("email") == user.email
    }
}
