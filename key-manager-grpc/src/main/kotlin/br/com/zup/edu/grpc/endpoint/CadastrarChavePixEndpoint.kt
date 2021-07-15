package br.com.zup.edu.grpc.endpoint

import br.com.zup.edu.*
import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.exception.ValidacaoException
import br.com.zup.edu.grpc.dto.ChavePixRequestDto
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.service.NovaChavePixService
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@ErrorAroundHandler
@Singleton
class CadastrarChavePixEndpoint(
    private val service: NovaChavePixService,
    private val validator: Validator,
) : ChavePixServiceGrpc.ChavePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(CadastrarChavePixEndpoint::class.java)

    override fun cadastrar(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>,
    ) {
        this.logger.info("Endpoint -> recebendo requisição para cadastro de chave PIX")
        this.logger.info("Endpoint -> efetuando validações de entrada para a requisição")

        val chavePixDto: ChavePixRequestDto = request.paraChavePixRequestDto(this.validator)

        this.logger.info("Endpoint -> repassando requisição para a Service")

        this.service.cadastrar(chavePixDto).run {
            logger.info("Endpoint -> recebendo o pixIdInterno retornado pela Service")
            responseObserver.onNext(ChavePixResponse.newBuilder().setPixId(this).build())
        }.let {
            this.logger.info("Endpoint -> terminando requisição para cadastro de chave PIX")
            responseObserver.onCompleted()
        }
    }
}

private fun ChavePixRequest.paraChavePixRequestDto(validator: Validator): ChavePixRequestDto =
    ChavePixRequestDto(
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
