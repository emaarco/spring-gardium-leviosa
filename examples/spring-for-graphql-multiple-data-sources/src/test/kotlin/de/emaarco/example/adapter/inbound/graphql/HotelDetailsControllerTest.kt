package de.emaarco.example.adapter.inbound.graphql

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.example.application.port.inbound.LoadAvailableRoomsQuery
import de.emaarco.example.application.port.inbound.LoadHotelQuery
import de.emaarco.example.application.port.inbound.LoadHotelReviewsQuery
import de.emaarco.example.domain.Hotel
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.HotelName
import de.emaarco.example.domain.Rating
import de.emaarco.example.domain.Review
import de.emaarco.example.domain.RoomOffer
import de.emaarco.example.domain.RoomType
import de.emaarco.example.domain.Stay
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.graphql.execution.ErrorType
import org.springframework.graphql.test.tester.GraphQlTester
import java.time.LocalDate

@GraphQlTest(
    controllers = [
        HotelDetailsController::class,
        HotelReviewsController::class,
        HotelDetailsAvailableRoomsController::class,
    ],
)
class HotelDetailsControllerTest {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockkBean
    private lateinit var loadHotelQuery: LoadHotelQuery

    @MockkBean
    private lateinit var loadHotelReviewsQuery: LoadHotelReviewsQuery

    @MockkBean
    private lateinit var loadAvailableRoomsQuery: LoadAvailableRoomsQuery

    private val hotel = Hotel(HotelId("grand-hotel"), HotelName("Grand Hotel"), "München", 5)
    private val stay = Stay(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 13))

    @Test
    fun `combines content provider, review platform and booking engine into one hotel`() {
        every { loadHotelQuery.loadHotel(hotel.id) } returns hotel
        every { loadHotelReviewsQuery.loadRating(hotel.id) } returns Rating(4.7, 3)
        every { loadHotelReviewsQuery.loadLatestReviews(hotel.id, 2) } returns
            listOf(Review("Clara", 5, "Great breakfast.", LocalDate.of(2026, 10, 18)))
        every { loadAvailableRoomsQuery.loadAvailableRooms(setOf(hotel.id), stay) } returns
            listOf(RoomOffer(hotel.id, RoomType("Suite"), 890, 1780))

        loadHotelDetails()
            .path("hotel.name")
            .entity(String::class.java)
            .isEqualTo("Grand Hotel")
            .path("hotel.rating.score")
            .entity(Double::class.java)
            .isEqualTo(4.7)
            .path("hotel.reviews[*].author")
            .entityList(String::class.java)
            .containsExactly("Clara")
            .path("hotel.reviews[0].writtenOn")
            .entity(String::class.java)
            .isEqualTo("2026-10-18")
            .path("hotel.availableRooms[0].totalPriceInEuro")
            .entity(Int::class.java)
            .isEqualTo(1780)
    }

    @Test
    fun `only asks the sources whose fields are selected`() {
        every { loadHotelQuery.loadHotel(hotel.id) } returns hotel

        graphQlTester
            .documentName("hotelPreview")
            .variable("id", hotel.id.value)
            .execute()
            .path("hotel.name")
            .entity(String::class.java)
            .isEqualTo("Grand Hotel")

        verify(exactly = 0) { loadHotelReviewsQuery.loadRating(any()) }
        verify(exactly = 0) { loadHotelReviewsQuery.loadLatestReviews(any(), any()) }
        verify(exactly = 0) { loadAvailableRoomsQuery.loadAvailableRooms(any(), any()) }
    }

    @Test
    fun `still returns the hotel when the review platform fails`() {
        every { loadHotelQuery.loadHotel(hotel.id) } returns hotel
        every { loadHotelReviewsQuery.loadRating(any()) } throws IllegalStateException("Review platform unavailable")
        every { loadHotelReviewsQuery.loadLatestReviews(any(), any()) } returns emptyList()
        every { loadAvailableRoomsQuery.loadAvailableRooms(any(), any()) } returns emptyList()

        loadHotelDetails()
            .errors()
            .satisfy { errors -> assertThat(errors.map { it.path }).containsExactly("hotel.rating") }
            .path("hotel.name")
            .entity(String::class.java)
            .isEqualTo("Grand Hotel")
            .path("hotel.rating")
            .valueIsNull()
            .path("hotel.reviews")
            .matchesJson("[]")
    }

    @Test
    fun `rejects an invalid stay only for the available rooms`() {
        every { loadHotelQuery.loadHotel(hotel.id) } returns hotel
        every { loadHotelReviewsQuery.loadRating(hotel.id) } returns null
        every { loadHotelReviewsQuery.loadLatestReviews(hotel.id, 2) } returns emptyList()

        loadHotelDetails(checkIn = "2026-12-13", checkOut = "2026-12-11")
            .errors()
            .satisfy { errors ->
                assertThat(errors).singleElement().satisfies({
                    assertThat(it.errorType).isEqualTo(ErrorType.BAD_REQUEST)
                    assertThat(it.path).isEqualTo("hotel.availableRooms")
                })
            }.path("hotel.name")
            .entity(String::class.java)
            .isEqualTo("Grand Hotel")
            .path("hotel.availableRooms")
            .valueIsNull()

        verify(exactly = 0) { loadAvailableRoomsQuery.loadAvailableRooms(any(), any()) }
    }

    @Test
    fun `unknown hotel is null and asks no further source`() {
        every { loadHotelQuery.loadHotel(any()) } returns null

        loadHotelDetails(id = "unknown")
            .path("hotel")
            .valueIsNull()

        verify(exactly = 0) { loadHotelReviewsQuery.loadRating(any()) }
        verify(exactly = 0) { loadAvailableRoomsQuery.loadAvailableRooms(any(), any()) }
    }

    private fun loadHotelDetails(
        id: String = hotel.id.value,
        checkIn: String = "2026-12-11",
        checkOut: String = "2026-12-13",
    ) = graphQlTester
        .documentName("hotelDetails")
        .variable("id", id)
        .variable("checkIn", checkIn)
        .variable("checkOut", checkOut)
        .execute()
}
