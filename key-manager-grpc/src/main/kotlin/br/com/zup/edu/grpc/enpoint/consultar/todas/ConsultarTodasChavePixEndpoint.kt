package br.com.zup.edu.grpc.enpoint.consultar.todas

import br.com.zup.edu.ConsultarTodasChavePixRequestGrpc
import br.com.zup.edu.ConsultarTodasChavePixResponseGrpc
import br.com.zup.edu.ConsultarTodasChavePixResponseGrpc.DetalhesChavePix
import br.com.zup.edu.ConsultarTodasChavePixResponseGrpc.newBuilder
import br.com.zup.edu.ConsultarTodasChavePixServiceGrpc
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.enpoint.consultar.todas.request.ConsultarTodasChavePixRequestDto
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.service.ConsultarTodasChavePixService
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@ErrorAroundHandler
class ConsultarTodasChavePixEndpoint(
    private val service: ConsultarTodasChavePixService,
) : ConsultarTodasChavePixServiceGrpc.ConsultarTodasChavePixServiceImplBase() {

    @Transactional
    override fun consultar(
        request: ConsultarTodasChavePixRequestGrpc,
        responseObserver: StreamObserver<ConsultarTodasChavePixResponseGrpc>,
    ) {
        val requestDto = request.paraConsultarTodasChavePixRequestDto()

        this.service.consultar(requestDto)
            .map { criarListaDeChavePixResponseGrpc(it) }
            .run { criarResponseGrpc(this) }
            .let { responseObserver.onNext(it) }

        responseObserver.onCompleted()
    }

    private fun criarResponseGrpc(lista: List<DetalhesChavePix>) =
        newBuilder().addAllChavesPix(lista).build()

    private fun criarListaDeChavePixResponseGrpc(chavePix: ChavePix):
            DetalhesChavePix =
        DetalhesChavePix.newBuilder()
            .setPixId(chavePix.idInterno)
            .setClienteId(chavePix.clienteId)
            .setTipoChave(chavePix.tipoChave.name)
            .setChave(chavePix.chave)
            .setTipoConta(chavePix.tipoConta.name)
            .setRegistradaEm(chavePix.registradaEm.toString())
            .build()
}

private fun ConsultarTodasChavePixRequestGrpc.paraConsultarTodasChavePixRequestDto():
        ConsultarTodasChavePixRequestDto =
    ConsultarTodasChavePixRequestDto(
        clienteId = this.clienteId
    )

