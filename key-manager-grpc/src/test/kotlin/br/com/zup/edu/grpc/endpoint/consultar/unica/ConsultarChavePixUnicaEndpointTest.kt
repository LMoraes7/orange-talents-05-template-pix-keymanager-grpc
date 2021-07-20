package br.com.zup.edu.grpc.endpoint.consultar.unica

import br.com.zup.edu.ChavePixUnicaConsultarRequest
import br.com.zup.edu.ChavePixUnicaConsultarResponse
import br.com.zup.edu.ChavePixUnicaConsultarServiceGrpc
import br.com.zup.edu.FiltroPoPixId
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.modelo.ContaAssociada
import br.com.zup.edu.grpc.dominio.modelo.Instuicao
import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.bcb.dto.response.BankAccountResponse
import br.com.zup.edu.grpc.http.client.bcb.dto.response.OwnerResponse
import br.com.zup.edu.grpc.http.client.bcb.dto.response.PixKeyDetailsResponse
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import io.grpc.ManagedChannel
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
import org.mockito.Mockito
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class ConsultarChavePixUnicaEndpointTest(
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BcbClient,
    private val grpcClient: ChavePixUnicaConsultarServiceGrpc.ChavePixUnicaConsultarServiceBlockingStub,
) {

    lateinit var chavePix: ChavePix
    lateinit var requestClienteIdEPixId: ChavePixUnicaConsultarRequest.Builder
    lateinit var requestChave: ChavePixUnicaConsultarRequest.Builder

    @BeforeEach
    internal fun setUp() {
        this.chavePix = this.repository.save(
            ChavePix(
                clienteId = "5260263c-a3c1-4727-ae32-3bdb2538841b",
                tipoChave = TipoChaveModel.EMAIL,
                chave = "yuri@email.com",
                tipoConta = TipoContaModel.CONTA_CORRENTE,
                conta = ContaAssociada(
                    tipo = "CONTA_CORRENTE",
                    instituicao = Instuicao(
                        nome = "fdg",
                        ispb = "sfd"
                    ),
                    agencia = "dfg",
                    numero = "fdg",
                    titular = Titular(
                        id = "dfg",
                        nome = "dfg",
                        cpf = "dfg"
                    )
                )
            )
        )

        this.requestClienteIdEPixId = ChavePixUnicaConsultarRequest.newBuilder()
            .setPixId(
                FiltroPoPixId.newBuilder()
                    .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .setPixId(this.chavePix.pixIdInterno)
                    .build()
            )

        this.requestChave = ChavePixUnicaConsultarRequest.newBuilder()
            .setChave("yuri@email.com")
    }

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun `deve consultar chave pix pelo cliente id e pelo pix id`() {
        val response: ChavePixUnicaConsultarResponse = this.grpcClient.consultarUnica(this.requestClienteIdEPixId.build())
        assertEquals(response.pixId, this.chavePix.pixIdInterno)
    }

    @Test
    internal fun `deve consultar chave pix pela chave`() {
        Mockito.`when`(this.bcbClient.consultarPorChave(this.chavePix.chave))
            .thenReturn(HttpResponse.ok(this.createPixDetailsResponse()))
        val response: ChavePixUnicaConsultarResponse = this.grpcClient.consultarUnica(this.requestChave.build())
        assertTrue(response.clienteId.isNullOrBlank())
        assertEquals(response.chave, this.chavePix.chave)
    }

    private fun createPixDetailsResponse(): PixKeyDetailsResponse =
        PixKeyDetailsResponse(
            keyType = "EMAIL",
            key = this.chavePix.chave,
            bankAccount = BankAccountResponse(
                participant = "",
                branch = "",
                accountNumber = "",
                accountType = "CACC"
            ),
            owner = OwnerResponse(
                type = "",
                name = "",
                taxIdNumber = ""
            ),
            createdAt = LocalDateTime.now().toString()
        )

    @MockBean(ItauClient::class)
    fun mockItauClient(): ItauClient =
        Mockito.mock(ItauClient::class.java)

    @MockBean(BcbClient::class)
    fun mockBcbClient(): BcbClient =
        Mockito.mock(BcbClient::class.java)

    @Factory
    class Client5 {

        @Bean
        fun consultarUnica(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ChavePixUnicaConsultarServiceGrpc.ChavePixUnicaConsultarServiceBlockingStub =
            ChavePixUnicaConsultarServiceGrpc.newBlockingStub(channel)
    }
}