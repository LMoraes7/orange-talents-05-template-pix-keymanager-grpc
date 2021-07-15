package br.com.zup.edu.grpc.dto

import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.modelo.ContaAssociada
import br.com.zup.edu.grpc.dominio.modelo.Instuicao
import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.dominio.validacao.ChaveIsValid
import br.com.zup.edu.grpc.http.client.ContaAssociadaResponse
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ChaveIsValid
@Introspected
class ChavePixRequestDto(
    @field:NotBlank
    val clienteId: String,
    @field:NotNull
    val tipoChave: TipoChaveModel,
    val chave: String,
    @field:NotNull
    val tipoConta: TipoContaModel,
) {
    fun paraChavePixModel(contaAssociadaResponse: ContaAssociadaResponse): ChavePix =
        ChavePix(
            clienteId = this.clienteId,
            tipoChave = this.tipoChave,
            chave =
                if (this.tipoChave == TipoChaveModel.ALEATORIA)
                    UUID.randomUUID().toString()
                else
                    this.chave,
            tipoConta = this.tipoConta,
            conta = ContaAssociada(
                tipo = contaAssociadaResponse.tipo,
                instituicao = Instuicao(
                    nome = contaAssociadaResponse.instituicao.nome,
                    ispb = contaAssociadaResponse.instituicao.ispb
                ),
                agencia = contaAssociadaResponse.agencia,
                numero = contaAssociadaResponse.numero,
                titular = Titular(
                    id = contaAssociadaResponse.titular.id,
                    nome = contaAssociadaResponse.titular.nome,
                    cpf = contaAssociadaResponse.titular.cpf
                )
            )
        )
}
