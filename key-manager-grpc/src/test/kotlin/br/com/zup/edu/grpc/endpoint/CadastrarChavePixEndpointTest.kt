package br.com.zup.edu.grpc.endpoint

import br.com.zup.edu.*
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarChavePixEndpointTest(
    private val grpcClient: ChavePixServiceGrpc.ChavePixServiceBlockingStub,
) {

    @field:Inject
    lateinit var repository: ChavePixRepository

    lateinit var request: ChavePixRequest

    @BeforeEach
    fun init() {
        this.request = ChavePixRequest.newBuilder()
            .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setTipoChave(TipoChave.EMAIL)
            .setChave("yuri@email.com")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()
    }

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun deveCadastrarChavePixEBuscarPeloIdInterno() {
        val response: ChavePixResponse = this.grpcClient.cadastrar(this.request)
        assertNotNull(response.pixId)
        assertTrue(this.repository.buscarPeloIdInterno(response.pixId).isPresent)
    }

    @Test
    internal fun naoDeveCadastrarChavePixDuplicada() {
        this.grpcClient.cadastrar(this.request)
        val error: StatusRuntimeException =
            assertThrows<StatusRuntimeException> { this.grpcClient.cadastrar(this.request) }
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
    }

    @Test
    internal fun naoDeveCadastrarChaveComClienteInexistente() {
        val request = ChavePixRequest.newBuilder()
            .setClienteId("xxxx")
            .setTipoChave(TipoChave.ALEATORIA)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val error: StatusRuntimeException =
            assertThrows<StatusRuntimeException> { this.grpcClient.cadastrar(this.request) }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
    }

    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ChavePixServiceGrpc.ChavePixServiceBlockingStub? {
            return ChavePixServiceGrpc.newBlockingStub(channel)
        }
    }
}