package br.com.zup.edu.grpc.endpoint.remover.dto

import br.com.zup.edu.grpc.dominio.validacao.IsUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class ChavePixRemoverRequestDto(
    @field:NotBlank
    @field:IsUUID
    val clienteId: String?,
    @field:NotBlank
    val pixIdInterno: String?,
) {

}
