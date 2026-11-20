package support

import groovy.json.JsonOutput
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

class AuthHelper {
    private static final Map<String, Map> USERS = [:]
    private static final Map<String, String> TOKENS = [:]
    private static final Map<String, String> IDS = [:]

    /**
     * Creates the shared buyer and seller accounts and logs them in before the
     * suite runs. Every later call is cached, so this is a no-op afterwards.
     */
    static synchronized void ensureUsersCreated() {
        [TestUsers.BUYER_ROLE, TestUsers.SELLER_ROLE].each { role ->
            tokenFor(role)
        }
    }

    static synchronized Map userFor(String role) {
        USERS.computeIfAbsent(role) {
            (role == TestUsers.SELLER_ROLE) ? TestUsers.seller() : TestUsers.buyer()
        }
    }

    static synchronized String tokenFor(String role) {
        TOKENS.computeIfAbsent(role) {
            def user = userFor(role)
            register(user)
            login(user).token
        }
    }

    static synchronized String idFor(String role) {
        IDS.computeIfAbsent(role) {
            RestAssured.given().baseUri(E2EConfig.BASE_URL).relaxedHTTPSValidation()
                    .header("Authorization", "Bearer ${tokenFor(role)}")
                    .get("/users/me")
                    .then().statusCode(200)
                    .extract().path("id")
        }
    }

    static Map register(Map user) {
        Response response = RestAssured.given()
                .baseUri(E2EConfig.BASE_URL).relaxedHTTPSValidation()
                .contentType(ContentType.MULTIPART)
                .multiPart("name", user.name)
                .multiPart("email", user.email)
                .multiPart("password", user.password)
                .multiPart("role", user.role)
                .post("/users/auth/register")

        if (!(response.statusCode() in [200, 201])) {
            throw new IllegalStateException(
                    "Registration failed for ${user.email}: " +
                            "${response.statusCode()} ${response.asString()}")
        }

        return response.as(Map)
    }

    static Map login(Map user) {
        return login(user.email as String, user.password as String)
    }

    static Map login(String email, String password) {
        Response response = RestAssured.given()
                .baseUri(E2EConfig.BASE_URL).relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .body(JsonOutput.toJson([email: email, password: password]))
                .post("/users/auth/login")

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Login failed for ${email}: " +
                            "${response.statusCode()} ${response.asString()}")
        }

        def token = response.path("token") as String
        if (!token) {
            throw new IllegalStateException("Login for ${email} returned no token")
        }

        return [token: token, expiresAt: response.path("expiresAt")]
    }

    static RequestSpecification anonymous() {
        RestAssured.given().baseUri(E2EConfig.BASE_URL).relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
    }

    static RequestSpecification as(String role) {
        anonymous().header("Authorization", "Bearer ${tokenFor(role)}")
    }
}
