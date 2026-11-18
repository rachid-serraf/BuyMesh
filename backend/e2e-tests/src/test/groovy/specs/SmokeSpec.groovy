package specs

import io.restassured.RestAssured
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class SmokeSpec extends Specification {

  @Shared String base = System.getProperty("baseUrl", "http://localhost:18080")

  def setupSpec() {
    RestAssured.baseURI = base
    RestAssured.useRelaxedHTTPSValidation()

    new PollingConditions(timeout: 60, delay: 2).eventually {
      assert RestAssured.get("/actuator/health").statusCode() == 200
    }
  }

  def "gateway is up"() {
    expect:
    RestAssured.get("/actuator/health").statusCode() == 200
  }

  def "protected endpoint rejects anonymous calls"() {
    expect:
    RestAssured.post("/orders/checkout").statusCode() in [401, 403]
  }
}