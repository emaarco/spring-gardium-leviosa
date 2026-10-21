package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.RoomOffer
import de.emaarco.example.domain.Stay

interface LoadAvailableRoomsQuery {
    fun loadAvailableRooms(
        hotelIds: Set<HotelId>,
        stay: Stay,
    ): List<RoomOffer>
}
