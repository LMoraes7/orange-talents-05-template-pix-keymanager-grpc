package br.com.zup.edu.grpc.endpoint.consultar

import br.com.zup.edu.*
import br.com.zup.edu.ChavePixConsultarRequest.FiltroCase.*
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.consultar.dto.response.ChavePixResponseDto
import br.com.zup.edu.grpc.endpoint.consultar.util.Filtro
import br.com.zup.edu.grpc.handler.ErrorAroundHandler
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@ErrorAroundHandler
@Singleton
class ConsultarChavePixEndpoint(
    private val chavePixRepository: ChavePixRepository,
    private val bcbClient: BcbClient,
    private val validator: Validator,
) : ChavePixConsultarServiceGrpc.ChavePixConsultarServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun consultar(
        request: ChavePixConsultarRequest,
        responseObserver: StreamObserver<ChavePixConsultarResponse>,
    ) {
        this.logger.info("enpoint -> recebendo requisição para consulta de chave pix")
        this.logger.info("endpoint -> efetuando validações de entrada para a requisição")

        val chaveDto: ChavePixResponseDto = request.toParaFiltro(this.validator, this.logger)
            .run {
                logger.info("endpoint -> repassando requisição para o filtro de consulta")
                this.filtrar(chavePixRepository, bcbClient)
            }

        this.logger.info("endpoint -> convertendo dados recebidos do filtro para um response")

        responseObserver.onNext(this.createChavePixConsultarResponse(chaveDto))

        this.logger.info("endpoint -> terminando requisição para consulta de chave pix")

        responseObserver.onCompleted()
    }

    private fun createChavePixConsultarResponse(chaveDto: ChavePixResponseDto):
            ChavePixConsultarResponse =
        ChavePixConsultarResponse.newBuilder()
            .setPixId(chaveDto.pixId ?: "")
            .setClienteId(chaveDto.clienteId ?: "")
            .setTipoChave(chaveDto.tipoChave)
            .setChave(chaveDto.chave)
            .setTitular(
                Titular.newBuilder()
                    .setNome(chaveDto.titular.nome)
                    .setCpf(chaveDto.titular.cpf)
                    .build()
            )
            .setContaAssociada(
                ContaAssociada.newBuilder()
                    .setInstituicao(chaveDto.contaAssociada.instituicao)
                    .setAgencia(chaveDto.contaAssociada.agencia)
                    .setNumero(chaveDto.contaAssociada.numero)
                    .setTipoConta(chaveDto.contaAssociada.tipoConta)
                    .build()
            )
            .setDataRegistro(chaveDto.dataRegistro)
            .build()

}

private fun ChavePixConsultarRequest.toParaFiltro(validator: Validator, logger: Logger): Filtro {
    val filtro: Filtro = when (filtroCase) {
        PIXID -> {
            logger.info("endpoint -> requisição para consulta de chave pix será através de um cliente id e um pix id interno")
            return Filtro.PorClienteEChave(this.pixId.clienteId, this.pixId.pixId)
        }
        CHAVE -> {
            logger.info("endpoint -> requisição para consulta de chave pix será através de uma chave pix")
            return Filtro.PorChave(this.chave)
        }
        FILTRO_NOT_SET -> {
            logger.info("endpoint -> requisição para consulta de chave pix será invalidada")
            return Filtro.Invalida()
        }
    }

    val errors = validator.validate(filtro)

    if (errors.isNotEmpty())
        throw ConstraintViolationException(errors)
    return filtro
}

