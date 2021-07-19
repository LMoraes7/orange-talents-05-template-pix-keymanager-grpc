package br.com.zup.edu.grpc.endpoint.remover

import br.com.zup.edu.ChavePixRemoverRequest
import br.com.zup.edu.ChavePixRemoverServiceGrpc
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.modelo.ContaAssociada
import br.com.zup.edu.grpc.dominio.modelo.Instuicao
import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.bcb.dto.request.DeletePixKeyRequest
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@MicronautTest(transactional = false)
internal class RemoverChavePixEndpointTest(
    private val grpcCliente: ChavePixRemoverServiceGrpc.ChavePixRemoverServiceBlockingStub,
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BcbClient,
) {

    lateinit var chavePix: ChavePix
    lateinit var request: ChavePixRemoverRequest.Builder

    @BeforeEach
    internal fun setUp() {
        this.repository.deleteAll()

        this.chavePix = ChavePix(
            clienteId = "5260263c-a3c1-4727-ae32-3bdb2538841b",
            tipoChave = TipoChaveModel.EMAIL,
            chave = "yuri@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE,
            conta = ContaAssociada(
                tipo = TipoContaModel.CONTA_CORRENTE.name,
                instituicao = Instuicao(
                    nome = "ITAÚ UNIBANCO S.A.",
                    ispb = "60701190"
                ),
                agencia = "0001",
                numero = "123455",
                titular = Titular(
                    id = "5260263c-a3c1-4727-ae32-3bdb2538841b",
                    nome = "Yuri Matheus",
                    cpf = "86135457004"
                )
            )
        )

        this.request = ChavePixRemoverRequest.newBuilder()
            .setClienteId(this.chavePix.clienteId)
            .setPixIdInterno(this.chavePix.pixIdInterno)
    }

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun `deve remover chave pix`() {
        this.repository.save(this.chavePix)

        Mockito.`when`(this.itauClient.buscarCliente(this.chavePix.clienteId))
            .thenReturn(HttpResponse.ok())

        Mockito.`when`(this.bcbClient.remover(this.chavePix.chave,
            DeletePixKeyRequest((this.chavePix))))
            .thenReturn(HttpResponse.ok())

        val responseGrpcClient = this.grpcCliente.remover(this.request.build())

        with(responseGrpcClient) {
            assertTrue(this.removida)
        }
    }

    @Test
    internal fun `nao deve remover chave que nao existe`() {
        Mockito.`when`(this.itauClient.buscarCliente(this.chavePix.clienteId))
            .thenReturn(HttpResponse.ok())

        val error = assertThrows<StatusRuntimeException> { this.grpcCliente.remover(this.request.build()) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, error.status.code)
            assertEquals("Chave PIX informada não existe no sistema", error.status.description)
        }
    }

    @Test
    internal fun `deve dar erro quando cliente nao existir no sistema itau`() {
        Mockito.`when`(this.itauClient.buscarCliente(this.chavePix.clienteId))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> { this.grpcCliente.remover(this.request.build()) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, error.status.code)
            assertEquals("Cliente informado não existe no sistema Itaú", error.status.description)
        }
    }

    @Test
    internal fun `deve dar erro quando o bacen retornar forbidden(422)`() {
        Mockito.`when`(this.itauClient.buscarCliente(this.chavePix.clienteId))
            .thenReturn(HttpResponse.ok())

        Mockito.`when`(this.bcbClient.remover(this.chavePix.chave, this.chavePix.paraDeletePixKeyRequest()))
            .thenReturn(HttpResponse.unprocessableEntity())

        val error = assertThrows<StatusRuntimeException> { this.grpcCliente.remover(this.request.build()) }

        with(error) {
            assertEquals(Status.PERMISSION_DENIED.code, error.status.code)
            assertEquals(
                "O usuário não está autorizado a realizar a exclusão da chave pix",
                error.status.description
            )
        }
    }

    @Test
    internal fun `deve dar erro quando cliente tentar remover uma chave que nao lhe pertence`() {
        Mockito.`when`(this.itauClient.buscarCliente(this.chavePix.clienteId))
            .thenReturn(HttpResponse.ok())

        val error = assertThrows<StatusRuntimeException> { this.grpcCliente.remover(this.request.build()) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, error.status.code)
            assertEquals("Chave PIX informada não existe no sistema", error.status.description)
        }
    }

    @MockBean(ItauClient::class)
    fun mockItauClient(): ItauClient =
        Mockito.mock(ItauClient::class.java)

    @MockBean(BcbClient::class)
    fun mockBcbClient(): BcbClient =
        Mockito.mock(BcbClient::class.java)

    @Factory
    class Client2 {

        @Bean
        fun remover(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ChavePixRemoverServiceGrpc.ChavePixRemoverServiceBlockingStub =
            ChavePixRemoverServiceGrpc.newBlockingStub(channel)
    }
}