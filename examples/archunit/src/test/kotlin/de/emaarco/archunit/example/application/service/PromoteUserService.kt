package de.emaarco.archunit.example.application.service

import de.emaarco.archunit.example.application.port.inbound.PromoteUserUseCase
import de.emaarco.archunit.example.application.port.inbound.RegisterUserUseCase

/**
 * NEGATIVE case: implements its own use case but calls another one — rejected.
 *
 * It invokes [RegisterUserUseCase.register] on an injected inbound port. The rule forbids accessing
 * inbound *port interfaces* (not their command/query data types), and that call targets a port
 * interface, so this class is flagged.
 */
class PromoteUserService(
    private val registerUser: RegisterUserUseCase,
) : PromoteUserUseCase {
    override fun promote(command: PromoteUserUseCase.Command) {
        registerUser.register(RegisterUserUseCase.Command(command.name))
    }
}
