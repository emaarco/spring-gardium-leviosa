package de.emaarco.example.adapter.outbound

import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Stay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BookingEngineAdapterTest {

    private val adapter = BookingEngineAdapter()

    private val bayerischerHof = HotelId("muc-bayerischer-hof")
    private val pension = HotelId("muc-pension-viktualienmarkt")
    private val twoNights = Stay(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 13))

    @Test
    fun `offers the free rooms cheapest first and skips rooms booked during the stay`() {
        val offers = adapter.findAvailableRooms(setOf(bayerischerHof), twoNights)

        assertThat(offers.map { it.roomType.value }).containsExactly("Doppelzimmer", "Suite")
    }

    @Test
    fun `total price is the price per night times the number of nights`() {
        val suite = adapter.findAvailableRooms(setOf(bayerischerHof), twoNights).single { it.roomType.value == "Suite" }

        assertThat(suite.totalPriceInEuro).isEqualTo(1780)
    }

    @Test
    fun `fully booked hotel has no offers`() {
        assertThat(adapter.findAvailableRooms(setOf(pension), twoNights)).isEmpty()
    }

    @Test
    fun `only offers of the requested hotels are returned`() {
        val offers = adapter.findAvailableRooms(setOf(bayerischerHof, HotelId("unknown")), twoNights)

        assertThat(offers.map { it.hotelId }).containsOnly(bayerischerHof)
    }
}
