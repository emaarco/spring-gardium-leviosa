package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.ReviewPlatformPort
import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Rating
import de.emaarco.example.domain.Review
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.math.roundToInt

@Component
class ReviewPlatformAdapter : ReviewPlatformPort {

    private val log = KotlinLogging.logger {}

    private val reviewsPerHotel =
        mapOf(
            HotelId("muc-bayerischer-hof") to
                listOf(
                    Review("Anna", 5, "Legendary bar and flawless service.", LocalDate.of(2026, 9, 14)),
                    Review("Ben", 4, "Great location, the room was a bit noisy.", LocalDate.of(2026, 10, 2)),
                    Review("Clara", 5, "Breakfast on the roof terrace is a dream.", LocalDate.of(2026, 10, 18)),
                ),
            HotelId("muc-isarblick") to
                listOf(
                    Review("David", 4, "Quiet and right next to the river.", LocalDate.of(2026, 8, 30)),
                    Review("Eva", 3, "Clean, but small rooms.", LocalDate.of(2026, 10, 11)),
                ),
        )

    override fun findRating(hotelId: HotelId): Rating? {
        log.info { "Review platform requested for the rating of ${hotelId.value}" }
        val reviews = reviewsPerHotel[hotelId] ?: return null
        val averageScore = reviews.map { it.score }.average()
        return Rating(score = (averageScore * 10).roundToInt() / 10.0, reviewCount = reviews.size)
    }

    override fun findLatestReviews(
        hotelId: HotelId,
        limit: Int,
    ): List<Review> {
        log.info { "Review platform requested for the latest $limit reviews of ${hotelId.value}" }
        return reviewsPerHotel[hotelId]
            .orEmpty()
            .sortedByDescending { it.writtenOn }
            .take(limit)
    }
}
