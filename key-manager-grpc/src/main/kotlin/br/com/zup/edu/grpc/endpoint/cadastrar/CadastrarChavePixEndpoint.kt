package br.com.zup.edu.grpc.endpoint.cadastrar

import br.com.zup.edu.*
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.exception.badrequest.ValidacaoException
import br.com.zup.edu.grpc.endpoint.cadastrar.dto.ChavePixCadastrarRequestDto
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.service.NovaChavePixService
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@ErrorAroundHandler
@Singleton
class CadastrarChavePixEndpoint(
    private val service: NovaChavePixService,
    private val validator: Validator,
) : ChavePixCadastrarServiceGrpc.ChavePixCadastrarServiceImplBase() {

    private val logger: Logger = LoggerFactory.getLogger(CadastrarChavePixEndpoint::class.java)

    override fun cadastrar(
        request: ChavePixCadastrarRequest,
        responseObserver: StreamObserver<ChavePixCadastrarResponse>
    ) {
        this.logger.info("endpoint -> recebendo requisição para cadastro de chave pix")
        this.logger.info("endpoint -> efetuando validações de entrada para a requisição")

        val chavePixCadastrarDto: ChavePixCadastrarRequestDto = request.paraChavePixCadastrarRequestDto(this.validator)

        this.logger.info("endpoint -> repassando requisição para a Service")

        this.service.cadastrar(chavePixCadastrarDto).run {
            logger.info("endpoint -> recebendo o pixIdInterno retornado pela service")
            responseObserver.onNext(ChavePixCadastrarResponse.newBuilder().setPixId(this).build())
        }.let {
            this.logger.info("endpoint -> terminando requisição para cadastro de chave pix")
            responseObserver.onCompleted()
        }
    }
}

private fun ChavePixCadastrarRequest.paraChavePixCadastrarRequestDto(validator: Validator): ChavePixCadastrarRequestDto =
    ChavePixCadastrarRequestDto(
        clienteId = this.clienteId,
        tipoChave =
        if
                (this.tipoChave == TipoChave.CHAVE_DESCONHECIDA)
            throw ValidacaoException("Tipo da chave deve ser informada")
        else
            TipoChaveModel.valueOf(this.tipoChave.name),
        chave = this.chave,
        tipoConta =
        if
                (this.tipoConta == TipoConta.CONTA_DESCONHECIDA)
            throw ValidacaoException("Tipo da conta deve ser informada")
        else
            TipoContaModel.valueOf(this.tipoConta.name),
    ).run {
        val errors = validator.validate(this)
        if (errors.isNotEmpty())
            throw ConstraintViolationException(errors)
        this
    }
