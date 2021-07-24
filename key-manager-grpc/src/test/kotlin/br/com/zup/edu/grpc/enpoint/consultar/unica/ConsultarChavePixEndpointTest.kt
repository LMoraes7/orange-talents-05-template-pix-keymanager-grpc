package br.com.zup.edu.grpc.enpoint.consultar.unica

import br.com.zup.edu.ConsultarChavePixRequestGrpc
import br.com.zup.edu.ConsultarChavePixRequestGrpc.*
import br.com.zup.edu.ConsultarChavePixServiceGrpc
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.modelo.Conta
import br.com.zup.edu.grpc.dominio.modelo.Instituicao
import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.enpoint.consultar.unica.util.Filtro
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.bcb.request.BankAccount
import br.com.zup.edu.grpc.http.client.bcb.request.KeyType
import br.com.zup.edu.grpc.http.client.bcb.request.Owner
import br.com.zup.edu.grpc.http.client.bcb.response.PixKeyDetailsResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultarChavePixEndpointTest(
    private val grpcClient: ConsultarChavePixServiceGrpc.ConsultarChavePixServiceBlockingStub,
    private val repository: ChavePixRepository,
) {

    @field:Inject
    lateinit var bcbClient: BcbClient

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun `deve consultar chave pix pelo clienteId e pelo pixId`() {
        val chavePix = this.repository.save(this.criarChavePix(
            clienteId = UUID.randomUUID().toString(),
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE
        ))

        val requestGrpc = this.criarRequestParaConsultarPeloClienteIdEhPixId(
            clienteId = chavePix.clienteId,
            pixId = chavePix.idInterno
        )

        val responseGrpc = this.grpcClient.consultar(requestGrpc)

        with(responseGrpc) {
            assertNotNull(this.clienteId)
            assertNotNull(this.pixId)

            assertTrue(this.clienteId.isNotBlank())
            assertTrue(this.pixId.isNotBlank())

            assertEquals(chavePix.clienteId, this.clienteId)
            assertEquals(chavePix.idInterno, this.pixId)
            assertEquals(chavePix.chave, this.chave)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando consultar chave pix pelo clienteId e pelo pixId`() {
        val requestGrpc = this.criarRequestParaConsultarPeloClienteIdEhPixId(
            clienteId = UUID.randomUUID().toString(),
            pixId = UUID.randomUUID().toString()
        )

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.consultar(requestGrpc) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave pix junto ao cliente informado não existe", this.status.description)
        }
    }

    @Test
    internal fun `deve consultar chave pix pela chave`() {
        val chavePix = this.repository.save(this.criarChavePix(
            clienteId = UUID.randomUUID().toString(),
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE
        ))

        val requestGrpc = this.criarRequestParaConsultarPorChave(chave = chavePix.chave)

        Mockito.`when`(this.bcbClient.consultarPorChave(key = requestGrpc.chave))
            .thenReturn(HttpResponse.ok(this.criarPixKeyDetailsResponse(chavePix)))

        val responseGrpc = this.grpcClient.consultar(requestGrpc)

        with(responseGrpc) {
            assertNotNull(this.clienteId)
            assertNotNull(this.pixId)

            assertTrue(this.clienteId.isBlank())
            assertTrue(this.pixId.isBlank())

            assertEquals(chavePix.chave, this.chave)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando consultar chave pix pela chave`() {
        val requestGrpc = this.criarRequestParaConsultarPorChave(chave = "email@email.com")

        Mockito.`when`(this.bcbClient.consultarPorChave(key = requestGrpc.chave))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.consultar(requestGrpc) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave pix informada não existe no Bacen", this.status.description)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando requisição for invalida`() {
        val requestGrpc = this.criarRequestInvalida()

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.consultar(requestGrpc) }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Requisição para consulta de chave pix foi inválida", this.status.description)
        }
    }

    private fun criarPixKeyDetailsResponse(chavePix: ChavePix) =
        PixKeyDetailsResponse(
            keyType = chavePix.tipoChave.getKeyType().name,
            key = chavePix.chave,
            bankAccount = BankAccount(
                participant = chavePix.conta.instituicao.ispb,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numero,
                accountType = chavePix.tipoConta.getAccountType().name
            ),
            owner = Owner(
                type = "NATURAL_PERSON",
                name = chavePix.conta.titular.nome,
                taxIdNumber = chavePix.conta.titular.cpf
            ),
            createdAt = chavePix.registradaEm.toString()
        )


    private fun criarChavePix(
        clienteId: String,
        tipoChave: TipoChaveModel,
        chave: String,
        tipoConta: TipoContaModel,
    ) =
        ChavePix(
            clienteId = clienteId,
            tipoChave = tipoChave,
            chave = chave,
            tipoConta = tipoConta,
            conta = Conta(
                tipo = tipoConta.name,
                instituicao = Instituicao(
                    nome = "",
                    ispb = ""
                ),
                agencia = "",
                numero = "",
                titular = Titular(
                    nome = "",
                    cpf = ""
                )
            )

        )


    private fun criarRequestParaConsultarPeloClienteIdEhPixId(
        clienteId: String,
        pixId: String,
    ): ConsultarChavePixRequestGrpc =
        newBuilder()
            .setPixId(
                FiltroPoPixId.newBuilder()
                    .setClienteId(clienteId)
                    .setPixId(pixId)
                    .build()
            )
            .build()

    private fun criarRequestParaConsultarPorChave(chave: String):
            ConsultarChavePixRequestGrpc =
        newBuilder()
            .setChave(chave)
            .build()

    private fun criarRequestInvalida(): ConsultarChavePixRequestGrpc =
        newBuilder().build()

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class ConsultarChavePixGrpc {
        @Singleton
        fun consultar(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ConsultarChavePixServiceGrpc.ConsultarChavePixServiceBlockingStub {
            return ConsultarChavePixServiceGrpc.newBlockingStub(channel)
        }
    }
}