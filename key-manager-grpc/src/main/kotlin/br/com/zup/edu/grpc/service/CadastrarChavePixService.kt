package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.ChavePixDuplicadaException
import br.com.zup.edu.grpc.dominio.exception.ContaInexistenteException
import br.com.zup.edu.grpc.dominio.exception.ErroDesconhecidoException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.enpoint.cadastrar.dto.NovaChavePixRequestDto
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.bcb.request.KeyType
import br.com.zup.edu.grpc.http.client.bcb.response.CreatePixKeyResponse
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import br.com.zup.edu.grpc.http.client.itau.response.DadosDaContaResponse
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class CadastrarChavePixService(
    private val itauClient: ItauClient,
    private val bcbClient: BcbClient,
    private val repository: ChavePixRepository,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun cadastrar(@Valid chaveRequest: NovaChavePixRequestDto): ChavePix {

        this.logger.info("service -> recebendo requisição para cadastro de chave pix")
        this.logger.info("service -> verificando se a chave informada já está cadastrada no sistema")

        this.consultarSeAChaveJaExisteNoSistemaDoBacen(chaveRequest)

        this.logger.info("service -> efetuando busca do cliente no sistema itaú")

        val chaveModel = this.buscarContaDoClienteNoSistemaItau(chaveRequest)
            .run {
                chaveRequest.paraChavePixModel(this)
            }.let {
                this.cadastrarChavePixNoSistemaInterno(it)
            }

        return this.cadastrarChavePixNoSistemaDoBacen(chaveModel)
            .run {
                this.verificaSePrecisaAtualizarChavePixEAtualiza(chaveModel)
            }
    }

    private fun CreatePixKeyResponse.verificaSePrecisaAtualizarChavePixEAtualiza(chaveModel: ChavePix): ChavePix {
        if (this.keyType.equals(KeyType.RANDOM.name))
            repository.atualizarChavePixComNovaChave(this.key, chaveModel.identificador!!)
        return chaveModel
    }

    private fun cadastrarChavePixNoSistemaDoBacen(chaveModel: ChavePix) =
        this.bcbClient.cadastrar(chaveModel.paraCreatePixKeyRequest()).body()!!


    private fun cadastrarChavePixNoSistemaInterno(it: ChavePix): ChavePix {
        this.logger.info("service -> efetuando persistência da chave pix na base de dados do sistema interno")
        return repository.save(it)
    }

    private fun consultarSeAChaveJaExisteNoSistemaDoBacen(chaveRequest: NovaChavePixRequestDto) {
        if (!chaveRequest.chave.isNullOrBlank()) {
            this.bcbClient.consultarPorChave(chaveRequest.chave)
                .run {
                    if (!this.status.equals(HttpStatus.NOT_FOUND))
                        throw ChavePixDuplicadaException("Chave pix informada já está cadastrada no Bacen")
                }
        }
    }

    private fun buscarContaDoClienteNoSistemaItau(chaveRequest: NovaChavePixRequestDto):
            DadosDaContaResponse {
        val response =
            this.itauClient.buscarClienteEConta(
                chaveRequest.clienteId!!,
                chaveRequest.tipoConta.name
            )

        return with(response) {
            when (this.status) {
                HttpStatus.OK -> this.body()!!
                HttpStatus.NOT_FOUND -> throw ContaInexistenteException("Dados do cliente não existem no sistema Itaú")
                else -> throw ErroDesconhecidoException("${this.status} - Erro desconhecido durante a resposta do sistema Itaú")
            }
        }
    }
}
