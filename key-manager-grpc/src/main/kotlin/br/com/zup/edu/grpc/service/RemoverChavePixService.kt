package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.ChavePixInexistenteException
import br.com.zup.edu.grpc.dominio.exception.ExclusaoDeChavePixInvalidaException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.enpoint.remover.dto.RemoverChavePixRequestDto
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class RemoverChavePixService(
    private val repository: ChavePixRepository,
    private val bcbClient: BcbClient,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun remover(@Valid chaveRequest: RemoverChavePixRequestDto): Boolean {
        this.logger.info("service -> efetuando busca da chave pix com o cliente no sistema interno")

        val chavePix =
            this.verificarSeChavePixComOClienteEstaCadastradaNoSistema(chaveRequest)

        this.removerChavePixDoSistemaDoBacen(chavePix)

        this.logger.info("service -> efetuando remoção da chave pix na base de dados")

        this.repository.deleteById(chavePix.identificador!!)
        return true
    }

    private fun removerChavePixDoSistemaDoBacen(chavePix: ChavePix) {
        this.bcbClient.remover(chavePix.chave, chavePix.paraDeletePixKeyRequest())
            .run {
                if (!this.status.equals(HttpStatus.OK))
                    throw ExclusaoDeChavePixInvalidaException("Exclusão de chave pix não foi permitida")
            }
    }

    private fun verificarSeChavePixComOClienteEstaCadastradaNoSistema(chaveRequest: RemoverChavePixRequestDto) =
        this.repository.consultarPeloIdInternoEPeloCliente(
            chaveRequest.pixId!!,
            chaveRequest.clienteId!!
        ).run {
            this.orElseThrow {
                throw ChavePixInexistenteException(
                    "Chave pix junto ao cliente informado não existe"
                )
            }
        }

}
