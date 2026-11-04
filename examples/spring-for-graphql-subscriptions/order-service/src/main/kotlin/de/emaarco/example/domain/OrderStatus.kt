package de.emaarco.example.domain

enum class OrderStatus {
    PLACED,
    PAYMENT_CONFIRMED,
    READY_TO_SHIP,
    SHIPPED,
    ;

    val isFinal get() = this == SHIPPED
}
