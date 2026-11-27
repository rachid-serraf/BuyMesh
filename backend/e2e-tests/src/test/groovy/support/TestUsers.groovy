package support

class TestUsers {
    private static final Map<String, Map> USERS = [:]

    static final String BUYER_ROLE = "CLIENT"
    static final String SELLER_ROLE = "SELLER"

    static Map buyer() { USERS.computeIfAbsent(BUYER_ROLE) { build(BUYER_ROLE) } }
    static Map seller() { USERS.computeIfAbsent(SELLER_ROLE) { build(SELLER_ROLE) } }

    static Map newBuyer() { build(BUYER_ROLE) }
    static Map newSeller() { build(SELLER_ROLE) }

    private static Map build(String role) {
        def id = UUID.randomUUID().toString().take(8)
        [name: "e2e-${role.toLowerCase()}-${id}",
         email: "e2e-${role.toLowerCase()}-${id}@test.com",
         password: "Passw0rd!123",
         role: role]
    }
}
