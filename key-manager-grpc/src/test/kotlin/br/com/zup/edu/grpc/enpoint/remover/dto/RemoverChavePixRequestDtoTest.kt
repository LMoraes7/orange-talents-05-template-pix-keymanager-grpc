package br.com.zup.edu.grpc.enpoint.remover.dto

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import javax.validation.Validator

@MicronautTest
internal class RemoverChavePixRequestDtoTest(
    private val validator: Validator
) {

    @Test
    internal fun `deve validar`() {
        val dto = RemoverChavePixRequestDto(
            pixId = UUID.randomUUID().toString(),
            clienteId = UUID.randomUUID().toString()
        )

        val errors = this.validator.validate(dto)

        assertTrue(errors.isEmpty())
    }

    @Test
    internal fun `nao deve validar quando pixId vier nulo, em branco e invalido`() {
        var dto = RemoverChavePixRequestDto(
            pixId = null,
            clienteId = UUID.randomUUID().toString()
        )

        var errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        //-------------

        dto = RemoverChavePixRequestDto(
            pixId = "",
            clienteId = UUID.randomUUID().toString()
        )

        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        //-------------

        dto = RemoverChavePixRequestDto(
            pixId = "ghjyresfrh",
            clienteId = UUID.randomUUID().toString()
        )

        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())
    }

    @Test
    internal fun `nao deve validar quando clienteId vier nulo, em branco e invalido`() {
        var dto = RemoverChavePixRequestDto(
            pixId = UUID.randomUUID().toString(),
            clienteId = null
        )

        var errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        //-------------

        dto = RemoverChavePixRequestDto(
            pixId = UUID.randomUUID().toString(),
            clienteId = ""
        )

        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        //-------------

        dto = RemoverChavePixRequestDto(
            pixId = UUID.randomUUID().toString(),
            clienteId = "null"
        )

        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())
    }
}