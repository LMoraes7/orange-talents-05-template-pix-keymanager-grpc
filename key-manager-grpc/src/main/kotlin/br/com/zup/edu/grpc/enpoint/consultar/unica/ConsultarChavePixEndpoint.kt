package br.com.zup.edu.grpc.enpoint.consultar.unica

import br.com.zup.edu.ConsultarChavePixRequestGrpc
import br.com.zup.edu.ConsultarChavePixRequestGrpc.FiltroCase.*
import br.com.zup.edu.ConsultarChavePixResponseGrpc
import br.com.zup.edu.ConsultarChavePixServiceGrpc
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.enpoint.consultar.unica.util.Filtro
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Singleton
@ErrorAroundHandler
class ConsultarChavePixEndpoint(
    private val validator: Validator,
    private val repository: ChavePixRepository,
    private val bcbCliente: BcbClient,
) : ConsultarChavePixServiceGrpc.ConsultarChavePixServiceImplBase() {

    @Transactional
    override fun consultar(
        request: ConsultarChavePixRequestGrpc,
        responseObserver: StreamObserver<ConsultarChavePixResponseGrpc>,
    ) {
        val responseGrpc = request.verificarSolicitacaoDeBusca(this.validator)
            .run {
                this.filtrar(repository, bcbCliente)
            }.let {
                it.paraResponseGrpc()
            }
        responseObserver.onNext(responseGrpc)
        responseObserver.onCompleted()
    }
}

private fun ConsultarChavePixRequestGrpc.verificarSolicitacaoDeBusca(validator: Validator)
        : Filtro {
    val filtro: Filtro =
        when (filtroCase) {
            PIXID -> Filtro.PorClienteEhChaveId(this.pixId.clienteId, this.pixId.pixId)
            CHAVE -> Filtro.PorChavePix(this.chave)
            FILTRO_NOT_SET -> Filtro.Invalidar()
        }

    val errors = validator.validate(filtro)

    if (errors.isNotEmpty())
        throw ConstraintViolationException(errors)
    return filtro
}
