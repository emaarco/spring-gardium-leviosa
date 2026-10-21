package de.emaarco.example.domain

import java.time.LocalDate

data class Review(
    val author: String,
    val score: Int,
    val text: String,
    val writtenOn: LocalDate,
)
