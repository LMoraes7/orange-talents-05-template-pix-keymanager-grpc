package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.forbidden.ManipulacaoInvalidaDeChaveException
import br.com.zup.edu.grpc.dominio.exception.notfound.ChavePixNaoEncontradaException
import br.com.zup.edu.grpc.dominio.exception.notfound.ClienteNaoEncontradoException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.consultar.util.VerificarSeClienteExisteNoSistemaItau
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

        VerificarSeClienteExisteNoSistemaItau.verifica(this.itauClient, chaveDto.clienteId!!)

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
                if (this.status == HttpStatus.FORBIDDEN)
                    throw ManipulacaoInvalidaDeChaveException("O usuário não está autorizado a realizar a exclusão da chave pix")

                if (this.status == HttpStatus.NOT_FOUND)
                    throw ChavePixNaoEncontradaException("Chave PIX informada não existe no sistema do Bacen")
            }
    }

    // Ao invés de buscar somente pelo pixIdInterno e depois fazer um 'if' para verificar se os
    //  id's dos clientes são iguais, preferi buscar a ChavePix pelo pixIdInterno e pelo clienteId informados
    //      fazendo um AND na consulta no BD
    //
    //Com isso deixaria de retornar um código 422 para o consumidor e retornaria um 404.
    //  Mas vou deixar comentado o código com o retorno 422 abaixo como exemplo que eu iria usar
    //
    //Claro, no exemplo que eu apliquei a semântica de retorno poderia ficar um pouco confusa para o cliente,
    //  mas acredito que em um sistema que possua interface gráfica,
    //      um usuário não poderia e nem deveria 'ver' chaves pix que não lhe pertencem
    //
    private fun removerChavePixNoSistemaInterno(chaveDto: ChavePixRemoverRequestDto): ChavePix =
        this.chavePixRepository.buscarChavePeloIdDoClienteEPeloIdInterno(chaveDto.clienteId!!, chaveDto.pixIdInterno!!)
            //this.chavePixRepository.buscarPeloIdInterno(chaveDto.pixIdInterno!!)
            .run {
                this.ifPresentOrElse({
                    //if(!chaveDto.clienteId.equals(it.clienteId))
                    //throw ManipulacaoInvalidaDeChaveException("O usuário não está autorizado a realizar a exclusão da chave pix")
                    logger.info("service -> efetuando remoção da chave pix na base de dados")
                    chavePixRepository.deletarPeloIdInterno(chaveDto.pixIdInterno)
                }, {
                    throw ChavePixNaoEncontradaException("Chave PIX informada não existe no sistema")
                })
                this.get()
            }

    private fun verificarSeOClienteExisteNoSistemaDoItau(chaveDto: ChavePixRemoverRequestDto) {
        this.itauClient.buscarCliente(chaveDto.clienteId!!)
            .run {
                if (this.status == HttpStatus.NOT_FOUND)
                    throw ClienteNaoEncontradoException("Cliente informado não existe no sistema Itaú")
            }

        //try {
        //    this.itauClient.buscarCliente(chaveDto.clienteId!!)
        //} catch (e: HttpClientResponseException) {
        //    throw ClienteNaoEncontradoException("Cliente informado não existe no sistema Itaú")
        //}
    }
}