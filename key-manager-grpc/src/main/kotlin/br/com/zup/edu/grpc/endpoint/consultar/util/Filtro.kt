package br.com.zup.edu.grpc.endpoint.consultar.util

import br.com.zup.edu.grpc.dominio.exception.badrequest.ValidacaoException
import br.com.zup.edu.grpc.dominio.exception.notfound.ChavePixNaoEncontradaException
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.consultar.dto.response.ChavePixResponseDto
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto

    @Introspected
    data class PorClienteEChave(
        @field:NotBlank
        val clienteId: String,
        @field:NotBlank
        val pixId: String,
    ) : Filtro() {

        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto {
            this.logger.info("filtro -> buscando dados da chave pix na base de dados interna")
            return repository.buscarChavePeloIdDoClienteEPeloIdInterno(this.clienteId, this.pixId)
                .map { chave ->
                    this.logger.info("filtro -> retornando dados da chave para o endpoint")
                    ChavePixResponseDto.build(chave)
                }
                .orElseThrow {
                    this.logger.info("filtro -> chave não foi encontrada na base de dados interna")
                    throw ChavePixNaoEncontradaException("Chave PIX informada não existe no sistema")
                }
        }
    }

    @Introspected
    data class PorChave(
        @field:NotBlank
        @field:Size(max = 77)
        val chave: String,
    ) : Filtro() {

        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto {
            this.logger.info("filtro -> buscando dados da chave pix na base de dados do bacen")
            val response = bcbClient.consultarPorChave(this.chave)

            return when (response.status) {
                HttpStatus.OK -> {
                    this.logger.info("filtro -> retornando dados da chave para o endpoint")
                    return response.body().paraChavePixResponseDto()
                }
                else -> {
                    this.logger.info("filtro -> chave não foi encontrada na base de dados do bacen")
                    throw ChavePixNaoEncontradaException("Chave PIX informada não existe no sistema do Bacen")
                }
            }
        }
    }

    @Introspected
    class Invalida : Filtro() {

        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto {
            this.logger.info("filtro- > dados da requisição são inválidas")
            throw ValidacaoException("Requisição para consulta de chave PIX foi inválida")
        }
    }
}