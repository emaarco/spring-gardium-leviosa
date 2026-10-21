package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.Hotel

data class HotelDetailsDto(
    val id: String,
    val name: String,
    val stars: Int,
) {
    companion object {
        fun from(hotel: Hotel) =
            HotelDetailsDto(
                id = hotel.id.value,
                name = hotel.name.value,
                stars = hotel.stars,
            )
    }
}
