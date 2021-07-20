package br.com.zup.edu.grpc.endpoint.consultar.todas

import br.com.zup.edu.ChavePixTodasConsultarRequest
import br.com.zup.edu.ChavePixTodasConsultarResponse
import br.com.zup.edu.ChavePixTodasConsultarServiceGrpc
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

@MicronautTest(transactional = false)
internal class ConsultarTodasChavePixEnpointTest(
    private val grpcClient: ChavePixTodasConsultarServiceGrpc.ChavePixTodasConsultarServiceBlockingStub,
    private val itauClient: ItauClient,
    private val repository: ChavePixRepository,
) {

    val request: ChavePixTodasConsultarRequest.Builder =
        ChavePixTodasConsultarRequest.newBuilder()
            .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")

    @Test
    internal fun `deve consultar todas as chaves de um cliente cadastrado`() {
        Mockito.`when`(this.itauClient.buscarCliente(this.request.clienteId)).thenReturn(HttpResponse.ok())
        val response: ChavePixTodasConsultarResponse = this.grpcClient.consultarTodas(this.request.build())
        assertNotNull(response.chavesList)
        assertTrue(response.chavesList.isEmpty())
        assertNotNull(response.clienteId)
    }

    @Test
    internal fun `nao deve consultar todas as chaves de um cliente nao cadastrado`() {
        Mockito.`when`(this.itauClient.buscarCliente(this.request.clienteId)).thenReturn(HttpResponse.notFound())
        val error = assertThrows<StatusRuntimeException> { this.grpcClient.consultarTodas(this.request.build()) }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
    }

    @MockBean(ItauClient::class)
    fun mockItauClient(): ItauClient =
        Mockito.mock(ItauClient::class.java)

    @Factory
    class Client4 {

        @Bean
        fun consultarTodas(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ChavePixTodasConsultarServiceGrpc.ChavePixTodasConsultarServiceBlockingStub =
            ChavePixTodasConsultarServiceGrpc.newBlockingStub(channel)
    }
}