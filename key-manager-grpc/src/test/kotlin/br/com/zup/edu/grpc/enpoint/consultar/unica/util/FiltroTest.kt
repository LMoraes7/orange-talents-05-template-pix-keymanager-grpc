package br.com.zup.edu.grpc.enpoint.consultar.unica.util

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import javax.validation.Validator

@MicronautTest
internal class FiltroTest(
    private val validator: Validator,
) {

    @Test
    internal fun `PorClienteEhChaveId deve validar`() {
        val classe = Filtro.PorClienteEhChaveId(
            clienteId = UUID.randomUUID().toString(),
            pixId = UUID.randomUUID().toString()
        )

        val errors = this.validator.validate(classe)

        assertTrue(errors.isEmpty())
    }

    @Test
    internal fun `PorClienteEhChaveId nao deve validar clienteId quando ele for nulo, em braco e invalido`() {
        var classe: Filtro = Filtro.PorClienteEhChaveId(
            clienteId = null,
            pixId = UUID.randomUUID().toString()
        )
        var errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())

        classe = Filtro.PorClienteEhChaveId(
            clienteId = "",
            pixId = UUID.randomUUID().toString()
        )
        errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())

        classe = Filtro.PorClienteEhChaveId(
            clienteId = "clienteId",
            pixId = UUID.randomUUID().toString()
        )
        errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())
    }

    @Test
    internal fun `PorClienteEhChaveId nao deve validar pixId quando ele for nulo, em braco e invalido`() {
        var classe: Filtro = Filtro.PorClienteEhChaveId(
            clienteId = UUID.randomUUID().toString(),
            pixId = null
        )
        var errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())

        classe = Filtro.PorClienteEhChaveId(
            clienteId = UUID.randomUUID().toString(),
            pixId = ""
        )
        errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())

        classe = Filtro.PorClienteEhChaveId(
            clienteId = UUID.randomUUID().toString(),
            pixId = "pixId"
        )
        errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())
    }

    @Test
    internal fun `PorChavePix deve validar`() {
        val classe = Filtro.PorChavePix(
            chave = "chave"
        )

        val errors = this.validator.validate(classe)

        assertTrue(errors.isEmpty())
    }

    @Test
    internal fun `PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres`() {
        var classe: Filtro = Filtro.PorChavePix(
            chave = null
        )
        var errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())

        classe = Filtro.PorChavePix(
            chave = ""
        )
        errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())

        classe = Filtro.PorChavePix(
            chave = "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres" +
                    "PorChavePix nao deve validar chave quando ela for nula, em bracoe maior do que 77 caracteres"
        )
        errors = this.validator.validate(classe)
        assertTrue(errors.isNotEmpty())
    }
}