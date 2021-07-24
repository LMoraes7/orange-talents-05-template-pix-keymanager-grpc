package br.com.zup.edu.grpc.enpoint.cadastrar

import br.com.zup.edu.*
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.exception.ContaInvalidaException
import br.com.zup.edu.grpc.dominio.exception.EnumInvalidaException
import br.com.zup.edu.grpc.enpoint.cadastrar.dto.NovaChavePixRequestDto
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.service.CadastrarChavePixService
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorAroundHandler
@Singleton
class CadastrarChavePixEndpoint(
    private val service: CadastrarChavePixService,
) : CadastrarChavePixServiceGrpc.CadastrarChavePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun cadastrar(
        request: NovaChavePixRequestGrpc,
        responseObserver: StreamObserver<NovaChavePixResponseGrpc>,
    ) {
        this.logger.info("endpoint -> recebendo requisição para cadastro de chave pix")
        this.logger.info("endpoint -> efetuando validações de entrada para a requisição")

        val chaveRequest = request.paraChavePixRequestDto()

        this.logger.info("endpoint -> repassando requisição para a Service")

        this.service.cadastrar(chaveRequest)
            .run {
                logger.info("endpoint -> recebendo a chave pix retornada pela service")

                NovaChavePixResponseGrpc.newBuilder()
                    .setClienteId(this.clienteId)
                    .setPixIdInterno(this.idInterno)
                    .build()
            }.let {
                responseObserver.onNext(it)
            }

        this.logger.info("endpoint -> terminando requisição para cadastro de chave pix")

        responseObserver.onCompleted()
    }
}

private fun NovaChavePixRequestGrpc.paraChavePixRequestDto() =
    NovaChavePixRequestDto(
        clienteId = this.clienteId,
        tipoChave =
        if (this.tipoChave.equals(TipoChaveGrpc.CHAVE_DESCONHECIDA))
            throw EnumInvalidaException("O tipo da chave deve ser informado")
        else
            TipoChaveModel.valueOf(this.tipoChave.name),
        chave = this.chave,
        tipoConta =
        if (this.tipoConta.equals(TipoContaGrpc.CONTA_DESCONHECIDA))
            throw ContaInvalidaException("O tipo da chave deve ser informado")
        else
            TipoContaModel.valueOf(this.tipoConta.name)
    )
