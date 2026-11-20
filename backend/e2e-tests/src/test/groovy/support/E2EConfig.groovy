package support

class E2EConfig {
    static final String BASE_URL = System.getProperty("e2e.baseUrl", 
    System.getenv("E2E_BASE_URL") ?: "https://localhost:18080")
}