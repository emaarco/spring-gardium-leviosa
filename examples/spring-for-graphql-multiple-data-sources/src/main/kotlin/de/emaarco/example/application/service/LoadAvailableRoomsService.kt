package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.LoadAvailableRoomsQuery
import de.emaarco.example.application.port.outbound.RoomAvailabilityPort
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Stay
import org.springframework.stereotype.Service

@Service
class LoadAvailableRoomsService(
    private val roomAvailability: RoomAvailabilityPort,
) : LoadAvailableRoomsQuery {
    override fun loadAvailableRooms(
        hotelIds: Set<HotelId>,
        stay: Stay,
    ) = roomAvailability.findAvailableRooms(hotelIds, stay)
}
