package de.emaarco.example

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester

@SpringBootTest
@AutoConfigureGraphQlTester
class HotelSearchIntegrationTest {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @Test
    fun `combines hotels of the content provider with offers of the booking engine`() {
        graphQlTester
            .documentName("hotelSearch")
            .variable("city", "München")
            .variable("checkIn", "2026-12-11")
            .variable("checkOut", "2026-12-13")
            .execute()
            .path("hotelSearch[*].name")
            .entityList(String::class.java)
            .containsExactly("Hotel Bayerischer Hof", "Hotel Isarblick", "Pension am Viktualienmarkt")
            .path("hotelSearch[0].availableRooms[*].roomType")
            .entityList(String::class.java)
            .containsExactly("Doppelzimmer", "Suite")
            .path("hotelSearch[0].availableRooms[0].totalPriceInEuro")
            .entity(Int::class.java)
            .isEqualTo(640)
            .path("hotelSearch[1].availableRooms[*].roomType")
            .entityList(String::class.java)
            .containsExactly("Einzelzimmer")
            .path("hotelSearch[2].availableRooms")
            .matchesJson("[]")
    }
}
