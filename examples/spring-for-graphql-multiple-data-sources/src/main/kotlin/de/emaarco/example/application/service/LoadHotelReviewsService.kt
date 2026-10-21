package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.LoadHotelReviewsQuery
import de.emaarco.example.application.port.outbound.ReviewPlatformPort
import de.emaarco.example.domain.HotelId
import org.springframework.stereotype.Service

@Service
class LoadHotelReviewsService(
    private val reviewPlatform: ReviewPlatformPort,
) : LoadHotelReviewsQuery {
    override fun loadRating(hotelId: HotelId) = reviewPlatform.findRating(hotelId)

    override fun loadLatestReviews(
        hotelId: HotelId,
        limit: Int,
    ) = reviewPlatform.findLatestReviews(hotelId, limit)
}
