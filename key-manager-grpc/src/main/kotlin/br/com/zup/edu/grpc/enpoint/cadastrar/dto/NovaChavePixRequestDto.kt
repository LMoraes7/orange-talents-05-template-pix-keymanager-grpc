package br.com.zup.edu.grpc.enpoint.cadastrar.dto

import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.dominio.modelo.ChavePix
import br.com.zup.edu.grpc.dominio.validacao.ValidarChave
import br.com.zup.edu.grpc.dominio.validacao.ValidarUUID
import br.com.zup.edu.grpc.http.client.itau.response.DadosDaContaResponse
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidarChave
@Introspected
class NovaChavePixRequestDto(
    @field:NotBlank
    @field:ValidarUUID
    val clienteId: String?,
    @field:NotNull
    val tipoChave: TipoChaveModel,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoContaModel,
) {
    fun paraChavePixModel(dadosDaContaResponse: DadosDaContaResponse) =
        ChavePix(
            clienteId = this.clienteId!!,
            tipoChave = this.tipoChave,
            chave =
            if (this.tipoChave.equals(TipoChaveModel.ALEATORIA))
                ""
            else
                this.chave!!,
            tipoConta = this.tipoConta,
            conta = dadosDaContaResponse.paraContaModel()
        )
}
