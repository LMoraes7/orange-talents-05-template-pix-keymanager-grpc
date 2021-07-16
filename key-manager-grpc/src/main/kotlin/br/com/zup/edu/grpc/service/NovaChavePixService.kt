package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.ChavePixDuplicadaException
import br.com.zup.edu.grpc.dominio.exception.ContaInexisteException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.dto.ChavePixRequestDto
import br.com.zup.edu.grpc.http.client.ContaAssociadaResponse
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

    fun cadastrar(chavePixDto: ChavePixRequestDto): String {

        this.logger.info("Service -> recebendo requisição para cadastro de chave PIX")
        this.logger.info("Service -> verificando se a chave informada já está cadastrada no sistema")

        if (this.chavePixRepository.existsByChave(chavePixDto.chave))
            throw ChavePixDuplicadaException("Chave informada já está cadastrada")

        this.logger.info("Service -> efetuando busca do cliente no sistema Itaú")

        val contaAssociadaResponse: ContaAssociadaResponse =
            try {
                this.itauClient.buscarContaCliente(
                    chavePixDto.clienteId,
                    chavePixDto.tipoConta.name
                ).body.get()
            } catch (e: HttpClientResponseException) {
                throw ContaInexisteException("Conta não existe no sistema Itaú")
            }

        //val contaAssociadaResponse: ContaAssociadaResponse = this.itauClient.buscarContaCliente(
        //    chavePixDto.clienteId,
        //    chavePixDto.tipoConta.name
        //).run {
        //    this.body() ?: throw ContaInexisteException("Conta não existe no sistema Itaú")
        //}

        val chavePix: ChavePix = chavePixDto.paraChavePixModel(contaAssociadaResponse)

        this.logger.info("Service -> efetuando persistência da chave PIX na base de dados")

        return this.chavePixRepository.save(chavePix)
            .let {
                this.logger.info("Service -> retornando o pixIdInterno para o Endpoint")
                it.pixIdInterno
            }
    }
}
