package br.com.zup.edu.grpc.endpoint.cadastrar

import br.com.zup.edu.ChavePixCadastrarRequest
import br.com.zup.edu.ChavePixCadastrarServiceGrpc
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.bcb.dto.request.Type
import br.com.zup.edu.grpc.http.client.bcb.dto.response.BankAccountResponse
import br.com.zup.edu.grpc.http.client.bcb.dto.response.CreatePixKeyResponse
import br.com.zup.edu.grpc.http.client.bcb.dto.response.OwnerResponse
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import br.com.zup.edu.grpc.http.client.itau.response.ContaAssociadaResponse
import br.com.zup.edu.grpc.http.client.itau.response.InstuicaoResponse
import br.com.zup.edu.grpc.http.client.itau.response.TitularResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class CadastrarChavePixEndpointTest(
    private val grpcClient: ChavePixCadastrarServiceGrpc.ChavePixCadastrarServiceBlockingStub,
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BcbClient,
) {

    lateinit var request: ChavePixCadastrarRequest.Builder
    lateinit var chavePix: ChavePix

    @BeforeEach
    internal fun setUp() {
        this.repository.deleteAll()
        this.request = ChavePixCadastrarRequest.newBuilder()
            .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
            .setTipoChave(TipoChave.EMAIL)
            .setChave("yuri@email.com")
            .setTipoConta(TipoConta.CONTA_CORRENTE)

        this.chavePix = ChavePix(
            clienteId = this.request.clienteId,
            tipoChave = TipoChaveModel.valueOf(this.request.tipoChave.name),
            chave = this.request.chave,
            tipoConta = TipoContaModel.valueOf(this.request.tipoConta.name),
            conta = createContaAssociadaResponse().paraContaAssociadaModel()
        )
    }

    @AfterEach
    fun close() {
        this.repository.deleteAll()
    }

    //O teste abaixo não passa de jeito nenhum.
    //Fica dando NullPointerException no HttpResponse mesmo eu usando um Mock no client bcbClient.
    //Não consegui identificar o problema.
    //Quando levanto o servidor para realizar o teste manual não aparece erro nenhum e a aplicação funciona normalmente.
    //É como se o HttpResponse<CreatePixKeyResponse> fosse == null mesmo eu pedindo para o Mock retonar um HttpResponse criado por mim
    @Test
    internal fun `deve cadastrar chave pix e buscar pelo id interno`() {
        Mockito.`when`(this.itauClient.buscarContaCliente(this.request.clienteId, this.request.tipoConta.name))
            .thenReturn(HttpResponse.ok(this.createContaAssociadaResponse()))

        Mockito.`when`(this.bcbClient.cadastrar(this.createChavePixModel().paraCreateChavePixKeyRequest()))
            .thenReturn(HttpResponse.created(this.createChavePixKeyResponse()))

        val responseGrpcClient = this.grpcClient.cadastrar(this.request.build())

        with(responseGrpcClient) {
            assertNotNull(this.pixId)
            assertTrue(repository.buscarPeloIdInterno(this.pixId).isPresent)
        }
    }

    @Test
    internal fun `nao deve cadastrar chave pix quando conta do cliente nao existir`() {
        Mockito.`when`(this.itauClient.buscarContaCliente(this.request.clienteId, this.request.tipoConta.name))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> { this.grpcClient.cadastrar(this.request.build()) }
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("A conta do cliente informado não existe no sistema Itaú", error.status.description)
    }

    @Test
    internal fun `nao deve cadastrar chave pix duplicada`() {
        this.repository.save(this.createChavePixModel())
        val error = assertThrows<StatusRuntimeException> { this.grpcClient.cadastrar(this.request.build()) }
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
        assertEquals("Chave informada já está cadastrada", error.status.description)
    }

    //O teste abaixo não passa de jeito nenhum.
    //Fica dando NullPointerException no HttpResponse mesmo eu usando um Mock no client bcbClient.
    //Não consegui identificar o problema.
    //Quando levanto o servidor para realizar o teste manual não aparece erro nenhum e a aplicação funciona normalmente.
    //É como se o HttpResponse<CreatePixKeyResponse> fosse == null mesmo eu pedindo para o Mock retonar um HttpResponse criado por mim
    @Test
    internal fun `deve cadastrar chave pix quando o tipo de chave for aleatoria`() {
        Mockito.`when`(this.itauClient.buscarContaCliente(this.request.clienteId, this.request.tipoConta.name))
            .thenReturn(HttpResponse.ok(this.createContaAssociadaResponse()))

        Mockito.`when`(this.bcbClient.cadastrar(this.chavePix.paraCreateChavePixKeyRequest()))
            .thenReturn(HttpResponse.ok(this.createChavePixKeyResponse()))

        this.request.tipoChave = TipoChave.ALEATORIA
        this.request.chave = ""
        val responseGrpcClient = this.grpcClient.cadastrar(this.request.build())

        with(responseGrpcClient) {
            assertNotNull(this.pixId)
            assertTrue(repository.buscarPeloIdInterno(this.pixId).isPresent)
        }
    }

    private fun createChavePixKeyResponse(): CreatePixKeyResponse =
        CreatePixKeyResponse(
            keyType = this.chavePix.tipoChave.name,
            key = this.chavePix.chave,
            bankAccount = BankAccountResponse(
                participant = this.chavePix.conta.instituicao.ispb,
                branch = this.chavePix.conta.agencia,
                accountNumber = this.chavePix.conta.numero,
                accountType = this.chavePix.conta.numero
            ),
            owner = OwnerResponse(
                type = Type.NATURAL_PERSON.name,
                name = this.chavePix.conta.titular.nome,
                taxIdNumber = this.chavePix.conta.titular.cpf
            ),
            createdAt = LocalDateTime.now().toString()
        )

    private fun createChavePixModel(): ChavePix =
        ChavePix(
            clienteId = this.request.clienteId,
            tipoChave = TipoChaveModel.valueOf(this.request.tipoChave.name),
            chave = this.request.chave,
            tipoConta = TipoContaModel.valueOf(this.request.tipoConta.name),
            conta = createContaAssociadaResponse().paraContaAssociadaModel()
        )

    private fun createContaAssociadaResponse(): ContaAssociadaResponse =
        ContaAssociadaResponse(
            tipo = TipoContaModel.CONTA_CORRENTE.name,
            instituicao = InstuicaoResponse(
                nome = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "123455",
            titular = TitularResponse(
                id = this.request.clienteId,
                nome = "Yuri Matheus",
                cpf = "86135457004"
            )
        )

    @MockBean(ItauClient::class)
    fun mockItauClient(): ItauClient =
        Mockito.mock(ItauClient::class.java)

    @MockBean(BcbClient::class)
    fun mockBcbClient(): BcbClient =
        Mockito.mock(BcbClient::class.java)


    @Factory
    class ClientGrpc {
        @Bean
        fun cadastrar(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ChavePixCadastrarServiceGrpc.ChavePixCadastrarServiceBlockingStub? =
            ChavePixCadastrarServiceGrpc.newBlockingStub(channel)
    }
}