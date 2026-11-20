package specs

import spock.lang.Specification
import support.AuthHelper

abstract class BaseE2ESpec extends Specification {

    def setupSpec() {
        AuthHelper.ensureUsersCreated()
    }

    def anonymous() { AuthHelper.anonymous() }
    def asBuyer()   { AuthHelper.as("CLIENT") }
    def asSeller()  { AuthHelper.as("SELLER") }
}