package br.com.zup.edu.grpc.endpoint.remover

import br.com.zup.edu.*
import org.junit.jupiter.api.Assertions.*
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
internal class RemoverChavePixEndpointTest(
    val grpcClientRemover: ChavePixRemoverServiceGrpc.ChavePixRemoverServiceBlockingStub,
    val grpcClientCadastrar: ChavePixCadastrarServiceGrpc.ChavePixCadastrarServiceBlockingStub,
) {

    @field:Inject
    lateinit var repository: ChavePixRepository

    lateinit var requestCadastrar: ChavePixCadastrarRequest
    lateinit var responseCadastrar: ChavePixCadastrarResponse

    @BeforeEach
    fun init() {
        this.requestCadastrar = ChavePixCadastrarRequest.newBuilder()
            .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setTipoChave(TipoChave.EMAIL)
            .setChave("yuri@email.com")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        this.responseCadastrar = this.grpcClientCadastrar.cadastrar(this.requestCadastrar)
    }

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun deveRemoverChave() {
        val response: ChavePixRemoverResponse = this.grpcClientRemover.remover(
            ChavePixRemoverRequest.newBuilder()
                .setClienteId(this.requestCadastrar.clienteId)
                .setPixIdInterno(this.responseCadastrar.pixId)
                .build()
        )

        assertNotNull(response.removida)
        assertTrue(response.removida)
        assertTrue(this.repository.buscarPeloIdInterno(this.responseCadastrar.pixId).isEmpty)
    }

    @Test
    internal fun naoDeveRemoverChaveNaoExistente() {
        val error = assertThrows<StatusRuntimeException> {
            this.grpcClientRemover.remover(ChavePixRemoverRequest.newBuilder()
                .setClienteId(this.requestCadastrar.clienteId)
                .setPixIdInterno("xxxx")
                .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, error.status.code)
    }

    @Test
    internal fun naoDeveRemoverChaveQueClienteNaoExiste() {
        val error = assertThrows<StatusRuntimeException> {
            this.grpcClientRemover.remover(ChavePixRemoverRequest.newBuilder()
                .setClienteId("xxxx")
                .setPixIdInterno(this.responseCadastrar.pixId)
                .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, error.status.code)
    }

    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ChavePixRemoverServiceGrpc.ChavePixRemoverServiceBlockingStub? {
            return ChavePixRemoverServiceGrpc.newBlockingStub(channel)
        }

        @Singleton
        fun blockingStub2(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ChavePixCadastrarServiceGrpc.ChavePixCadastrarServiceBlockingStub? {
            return ChavePixCadastrarServiceGrpc.newBlockingStub(channel)
        }
    }
}