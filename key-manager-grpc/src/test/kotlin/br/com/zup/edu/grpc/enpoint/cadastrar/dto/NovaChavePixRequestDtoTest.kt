package br.com.zup.edu.grpc.enpoint.cadastrar.dto

import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.enpoint.cadastrar.dto.NovaChavePixRequestDto
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import javax.validation.Validator

@MicronautTest
internal class NovaChavePixRequestDtoTest(
    private val validator: Validator
) {

    @Test
    internal fun `deve validar`() {
        val dto = NovaChavePixRequestDto(
            clienteId = UUID.randomUUID().toString(),
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE
        )

        val errors = this.validator.validate(dto)

        assertTrue(errors.isEmpty())
    }

    @Test
    internal fun `nao deve validar quando clienteId vier em nulo, em branco ou invalido`() {
        var dto = NovaChavePixRequestDto(
            clienteId = null,
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE
        )

        var errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        dto = NovaChavePixRequestDto(
            clienteId = "",
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE
        )

        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())

        dto = NovaChavePixRequestDto(
            clienteId = "dshjn5tre",
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE
        )

        errors = this.validator.validate(dto)

        assertTrue(errors.isNotEmpty())
    }
}