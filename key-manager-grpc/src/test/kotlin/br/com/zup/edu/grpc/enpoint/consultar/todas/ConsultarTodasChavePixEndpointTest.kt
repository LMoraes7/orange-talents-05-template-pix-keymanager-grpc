package br.com.zup.edu.grpc.enpoint.consultar.todas

import br.com.zup.edu.ConsultarTodasChavePixRequestGrpc
import br.com.zup.edu.ConsultarTodasChavePixServiceGrpc
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.modelo.Conta
import br.com.zup.edu.grpc.dominio.modelo.Instituicao
import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.http.client.itau.ItauClient
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
internal class ConsultarTodasChavePixEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: ConsultarTodasChavePixServiceGrpc.ConsultarTodasChavePixServiceBlockingStub,
) {

    @field:Inject
    lateinit var itauClient: ItauClient

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun `deve consultar chaves pix e retornar lista com objetos`() {
        val chavePix = this.repository.save(this.criarChavePix(
            clienteId = "5260263c-a3c1-4727-ae32-3bdb2538841b",
            tipoChave = TipoChaveModel.EMAIL,
            chave = "email@email.com",
            tipoConta = TipoContaModel.CONTA_CORRENTE
        ))

        val requestGrpc = this.criarRequestGrpc(clienteId = chavePix.clienteId)

        Mockito.`when`(this.itauClient.buscarCliente(
            clienteId = requestGrpc.clienteId
        )).thenReturn(HttpResponse.ok())

        val responseGrpc = this.grpcClient.consultar(requestGrpc)

        with(responseGrpc) {
            assertNotNull(this.chavesPixList)
            assertTrue(this.chavesPixList.isNotEmpty())
        }
    }

    @Test
    internal fun `deve consultar chaves pix e retornar lista vazia`() {
        val requestGrpc = this.criarRequestGrpc(clienteId = "5260263c-a3c1-4727-ae32-3bdb2538841b")

        Mockito.`when`(this.itauClient.buscarCliente(
            clienteId = requestGrpc.clienteId
        )).thenReturn(HttpResponse.ok())

        val responseGrpc = this.grpcClient.consultar(requestGrpc)

        with(responseGrpc) {
            assertNotNull(this.chavesPixList)
            assertTrue(this.chavesPixList.isEmpty())
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando cliente não existir no itaú`() {
        val requestGrpc = this.criarRequestGrpc(clienteId = UUID.randomUUID().toString())

        Mockito.`when`(this.itauClient.buscarCliente(
            clienteId = requestGrpc.clienteId
        )).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.consultar(requestGrpc) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Cliente informado não está cadastrado no Itaú", this.status.description)
        }
    }

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


    private fun criarRequestGrpc(clienteId: String) =
        ConsultarTodasChavePixRequestGrpc.newBuilder()
            .setClienteId(clienteId)
            .build()

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @Factory
    class ConsultarTodasChavePixGrpc {

        @Singleton
        fun consultar(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ConsultarTodasChavePixServiceGrpc.ConsultarTodasChavePixServiceBlockingStub {
            return ConsultarTodasChavePixServiceGrpc.newBlockingStub(channel)
        }
    }
}