package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Rating
import de.emaarco.example.domain.Review

interface ReviewPlatformPort {
    fun findRating(hotelId: HotelId): Rating?

    fun findLatestReviews(
        hotelId: HotelId,
        limit: Int,
    ): List<Review>
}
