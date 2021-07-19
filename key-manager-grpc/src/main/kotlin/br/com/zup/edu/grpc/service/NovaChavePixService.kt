package br.com.zup.edu.grpc.service

import br.com.zup.edu.grpc.dominio.exception.badrequest.ChavePixDuplicadaException
import br.com.zup.edu.grpc.dominio.exception.notfound.ContaNaoEncontradaException
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.endpoint.cadastrar.dto.ChavePixCadastrarRequestDto
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import br.com.zup.edu.grpc.http.client.bcb.dto.request.KeyType
import br.com.zup.edu.grpc.http.client.itau.ItauClient
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    private val itauClient: ItauClient,
    private val bcbClient: BcbClient,
    private val chavePixRepository: ChavePixRepository,
) {

    private val logger = LoggerFactory.getLogger(NovaChavePixService::class.java)

    @Transactional
    fun cadastrar(@Valid chavePixCadastrarDto: ChavePixCadastrarRequestDto): String {

        this.logger.info("service -> recebendo requisição para cadastro de chave pix")
        this.logger.info("service -> verificando se a chave informada já está cadastrada no sistema")

        this.verificarSeAChavePixInformadaJaExisteNoSistemaLocal(chavePixCadastrarDto)

        this.logger.info("service -> efetuando busca do cliente no sistema itaú")

        val chavePix: ChavePix = this.buscarContaNoSistemaDoItau(chavePixCadastrarDto)
            .run {
                chavePixCadastrarDto.paraChavePixModel(this)
            }

        this.logger.info("service -> efetuando persistência da chave pix na base de dados do sistema interno")

        this.chavePixRepository.save(chavePix)

        this.logger.info("service -> efetuando requisição de cadastrado de chave pix no sistema do banco central do brasil")

        this.cadastrarChavePixNoBacen(chavePix)

        this.logger.info("service -> retornando o pixIdInterno para o endpoint")

        return chavePix.pixIdInterno

    }

    private fun verificarSeAChavePixInformadaJaExisteNoSistemaLocal(chavePixCadastrarDto: ChavePixCadastrarRequestDto) {
        if (this.chavePixRepository.existsByChave(chavePixCadastrarDto.chave))
            throw ChavePixDuplicadaException("Chave informada já está cadastrada")
    }

    private fun buscarContaNoSistemaDoItau(chavePixCadastrarDto: ChavePixCadastrarRequestDto) =
        this.itauClient.buscarContaCliente(chavePixCadastrarDto.clienteId, chavePixCadastrarDto.tipoConta.name)
            .run {
                this.body()
                    ?: throw ContaNaoEncontradaException("A conta do cliente informado não existe no sistema Itaú")
            }

    private fun cadastrarChavePixNoBacen(chavePix: ChavePix) {
        this.bcbClient.cadastrar(chavePix.paraCreateChavePixKeyRequest())
            .run {
                if (this.status == HttpStatus.UNPROCESSABLE_ENTITY)
                    throw ChavePixDuplicadaException("Chave informada já está cadastrada")

                if (chavePix.tipoChave.keyType() == KeyType.RANDOM) {
                    logger.info("service -> atualizando chave pix na base de dados do sistema interno")
                    chavePixRepository.atualizarChavePeloIdInterno(this.body().key, chavePix.pixIdInterno)
                }
            }
    }
}
