package br.com.zup.edu.grpc.endpoint.cadastrar.dto

import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.modelo.ContaAssociada
import br.com.zup.edu.grpc.dominio.modelo.Instuicao
import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.dominio.validacao.ChaveIsValid
import br.com.zup.edu.grpc.dominio.validacao.IsUUID
import br.com.zup.edu.grpc.http.client.itau.response.ContaAssociadaResponse
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ChaveIsValid
@Introspected
class ChavePixCadastrarRequestDto(
    @field:NotBlank
    @field:IsUUID
    val clienteId: String,
    @field:NotNull
    val tipoChave: TipoChaveModel,
    @field:Size(max = 77)
    val chave: String = "",
    @field:NotNull
    val tipoConta: TipoContaModel,
) {
    fun paraChavePixModel(contaAssociadaResponse: ContaAssociadaResponse): ChavePix =
        ChavePix(
            clienteId = this.clienteId,
            tipoChave = this.tipoChave,
            chave = this.chave,
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
