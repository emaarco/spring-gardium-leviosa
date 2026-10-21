package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.HotelDetailsDto
import de.emaarco.example.adapter.inbound.shared.RoomOfferDto
import de.emaarco.example.application.port.inbound.LoadAvailableRoomsQuery
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Stay
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate

@Controller
class HotelDetailsAvailableRoomsController(
    private val query: LoadAvailableRoomsQuery,
) {

    @SchemaMapping(typeName = "HotelDetails")
    fun availableRooms(
        hotel: HotelDetailsDto,
        @Argument checkIn: String,
        @Argument checkOut: String,
    ): List<RoomOfferDto> {
        val stay = Stay(LocalDate.parse(checkIn), LocalDate.parse(checkOut))
        return query.loadAvailableRooms(setOf(HotelId(hotel.id)), stay).map { RoomOfferDto.from(it) }
    }
}
