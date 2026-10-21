package de.emaarco.example.adapter.inbound.graphql

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.example.application.port.inbound.LoadAvailableRoomsQuery
import de.emaarco.example.application.port.inbound.SearchHotelsQuery
import de.emaarco.example.domain.Hotel
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.HotelName
import de.emaarco.example.domain.RoomOffer
import de.emaarco.example.domain.RoomType
import de.emaarco.example.domain.Stay
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.graphql.execution.ErrorType
import org.springframework.graphql.test.tester.GraphQlTester
import java.time.LocalDate

@GraphQlTest(controllers = [HotelSearchController::class, HotelAvailableRoomsDataLoader::class])
class HotelSearchControllerTest {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockkBean
    private lateinit var searchHotelsQuery: SearchHotelsQuery

    @MockkBean
    private lateinit var loadAvailableRoomsQuery: LoadAvailableRoomsQuery

    private val firstWeekend = Stay(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 13))
    private val secondWeekend = Stay(LocalDate.of(2026, 12, 18), LocalDate.of(2026, 12, 20))

    private val grandHotel = hotel("grand-hotel", "Grand Hotel")
    private val cityHotel = hotel("city-hotel", "City Hotel")
    private val pension = hotel("pension", "Pension")

    @Test
    fun `merges hotels of the content provider with offers of the booking engine`() {
        every { searchHotelsQuery.searchHotels("München") } returns listOf(grandHotel, pension)
        every { loadAvailableRoomsQuery.loadAvailableRooms(any(), firstWeekend) } returns
            listOf(offer(grandHotel, "Doppelzimmer", 320), offer(grandHotel, "Suite", 890))

        searchHotels("hotelSearch")
            .path("hotelSearch[*].name")
            .entityList(String::class.java)
            .containsExactly("Grand Hotel", "Pension")
            .path("hotelSearch[0].availableRooms[*].roomType")
            .entityList(String::class.java)
            .containsExactly("Doppelzimmer", "Suite")
            .path("hotelSearch[0].availableRooms[1].totalPriceInEuro")
            .entity(Int::class.java)
            .isEqualTo(1780)
            .path("hotelSearch[1].availableRooms")
            .matchesJson("[]")
    }

    @Test
    fun `does not ask the booking engine when no available rooms are selected`() {
        every { searchHotelsQuery.searchHotels("München") } returns listOf(grandHotel, pension)

        searchHotels("hotelSearchWithoutRooms")
            .path("hotelSearch[*].name")
            .entityList(String::class.java)
            .containsExactly("Grand Hotel", "Pension")

        verify(exactly = 0) { loadAvailableRoomsQuery.loadAvailableRooms(any(), any()) }
    }

    @Test
    fun `asks the booking engine in bulk instead of once per hotel`() {
        val hotels = listOf(grandHotel, cityHotel, pension)
        val requestedHotelIds = mutableListOf<Set<HotelId>>()
        every { searchHotelsQuery.searchHotels("München") } returns hotels
        every { loadAvailableRoomsQuery.loadAvailableRooms(capture(requestedHotelIds), firstWeekend) } returns
            emptyList()

        searchHotels("hotelSearch")
            .path("hotelSearch[*].availableRooms")
            .entityList(List::class.java)
            .hasSize(hotels.size)

        assertThat(requestedHotelIds).hasSizeLessThan(hotels.size)
        assertThat(requestedHotelIds.flatten()).containsExactlyInAnyOrderElementsOf(hotels.map { it.id })
    }

    @Test
    fun `keeps the offers of different stays apart`() {
        every { searchHotelsQuery.searchHotels("München") } returns listOf(pension)
        every { loadAvailableRoomsQuery.loadAvailableRooms(setOf(pension.id), firstWeekend) } returns emptyList()
        every { loadAvailableRoomsQuery.loadAvailableRooms(setOf(pension.id), secondWeekend) } returns
            listOf(offer(pension, "Einzelzimmer", 69))

        graphQlTester
            .documentName("hotelSearchTwoStays")
            .variable("city", "München")
            .execute()
            .path("firstWeekend[0].availableRooms")
            .matchesJson("[]")
            .path("secondWeekend[0].availableRooms[*].roomType")
            .entityList(String::class.java)
            .containsExactly("Einzelzimmer")

        verify(exactly = 1) { loadAvailableRoomsQuery.loadAvailableRooms(any(), firstWeekend) }
        verify(exactly = 1) { loadAvailableRoomsQuery.loadAvailableRooms(any(), secondWeekend) }
    }

    @Test
    fun `still returns the hotels when the booking engine fails`() {
        every { searchHotelsQuery.searchHotels("München") } returns listOf(grandHotel, pension)
        every { loadAvailableRoomsQuery.loadAvailableRooms(any(), any()) } throws
            IllegalStateException("Booking engine unavailable")

        searchHotels("hotelSearch")
            .errors()
            .satisfy { errors ->
                assertThat(errors).isNotEmpty.allSatisfy { assertThat(it.path).endsWith("availableRooms") }
            }.path("hotelSearch[*].name")
            .entityList(String::class.java)
            .containsExactly("Grand Hotel", "Pension")
            .path("hotelSearch[0].availableRooms")
            .valueIsNull()
    }

    @ParameterizedTest
    @CsvSource("2026-12-13, 2026-12-11", "2026-12-11, 2026-12-11", "11.12.2026, 2026-12-13")
    fun `rejects an invalid stay`(
        checkIn: String,
        checkOut: String,
    ) {
        searchHotels("hotelSearch", checkIn, checkOut)
            .errors()
            .satisfy { errors -> assertThat(errors.map { it.errorType }).contains(ErrorType.BAD_REQUEST) }

        verify(exactly = 0) { searchHotelsQuery.searchHotels(any()) }
        verify(exactly = 0) { loadAvailableRoomsQuery.loadAvailableRooms(any(), any()) }
    }

    private fun searchHotels(
        documentName: String,
        checkIn: String = "2026-12-11",
        checkOut: String = "2026-12-13",
    ) = graphQlTester
        .documentName(documentName)
        .variable("city", "München")
        .variable("checkIn", checkIn)
        .variable("checkOut", checkOut)
        .execute()

    private fun hotel(
        id: String,
        name: String,
    ) = Hotel(HotelId(id), HotelName(name), "München", 3)

    private fun offer(
        hotel: Hotel,
        roomType: String,
        pricePerNightInEuro: Int,
    ) = RoomOffer(hotel.id, RoomType(roomType), pricePerNightInEuro, pricePerNightInEuro * 2)
}
