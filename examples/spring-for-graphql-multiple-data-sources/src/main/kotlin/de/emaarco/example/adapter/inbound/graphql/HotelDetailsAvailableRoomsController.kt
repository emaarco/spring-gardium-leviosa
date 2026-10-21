package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.HotelDetailsDto
import de.emaarco.example.adapter.inbound.shared.RoomOfferDto
import de.emaarco.example.application.port.inbound.LoadAvailableRoomsQuery
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Stay
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate

@Controller
class HotelDetailsAvailableRoomsController(
    private val query: LoadAvailableRoomsQuery,
) {

    private val log = KotlinLogging.logger {}

    @SchemaMapping(typeName = "HotelDetails")
    fun availableRooms(
        hotel: HotelDetailsDto,
        @Argument checkIn: String,
        @Argument checkOut: String,
    ): List<RoomOfferDto> {
        log.debug { "Resolving available rooms of hotel ${hotel.id} from $checkIn to $checkOut" }
        val stay = Stay(LocalDate.parse(checkIn), LocalDate.parse(checkOut))
        val offers = query.loadAvailableRooms(setOf(HotelId(hotel.id)), stay)
        return offers.map { RoomOfferDto.from(it) }
    }
}
