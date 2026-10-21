package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.Rating

data class RatingDto(
    val score: Double,
    val reviewCount: Int,
) {
    companion object {
        fun from(rating: Rating) =
            RatingDto(
                score = rating.score,
                reviewCount = rating.reviewCount,
            )
    }
}
