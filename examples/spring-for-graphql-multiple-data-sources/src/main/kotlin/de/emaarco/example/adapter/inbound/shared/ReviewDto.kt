package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.Review

data class ReviewDto(
    val author: String,
    val score: Int,
    val text: String,
    val writtenOn: String,
) {
    companion object {
        fun from(review: Review) =
            ReviewDto(
                author = review.author,
                score = review.score,
                text = review.text,
                writtenOn = review.writtenOn.toString(),
            )
    }
}
