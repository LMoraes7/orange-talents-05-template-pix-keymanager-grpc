package br.com.zup.edu.grpc.endpoint.consultar.todas

import br.com.zup.edu.ChavePixTodasConsultarRequest
import br.com.zup.edu.ChavePixTodasConsultarResponse
import br.com.zup.edu.ChavePixTodasConsultarServiceGrpc
import br.com.zup.edu.grpc.dominio.exception.badrequest.ValidacaoException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.dominio.validacao.IsUUID
import br.com.zup.edu.grpc.dominio.validacao.IsUUIDValidator
import br.com.zup.edu.grpc.endpoint.consultar.util.VerificarSeClienteExisteNoSistemaItau
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.Validator

@ErrorAroundHandler
@Singleton
class ConsultarTodasChavePixEnpoint(
    private val validator: Validator,
    private val itauClient: ItauClient,
    private val chavePixRepository: ChavePixRepository,
) : ChavePixTodasConsultarServiceGrpc.ChavePixTodasConsultarServiceImplBase() {

    override fun consultarTodas(
        request: ChavePixTodasConsultarRequest,
        responseObserver: StreamObserver<ChavePixTodasConsultarResponse>,
    ) {
        if (request.clienteId.isNullOrBlank() || !this.clienteIdEhValido(request.clienteId))
            throw ValidacaoException("Id do cliente deve ser informado em formato válido")

        VerificarSeClienteExisteNoSistemaItau.verifica(this.itauClient, request.clienteId!!)

        val chaves: List<ChavePixTodasConsultarResponse.ChavePix> =
            this.chavePixRepository.findByClienteId(request.clienteId!!)
                .map { this.createListChavesPixResponse(it) }

        val response = this.createChavePixResponse(request, chaves)

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun clienteIdEhValido(clienteId: String): Boolean {
        val regex: Regex =
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$".toRegex()

        return clienteId.matches(regex)
    }

    private fun createChavePixResponse(
        request: ChavePixTodasConsultarRequest,
        chaves: List<ChavePixTodasConsultarResponse.ChavePix>,
    ) = ChavePixTodasConsultarResponse.newBuilder()
        .setClienteId(request.clienteId)
        .addAllChaves(chaves)
        .build()

    private fun createListChavesPixResponse(chavePix: ChavePix) =
        ChavePixTodasConsultarResponse.ChavePix.newBuilder()
            .setPixId(chavePix.id.toString())
            .setTipoChave(chavePix.tipoChave.name)
            .setChave(chavePix.chave)
            .setTipoConta(chavePix.tipoConta.name)
            .setDataRegistro(chavePix.criadaEm.toString())
            .build()
}