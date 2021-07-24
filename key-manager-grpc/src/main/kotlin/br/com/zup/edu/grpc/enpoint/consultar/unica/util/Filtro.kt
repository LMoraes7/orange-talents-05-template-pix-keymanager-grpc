package br.com.zup.edu.grpc.enpoint.consultar.unica.util

import br.com.zup.edu.grpc.dominio.exception.ChavePixInexistenteException
import br.com.zup.edu.grpc.dominio.exception.DadosDeEntrandaInvalidosException
import br.com.zup.edu.grpc.dominio.repository.ChavePixRepository
import br.com.zup.edu.grpc.dominio.validacao.ValidarUUID
import br.com.zup.edu.grpc.enpoint.consultar.unica.request.ChavePixResponseDto
import br.com.zup.edu.grpc.http.client.bcb.BcbClient
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

//@Introspected
sealed class Filtro {

    abstract fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto

    @Introspected
    class PorClienteEhChaveId(
        @field:NotBlank
        @field:ValidarUUID
        val clienteId: String?,
        @field:NotBlank
        @field:ValidarUUID
        val pixId: String?,
    ) : Filtro() {
        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto =
            repository.consultarPeloIdInternoEPeloCliente(this.pixId!!, this.clienteId!!)
                .map { chave -> ChavePixResponseDto.build(chave) }
                .orElseThrow { throw ChavePixInexistenteException("Chave pix junto ao cliente informado não existe") }
    }

    @Introspected
    class PorChavePix(
        @field:NotBlank
        @field:Size(max = 77)
        val chave: String?,
    ) : Filtro() {
        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto =
            bcbClient.consultarPorChave(this.chave!!)
                .run {
                    if (!this.status.equals(HttpStatus.OK))
                        throw ChavePixInexistenteException("Chave pix informada não existe no Bacen")
                    this.body()!!.paraChavePixResponseDto()
                }
    }

    @Introspected
    class Invalidar : Filtro() {
        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixResponseDto {
            throw DadosDeEntrandaInvalidosException("Requisição para consulta de chave pix foi inválida")
        }
    }
}
