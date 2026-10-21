package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.RoomAvailabilityPort
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.RoomOffer
import de.emaarco.example.domain.RoomType
import de.emaarco.example.domain.Stay
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class BookingEngineAdapter : RoomAvailabilityPort {

    private val log = KotlinLogging.logger {}

    private val inventory =
        listOf(
            Room("muc-bayerischer-hof", "Einzelzimmer", 240, setOf(LocalDate.of(2026, 12, 12))),
            Room("muc-bayerischer-hof", "Doppelzimmer", 320),
            Room("muc-bayerischer-hof", "Suite", 890),
            Room("muc-isarblick", "Einzelzimmer", 95),
            Room("muc-isarblick", "Doppelzimmer", 129, setOf(LocalDate.of(2026, 12, 11))),
            Room("muc-pension-viktualienmarkt", "Einzelzimmer", 69, setOf(LocalDate.of(2026, 12, 11))),
            Room("ham-elbufer", "Doppelzimmer", 149),
        )

    override fun findAvailableRooms(
        hotelIds: Set<HotelId>,
        stay: Stay,
    ): List<RoomOffer> {
        log.info { "Booking engine requested for ${hotelIds.size} hotel(s) and $stay" }
        val nights = stay.nights()
        return inventory
            .filter { HotelId(it.hotelId) in hotelIds && it.isFreeDuring(nights) }
            .sortedBy { it.pricePerNightInEuro }
            .map { it.toOffer(nights.size) }
    }

    private data class Room(
        val hotelId: String,
        val roomType: String,
        val pricePerNightInEuro: Int,
        val bookedNights: Set<LocalDate> = emptySet(),
    ) {
        fun isFreeDuring(nights: List<LocalDate>) = nights.none { it in bookedNights }

        fun toOffer(numberOfNights: Int) =
            RoomOffer(
                hotelId = HotelId(hotelId),
                roomType = RoomType(roomType),
                pricePerNightInEuro = pricePerNightInEuro,
                totalPriceInEuro = pricePerNightInEuro * numberOfNights,
            )
    }
}
