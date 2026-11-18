package specs

import spock.lang.Specification

class SmokeSpec extends Specification {
  def "setup works"() { expect: 1 + 1 == 2 }
}