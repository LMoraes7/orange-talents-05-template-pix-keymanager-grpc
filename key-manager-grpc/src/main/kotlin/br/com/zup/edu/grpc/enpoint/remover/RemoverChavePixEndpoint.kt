package br.com.zup.edu.grpc.enpoint.remover

import br.com.zup.edu.RemoverChavePixRequestGrpc
import br.com.zup.edu.RemoverChavePixResponseGrpc
import br.com.zup.edu.RemoverChavePixServiceGrpc
import br.com.zup.edu.grpc.enpoint.remover.dto.RemoverChavePixRequestDto
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.service.RemoverChavePixService
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@ErrorAroundHandler
class RemoverChavePixEndpoint(
    private val service: RemoverChavePixService
) : RemoverChavePixServiceGrpc.RemoverChavePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun remover(
        request: RemoverChavePixRequestGrpc,
        responseObserver: StreamObserver<RemoverChavePixResponseGrpc>,
    ) {
        this.logger.info("enpoint -> recebendo requisição para remoção de chave pix")
        this.logger.info("endpoint -> efetuando validações de entrada para a requisição")

        val chaveRequest = request.paraRemoverChavePixRequestDto()

        this.logger.info("endpoint -> repassando requisição para a service de remoção")

        this.service.remover(chaveRequest)
            .run {
                RemoverChavePixResponseGrpc.newBuilder()
                    .setRemovida(this)
                    .build()
            }.let {
                responseObserver.onNext(it)
            }

        this.logger.info("endpoint -> terminando requisição para remoção de chave pix")

        responseObserver.onCompleted()
    }
}

private fun RemoverChavePixRequestGrpc.paraRemoverChavePixRequestDto() =
    RemoverChavePixRequestDto(
        pixId = this.pixId,
        clienteId = this.clienteId
    )

