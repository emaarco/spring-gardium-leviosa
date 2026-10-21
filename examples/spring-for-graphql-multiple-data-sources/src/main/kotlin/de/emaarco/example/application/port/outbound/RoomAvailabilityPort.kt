package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.RoomOffer
import de.emaarco.example.domain.Stay

interface RoomAvailabilityPort {
    fun findAvailableRooms(
        hotelIds: Set<HotelId>,
        stay: Stay,
    ): List<RoomOffer>
}
