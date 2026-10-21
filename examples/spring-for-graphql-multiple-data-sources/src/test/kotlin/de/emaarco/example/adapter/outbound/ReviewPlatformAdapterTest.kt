package de.emaarco.example.adapter.outbound

import de.emaarco.example.domain.HotelId
import de.emaarco.example.domain.Rating
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReviewPlatformAdapterTest {

    private val adapter = ReviewPlatformAdapter()

    private val bayerischerHof = HotelId("muc-bayerischer-hof")
    private val pension = HotelId("muc-pension-viktualienmarkt")

    @Test
    fun `rating is the average score rounded to one decimal`() {
        assertThat(adapter.findRating(bayerischerHof)).isEqualTo(Rating(score = 4.7, reviewCount = 3))
    }

    @Test
    fun `hotel without reviews has no rating`() {
        assertThat(adapter.findRating(pension)).isNull()
    }

    @Test
    fun `latest reviews come newest first and are limited`() {
        val reviews = adapter.findLatestReviews(bayerischerHof, 2)

        assertThat(reviews.map { it.author }).containsExactly("Clara", "Ben")
    }
}
