package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.HotelDto
import de.emaarco.example.adapter.inbound.shared.RoomOfferDto
import de.emaarco.example.application.port.inbound.LoadAvailableRoomsQuery
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Stay
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class HotelAvailableRoomsDataLoader(
    private val query: LoadAvailableRoomsQuery,
) {

    @BatchMapping(typeName = "Hotel", field = "availableRooms")
    fun availableRooms(hotels: List<HotelDto>): List<List<RoomOfferDto>> {
        val offersPerStay = loadOffersPerStay(hotels)
        return hotels.map { offersPerStay.getValue(it.stay)[HotelId(it.id)].orEmpty() }
    }

    private fun loadOffersPerStay(hotels: List<HotelDto>): Map<Stay, Map<HotelId, List<RoomOfferDto>>> =
        hotels
            .groupBy({ it.stay }, { HotelId(it.id) })
            .mapValues { (stay, hotelIds) -> loadOffersPerHotel(hotelIds.toSet(), stay) }

    private fun loadOffersPerHotel(
        hotelIds: Set<HotelId>,
        stay: Stay,
    ): Map<HotelId, List<RoomOfferDto>> =
        query
            .loadAvailableRooms(hotelIds, stay)
            .groupBy({ it.hotelId }, { RoomOfferDto.from(it) })
}
