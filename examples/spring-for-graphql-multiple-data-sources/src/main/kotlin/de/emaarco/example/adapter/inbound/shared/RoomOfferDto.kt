package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.RoomOffer

data class RoomOfferDto(
    val roomType: String,
    val pricePerNightInEuro: Int,
    val totalPriceInEuro: Int,
) {
    companion object {
        fun from(offer: RoomOffer) =
            RoomOfferDto(
                roomType = offer.roomType.value,
                pricePerNightInEuro = offer.pricePerNightInEuro,
                totalPriceInEuro = offer.totalPriceInEuro,
            )
    }
}
