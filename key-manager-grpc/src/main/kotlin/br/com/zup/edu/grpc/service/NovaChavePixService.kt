package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.badrequest.ChavePixDuplicadaException
import br.com.zup.edu.grpc.dominio.exception.notfound.ContaNaoEncontradaException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.cadastrar.dto.ChavePixCadastrarRequestDto
import br.com.zup.edu.grpc.http.client.response.ContaAssociadaResponse
import br.com.zup.edu.grpc.http.client.ItauClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class NovaChavePixService(
    private val itauClient: ItauClient,
    private val chavePixRepository: ChavePixRepository,
) {

    private val logger = LoggerFactory.getLogger(NovaChavePixService::class.java)

    fun cadastrar(chavePixCadastrarDto: ChavePixCadastrarRequestDto): String {

        this.logger.info("service -> recebendo requisição para cadastro de chave pix")
        this.logger.info("service -> verificando se a chave informada já está cadastrada no sistema")

        if (this.chavePixRepository.existsByChave(chavePixCadastrarDto.chave))
            throw ChavePixDuplicadaException("Chave informada já está cadastrada")

        this.logger.info("service -> efetuando busca do cliente no sistema itaú")

        val contaAssociadaResponse: ContaAssociadaResponse =
            try {
                this.itauClient.buscarContaCliente(
                    chavePixCadastrarDto.clienteId,
                    chavePixCadastrarDto.tipoConta.name
                ).body.get()
            } catch (e: HttpClientResponseException) {
                throw ContaNaoEncontradaException("Conta não existe no sistema Itaú")
            }

        val chavePix: ChavePix = chavePixCadastrarDto.paraChavePixModel(contaAssociadaResponse)

        this.logger.info("service -> efetuando persistência da chave pix na base de dados")

        return this.chavePixRepository.save(chavePix)
            .let {
                this.logger.info("service -> retornando o pixIdInterno para o endpoint")
                it.pixIdInterno
            }
    }
}
