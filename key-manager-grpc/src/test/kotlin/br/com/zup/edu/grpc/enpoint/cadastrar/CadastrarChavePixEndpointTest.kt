package br.com.zup.edu.grpc.enpoint.cadastrar

import br.com.zup.edu.CadastrarChavePixServiceGrpc
import br.com.zup.edu.NovaChavePixRequestGrpc
import br.com.zup.edu.TipoChaveGrpc
import br.com.zup.edu.TipoContaGrpc
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.http.client.ItauClient
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.bcb.response.CreatePixKeyResponse
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
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastrarChavePixEndpointTest(
    private val grpcClient: CadastrarChavePixServiceGrpc.CadastrarChavePixServiceBlockingStub,
    private val repository: ChavePixRepository,
) {

    @field:Inject
    lateinit var itauClient: ItauClient

    @field:Inject
    lateinit var bcbClient: BcbClient

    lateinit var request: NovaChavePixRequestGrpc.Builder

    @BeforeEach
    fun init() {
        this.request =
            NovaChavePixRequestGrpc.newBuilder()
                .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                .setTipoChave(TipoChaveGrpc.EMAIL)
                .setChave("email@email.com")
                .setTipoConta(TipoContaGrpc.CONTA_CORRENTE)
    }

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    @Test
    internal fun `deve cadastrar chave pix com o tipo de chave aleatoria`() {
        this.request.tipoChave = TipoChaveGrpc.ALEATORIA
        this.request.chave = ""

        val requestGrpc = this.request.build()
        val chavePix = this.criarChavePix(requestGrpc)

        Mockito.`when`(this.itauClient.buscarClienteEConta(requestGrpc.clienteId, requestGrpc.tipoConta.name))
            .thenReturn(HttpResponse.ok(this.criarDadosDaContaResponse()))

        Mockito.`when`(this.bcbClient.cadastrar(chavePix.paraCreatePixKeyRequest()))
            .thenReturn(HttpResponse.created(this.criarCreatePixKeyResponse(chavePix)))

        val responseGrpc = this.grpcClient.cadastrar(requestGrpc)

        with(responseGrpc) {
            assertNotNull(responseGrpc.clienteId)
            assertNotNull(responseGrpc.pixIdInterno)
        }
    }

    @Test
    internal fun `deve cadastrar chave pix com tipo de chave diferente de aleatoria`() {
        val requestGrpc = this.request.build()
        val chavePix = this.criarChavePix(requestGrpc)

        Mockito.`when`(this.bcbClient.consultarPorChave(requestGrpc.chave))
            .thenReturn(HttpResponse.notFound())

        Mockito.`when`(this.itauClient.buscarClienteEConta(requestGrpc.clienteId, requestGrpc.tipoConta.name))
            .thenReturn(HttpResponse.ok(this.criarDadosDaContaResponse()))

        Mockito.`when`(this.bcbClient.cadastrar(chavePix.paraCreatePixKeyRequest()))
            .thenReturn(HttpResponse.created(this.criarCreatePixKeyResponse(chavePix)))

        val responseGrpc = this.grpcClient.cadastrar(requestGrpc)

        with(responseGrpc) {
            assertNotNull(this.clienteId)
            assertNotNull(this.pixIdInterno)
        }
    }

    @Test
    internal fun `deve retornar ALREADY_EXISTS ao tentar cadastrar chave duplicada`() {
        this.repository.deleteAll()
        val requestGrpc = this.request.build()
//      O ERRO OCORRE NA LINHA ABAIXO
        this.repository.save(this.criarChavePix(requestGrpc))

        Mockito.`when`(this.bcbClient.consultarPorChave(requestGrpc.chave))
            .thenReturn(HttpResponse.ok())

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.cadastrar(requestGrpc) }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("Chave pix informada já está cadastrada no Bacen", this.status.description)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND ao tentar cadastrar chave pix com cliente invalido`() {
        this.request.tipoChave = TipoChaveGrpc.ALEATORIA
        this.request.chave = ""
        this.request.clienteId = UUID.randomUUID().toString()

        val requestGrpc = this.request.build()

        Mockito.`when`(this.itauClient.buscarClienteEConta(requestGrpc.clienteId, requestGrpc.tipoConta.name))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.cadastrar(requestGrpc) }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Dados do cliente não existem no sistema Itaú", this.status.description)
        }

    }

    private fun criarCreatePixKeyResponse(chavePix: ChavePix): CreatePixKeyResponse =
        CreatePixKeyResponse(
            keyType = chavePix.tipoChave.name,
            key =
            if (chavePix.chave.isNullOrBlank())
                UUID.randomUUID().toString()
            else
                chavePix.chave,
            bankAccount = chavePix.paraCreatePixKeyRequest().bankAccount,
            owner = chavePix.paraCreatePixKeyRequest().owner,
            createdAt = LocalDateTime.now().toString()
        )

    private fun criarChavePix(request: NovaChavePixRequestGrpc) =
        ChavePix(
            clienteId = request.clienteId,
            tipoChave = TipoChaveModel.valueOf(request.tipoChave.name),
            chave = request.chave,
            tipoConta = TipoContaModel.valueOf(request.tipoConta.name),
            conta = criarDadosDaContaResponse().paraContaModel()
        )


    private fun criarDadosDaContaResponse() = DadosDaContaResponse(
        tipo = this.request.tipoConta.name,
        instituicao = InstituicaoResponse(
            nome = "Itaú Unibanco",
            ispb = "0001"
        ),
        agencia = "1111",
        numero = "1111",
        titular = TitularResponse(
            id = this.request.clienteId,
            nome = "Diego",
            cpf = "11111111111"
        )
    )

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @Factory
    class CadastrarChavePixGrpc {

        @Singleton
        fun cadastrar(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                CadastrarChavePixServiceGrpc.CadastrarChavePixServiceBlockingStub? {
            return CadastrarChavePixServiceGrpc.newBlockingStub(channel)
        }
    }
}