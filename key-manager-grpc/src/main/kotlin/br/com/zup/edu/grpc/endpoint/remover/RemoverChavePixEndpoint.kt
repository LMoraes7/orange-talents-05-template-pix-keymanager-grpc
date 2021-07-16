package br.com.zup.edu.grpc.endpoint.remover

import br.com.zup.edu.ChavePixRemoverRequest
import br.com.zup.edu.ChavePixRemoverResponse
import br.com.zup.edu.ChavePixRemoverServiceGrpc
import br.com.zup.edu.grpc.dominio.exception.badrequest.ValidacaoException
import br.com.zup.edu.grpc.endpoint.remover.dto.ChavePixRemoverRequestDto
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.service.RemoverChavePixService
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@ErrorAroundHandler
@Singleton
class RemoverChavePixEndpoint(
    private val validator: Validator,
    private val service: RemoverChavePixService,
) : ChavePixRemoverServiceGrpc.ChavePixRemoverServiceImplBase() {

    private val logger: Logger = LoggerFactory.getLogger(RemoverChavePixEndpoint::class.java)

    override fun remover(
        request: ChavePixRemoverRequest,
        responseObserver: StreamObserver<ChavePixRemoverResponse>,
    ) {
        this.logger.info("enpoint -> recebendo requisição para remoção de chave pix")
        this.logger.info("endpoint -> efetuando validações de entrada para a requisição")

        val chaveDto: ChavePixRemoverRequestDto = request.paraChavePixRemoverRequestDto(this.validator)

        this.logger.info("endpoint -> repassando requisição para a service de remoção")

        this.service.remover(chaveDto)
        responseObserver.onNext(ChavePixRemoverResponse.newBuilder()
            .setRemovida(true)
            .build()
        )

        this.logger.info("endpoint -> terminando requisição para remoção de chave pix")

        responseObserver.onCompleted()
    }
}

fun ChavePixRemoverRequest.paraChavePixRemoverRequestDto(validator: Validator): ChavePixRemoverRequestDto {
    val chaveDto = ChavePixRemoverRequestDto(
        clienteId = this.clienteId,
        pixIdInterno = this.pixIdInterno
    )

    val errors = validator.validate(chaveDto)
    if (errors.isNotEmpty())
        throw ValidacaoException("Há erros de validação")
    return chaveDto
}
