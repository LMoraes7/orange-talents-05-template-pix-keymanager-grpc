package br.com.zup.edu.grpc.enpoint.consultar.todas.request

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import javax.validation.Validator

@MicronautTest
internal class ConsultarTodasChavePixRequestDtoTest(
    private val validator: Validator
) {

    @Test
    internal fun `deve validar`() {
        val dto = ConsultarTodasChavePixRequestDto(clienteId = UUID.randomUUID().toString())
        val errors = this.validator.validate(dto)

        assertTrue(errors.isEmpty())
    }

    @Test
    internal fun `nao deve validar quando clienteId vier nulo, em branco e invalido`() {
        var dto = ConsultarTodasChavePixRequestDto(clienteId = null)
        var errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        dto = ConsultarTodasChavePixRequestDto(clienteId = "")
        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        dto = ConsultarTodasChavePixRequestDto(clienteId = "clienteId")
        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())
    }
}