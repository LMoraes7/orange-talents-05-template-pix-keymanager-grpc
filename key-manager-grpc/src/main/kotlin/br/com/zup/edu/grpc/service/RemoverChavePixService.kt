package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.notfound.ChavePixNaoEncontradaException
import br.com.zup.edu.grpc.dominio.exception.notfound.ClienteNaoEncontradoException
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.remover.dto.ChavePixRemoverRequestDto
import br.com.zup.edu.grpc.http.client.ItauClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class RemoverChavePixService(
    private val chavePixRepository: ChavePixRepository,
    private val itauClient: ItauClient,
) {

    private val logger = LoggerFactory.getLogger(RemoverChavePixService::class.java)

    fun remover(chaveDto: ChavePixRemoverRequestDto) {

        this.logger.info("service -> efetuando busca do cliente no sistema itaú")

        try {
            this.itauClient.buscarCliente(chaveDto.clienteId!!)
        } catch (e: HttpClientResponseException) {
            throw ClienteNaoEncontradoException("Cliente informado não existe no sistema Itaú")
        }

        this.logger.info("service -> efetuando busca da chave pix no sistema interno")

        this.chavePixRepository.buscarPeloIdInterno(chaveDto.pixIdInterno!!)
            .run {
                this.ifPresentOrElse({
                    logger.info("service -> efetuando remoção da chave pix na base de dados")
                    chavePixRepository.deletarPeloIdInterno(chaveDto.pixIdInterno)
                }, {
                    throw ChavePixNaoEncontradaException("Chave PIX informada não existe no sistema")
                })
            }

        this.logger.info("service -> retornando o fluxo da requisição para o endpoint")
    }
}
