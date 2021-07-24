package br.com.zup.edu.grpc.dominio.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveModelTest {

    @Nested
    inner class ChaveCpfTest {
        val cpf = TipoChaveModel.CPF

        @Test
        internal fun `deve validar quando CPF for valido`() {
            assertTrue(cpf.valida("86221379709"))
        }

        @Test
        internal fun `nao deve validar quando o CPF for nulo, em branco e invalido`() {
            assertFalse(cpf.valida(null))
            assertFalse(cpf.valida(""))
            assertFalse(cpf.valida("18459460933"))
        }
    }

    @Nested
    inner class ChaveCnpjTest {

        val cnpj = TipoChaveModel.CNPJ

        @Test
        internal fun `deve validar`() {
            assertTrue(cnpj.valida("52.550.571/0001-01"))
        }

        @Test
        internal fun `nao deve validar quando CNPJ for em branco, nulo ou invalido`() {
            assertFalse(cnpj.valida(""))
            assertFalse(cnpj.valida(null))
            assertFalse(cnpj.valida("00.000.000/0100-00"))
        }
    }

    @Nested
    inner class ChaveCelularTest {

        val celular = TipoChaveModel.CELULAR

        @Test
        internal fun `deve validar`() {
            assertTrue(celular.valida("+5511987654321"))
        }

        @Test
        internal fun `nao deve validar quando celular vier em branco, nulo ou invalido`() {
            assertFalse(celular.valida(""))
            assertFalse(celular.valida(null))
            assertFalse(celular.valida("11987654321"))
            assertFalse(celular.valida("+55a11987654321"))
        }
    }

    @Nested
    inner class ChaveEmailTest {

        val tipoChave = TipoChaveModel.EMAIL

        @Test
        internal fun `deve validar`() {
            assertTrue(tipoChave.valida("yuri@email.com"))
        }

        @Test
        internal fun `nao deve validar quando o email vier invalido, em branco ou nulo`() {
            assertFalse(tipoChave.valida("yuri.com"))
            assertFalse(tipoChave.valida(""))
            assertFalse(tipoChave.valida(null))
        }
    }

    @Nested
    inner class ChaveAleatoriaTest {

        val aleatoria = TipoChaveModel.ALEATORIA

        @Test
        internal fun `deve validar quando a chave for nula ou em branco`() {
            assertTrue(aleatoria.valida(null))
            assertTrue(aleatoria.valida(""))
        }

        @Test
        internal fun `nao deve validar quando chave vier preenchida`() {
            assertFalse(aleatoria.valida("kugjyfg"))
        }
    }

}