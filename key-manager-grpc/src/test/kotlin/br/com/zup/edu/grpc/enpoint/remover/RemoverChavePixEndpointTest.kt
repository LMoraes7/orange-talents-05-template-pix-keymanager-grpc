package br.com.zup.edu.grpc.enpoint.remover

import br.com.zup.edu.RemoverChavePixRequestGrpc
import br.com.zup.edu.RemoverChavePixServiceGrpc
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.itau.response.DadosDaContaResponse
import br.com.zup.edu.grpc.http.client.itau.response.InstituicaoResponse
import br.com.zup.edu.grpc.http.client.itau.response.TitularResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
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
internal class RemoverChavePixEndpointTest(
    private val grpcClient: RemoverChavePixServiceGrpc.RemoverChavePixServiceBlockingStub,
    private val repository: ChavePixRepository,
) {

    @field:Inject
    lateinit var bcbClient: BcbClient

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun `deve remover chave pix`() {
        val chavePix = this.salvarChavePix()

        val requestGrpc = RemoverChavePixRequestGrpc.newBuilder()
            .setPixId(chavePix.idInterno)
            .setClienteId(chavePix.clienteId)
            .build()

        Mockito.`when`(this.bcbClient.remover(chavePix.chave, chavePix.paraDeletePixKeyRequest()))
            .thenReturn(HttpResponse.ok())

        val response = this.grpcClient.remover(requestGrpc)

        with(response) {
            assertNotNull(this.removida)
            assertTrue(this.removida)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND`() {
        val requestGrpc = RemoverChavePixRequestGrpc.newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .setClienteId(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException> {
            this.grpcClient.remover(requestGrpc)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave pix junto ao cliente informado não existe", this.status.description)
            assertEquals(0, repository.count())
        }
    }

    @Test
    internal fun `deve retornar PERMISSION_DENIED quando nao for permitdo remover chave pix`() {
        val chavePix = this.salvarChavePix()

        val requestGrpc = RemoverChavePixRequestGrpc.newBuilder()
            .setPixId(chavePix.idInterno)
            .setClienteId(chavePix.clienteId)
            .build()

        Mockito.`when`(this.bcbClient.remover(chavePix.chave, chavePix.paraDeletePixKeyRequest()))
            .thenReturn(HttpResponse.status(HttpStatus.FORBIDDEN))

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.remover(requestGrpc) }

        with(error) {
            assertEquals(Status.PERMISSION_DENIED.code, this.status.code)
            assertEquals("Exclusão de chave pix não foi permitida", this.status.description)
        }
    }

    private fun salvarChavePix() =
        this.repository.save(ChavePix(
            clienteId = UUID.randomUUID().toString(),
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE,
            conta = this.criarDadosDaContaResponse().paraContaModel()
        ))

    private fun criarDadosDaContaResponse() = DadosDaContaResponse(
        tipo = TipoContaModel.CONTA_CORRENTE.name,
        instituicao = InstituicaoResponse(
            nome = "Itaú Unibanco",
            ispb = "0001"
        ),
        agencia = "1111",
        numero = "1111",
        titular = TitularResponse(
            id = UUID.randomUUID().toString(),
            nome = "Diego",
            cpf = "11111111111"
        )
    )

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class RemoverChavePixGrpc {

        @Singleton
        fun remover(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                RemoverChavePixServiceGrpc.RemoverChavePixServiceBlockingStub? {
            return RemoverChavePixServiceGrpc.newBlockingStub(channel)
        }
    }
}