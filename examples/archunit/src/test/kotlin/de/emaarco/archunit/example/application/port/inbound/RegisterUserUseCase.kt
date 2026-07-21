package de.emaarco.archunit.example.application.port.inbound

/** Inbound port of the third example service — a state-changing use case with its own command. */
interface RegisterUserUseCase {
    fun register(command: Command)

    data class Command(
        val name: String,
    )
}
