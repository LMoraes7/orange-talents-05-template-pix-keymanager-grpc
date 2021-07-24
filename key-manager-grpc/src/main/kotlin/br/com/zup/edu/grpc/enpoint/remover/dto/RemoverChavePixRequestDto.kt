package br.com.zup.edu.grpc.enpoint.remover.dto

import br.com.zup.edu.grpc.dominio.validacao.ValidarUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class RemoverChavePixRequestDto(
    @field:NotBlank
    @field:ValidarUUID
    val pixId: String?,
    @field:NotBlank
    @field:ValidarUUID
    val clienteId: String?,
) {

}
