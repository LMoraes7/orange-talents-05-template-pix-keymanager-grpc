package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.forbidden.ManipulacaoInvalidaDeChaveException
import br.com.zup.edu.grpc.dominio.exception.notfound.ChavePixNaoEncontradaException
import br.com.zup.edu.grpc.dominio.exception.notfound.ClienteNaoEncontradoException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.remover.dto.ChavePixRemoverRequestDto
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class RemoverChavePixService(
    private val chavePixRepository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BcbClient,
) {

    private val logger = LoggerFactory.getLogger(RemoverChavePixService::class.java)

    @Transactional
    fun remover(chaveDto: ChavePixRemoverRequestDto) {

        this.logger.info("service -> efetuando busca do cliente no sistema itaú")

        this.verificarSeOClienteExisteNoSistemaDoItau(chaveDto)

        this.logger.info("service -> efetuando busca da chave pix no sistema interno")

        this.removerChavePixNoSistemaInterno(chaveDto).run {
            logger.info("service -> efetuando remoção da chave pix no sistema do bacen")
            removerChavePixNoSistemaDoBacen(this)
        }

        this.logger.info("service -> retornando o fluxo da requisição para o endpoint")
    }

    private fun removerChavePixNoSistemaDoBacen(chavePixRemovida: ChavePix) {
        this.bcbClient.remover(chavePixRemovida.chave, chavePixRemovida.paraDeletePixKeyRequest())
            .run {
                if (this.status.code == HttpStatus.FORBIDDEN.code)
                    throw ManipulacaoInvalidaDeChaveException("O usuário não está autorizado a realizar a exclusão da chave pix")

                if (this.status.code == HttpStatus.NOT_FOUND.code)
                    throw ChavePixNaoEncontradaException("Chave PIX informada não existe no sistema do Bacen")
            }
    }

    private fun removerChavePixNoSistemaInterno(chaveDto: ChavePixRemoverRequestDto): ChavePix =
        //Verifica se a chave pix existe no BD
        this.chavePixRepository.buscarPeloIdInterno(chaveDto.pixIdInterno!!)
            .run {
                this.ifPresentOrElse({
                    logger.info("service -> efetuando remoção da chave pix na base de dados")
                    chavePixRepository.deletarPeloIdInterno(chaveDto.pixIdInterno)
                }, {
                    throw ChavePixNaoEncontradaException("Chave PIX informada não existe no sistema")
                })
                this.get()
            }


    private fun verificarSeOClienteExisteNoSistemaDoItau(chaveDto: ChavePixRemoverRequestDto) {
        try {
            this.itauClient.buscarCliente(chaveDto.clienteId!!)
        } catch (e: HttpClientResponseException) {
            throw ClienteNaoEncontradoException("Cliente informado não existe no sistema Itaú")
        }
    }
}
