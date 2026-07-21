package de.emaarco.example

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/**
 * Boots the full Spring application context to catch dependency-upgrade breakage
 * (bean wiring, autoconfiguration, GraphQL schema binding, and startup task seeding)
 * that structural architecture tests cannot detect.
 */
@SpringBootTest
class ApplicationSmokeTest {

    @Test
    fun contextLoads() {}
}
